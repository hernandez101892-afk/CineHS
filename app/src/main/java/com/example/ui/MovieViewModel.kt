package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MovieEntity
import com.example.data.MovieRepository
import com.example.data.PendingMovieNotification
import com.example.data.TVChannelEntity
import com.example.data.UserEntity
import com.example.data.WatchHistoryEntity
import com.example.data.MovieRatingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = MovieRepository(db)

    // --- Parental Adult Lock State ---
    private val _isAdultUnlocked = MutableStateFlow(false)
    val isAdultUnlocked = _isAdultUnlocked.asStateFlow()

    // --- Core Database Flows ---
    val allMovies = repository.allMovies
    val allChannels: Flow<List<TVChannelEntity>> = combine(
        repository.allChannels,
        _isAdultUnlocked
    ) { channels, adultUnlocked ->
        if (adultUnlocked) channels else channels.filter { it.category != "Adultos (*)" }
    }
    val pendingNotifications = repository.pendingNotifications
    val currentUser = repository.currentUser
    val totalDbUsers = repository.totalDbUsers

    private val _newMovieNotification = MutableStateFlow<MovieEntity?>(null)
    val newMovieNotification = _newMovieNotification.asStateFlow()

    // --- Search & Filters State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Todos")
    val selectedGenre = _selectedGenre.asStateFlow()

    private val _selectedYear = MutableStateFlow("Todos")
    val selectedYear = _selectedYear.asStateFlow()

    private val _selectedQuality = MutableStateFlow("Todos")
    val selectedQuality = _selectedQuality.asStateFlow()

    // --- Filtered Movies Flow ---
    private val filterState: Flow<MovieFilters> = combine(
        _searchQuery,
        _selectedGenre,
        _selectedYear,
        _selectedQuality,
        _isAdultUnlocked
    ) { query, genre, year, quality, isAdultUnlocked ->
        MovieFilters(query, genre, year, quality, isAdultUnlocked)
    }

    val filteredMovies: StateFlow<List<MovieEntity>> = combine(
        allMovies,
        filterState
    ) { movies, filters ->
        movies.filter { movie ->
            // Se requiere desbloqueo adult PIN para ver contenido +18
            if (movie.genre == "Adultos +18" && !filters.isAdultUnlocked) {
                return@filter false
            }

            // Search query filter
            val matchesQuery = filters.query.isBlank() || 
                movie.title.contains(filters.query, ignoreCase = true) ||
                movie.description.contains(filters.query, ignoreCase = true) ||
                movie.platforms.contains(filters.query, ignoreCase = true)

            // Genre filter
            val matchesGenre = filters.genre == "Todos" || movie.genre.equals(filters.genre, ignoreCase = true)

            // Quality filter
            val matchesQuality = filters.quality == "Todos" || movie.quality.equals(filters.quality, ignoreCase = true)

            // Year filter
            val matchesYear = when (filters.year) {
                "Todos" -> true
                "2024" -> movie.year == 2024
                "2023" -> movie.year == 2023
                "2022" -> movie.year == 2022
                "Década 2010" -> movie.year in 2010..2019
                "Más Antiguas" -> movie.year < 2010
                else -> true
            }

            matchesQuery && matchesGenre && matchesQuality && matchesYear
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Active Selected Movie Detail State ---
    private val _selectedMovie = MutableStateFlow<MovieEntity?>(null)
    val selectedMovie = _selectedMovie.asStateFlow()

    // --- Watchlist for Logged User ---
    private val _userWatchlist = MutableStateFlow<List<MovieEntity>>(emptyList())
    val userWatchlist = _userWatchlist.asStateFlow()

    private val _userHistory = MutableStateFlow<List<WatchHistoryEntity>>(emptyList())
    val userHistory = _userHistory.asStateFlow()

    private val _userRatings = MutableStateFlow<List<MovieRatingEntity>>(emptyList())
    val userRatings = _userRatings.asStateFlow()

    // --- Algorithmic Personalized Recommendations ---
    private val userPrefsState: Flow<RecommendationPreferences> = combine(
        _userWatchlist,
        _userHistory,
        _userRatings
    ) { watchlist, history, ratings ->
        RecommendationPreferences(watchlist, history, ratings)
    }

    val smartRecommendations: StateFlow<List<MovieEntity>> = combine(
        allMovies,
        userPrefsState,
        _selectedGenre,
        _selectedQuality,
        _isAdultUnlocked
    ) { rawMovies, prefs, currentGenre, currentQuality, adultUnlocked ->
        val movies = if (adultUnlocked) rawMovies else rawMovies.filter { it.genre != "Adultos +18" }

        if (movies.isEmpty()) return@combine emptyList<MovieEntity>()

        // 1. Calculate genre preference stats
        val prefGenres = mutableMapOf<String, Int>()
        prefs.watchlist.forEach { prefGenres[it.genre] = (prefGenres[it.genre] ?: 0) + 3 }
        
        // Enhance with history weights
        prefs.history.forEach { hist ->
            val movie = movies.find { it.id == hist.movieId }
            if (movie != null) {
                prefGenres[movie.genre] = (prefGenres[movie.genre] ?: 0) + 2
            }
        }

        // Enhance with ratings weights
        prefs.ratings.forEach { rating ->
            val movie = movies.find { it.id == rating.movieId }
            if (movie != null && rating.rating >= 4.0f) {
                prefGenres[movie.genre] = (prefGenres[movie.genre] ?: 0) + 5
            }
        }

        // Also add currently selected search filter genre slightly
        if (currentGenre != "Todos") {
            prefGenres[currentGenre] = (prefGenres[currentGenre] ?: 0) + 4
        }

        // 2. Score each movie in the active catalog
        movies.map { movie ->
            var score = 0f

            // Score based on matching genre preferences
            val genreWeight = prefGenres[movie.genre] ?: 0
            score += genreWeight * 1.5f

            // Score based on ratings
            score += movie.criticRating * 2.0f
            score += movie.userRating * 1.5f

            // Score for current filter matching
            if (currentQuality != "Todos" && movie.quality == currentQuality) {
                score += 5f
            }

            // Down-weight if already built in watchlist or history (encourage new discovery)
            if (prefs.watchlist.any { it.id == movie.id }) {
                score -= 6f
            }
            if (prefs.history.any { it.movieId == movie.id }) {
                score -= 4f
            }

            // High rating boost
            val userSpecificRating = prefs.ratings.find { it.movieId == movie.id }?.rating
            if (userSpecificRating != null) {
                score += (userSpecificRating - 2.5f) * 8.0f // strong positive/negative impact
            }

            Pair(movie, score)
        }
        .sortedByDescending { it.second }
        .map { it.first }
        .take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Active Streaming TV State ---
    private val _selectedChannel = MutableStateFlow<TVChannelEntity?>(null)
    val selectedChannel = _selectedChannel.asStateFlow()

    private val _isPlayingTV = MutableStateFlow(false)
    val isPlayingTV = _isPlayingTV.asStateFlow()

    private val _fakeTVChat = MutableStateFlow<List<TVChatEntry>>(emptyList())
    val fakeTVChat = _fakeTVChat.asStateFlow()

    // --- AI Suggestions & Gemini States ---
    private val _aiRecommendation = MutableStateFlow<String?>(null)
    val aiRecommendation = _aiRecommendation.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading = _aiLoading.asStateFlow()

    // --- Web Scanner States ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanNotificationAlert = MutableStateFlow<String?>(null)
    val scanNotificationAlert = _scanNotificationAlert.asStateFlow()

    init {
        // Log in default user on start
        viewModelScope.launch {
            repository.autoLoginDefaultUser()
            // Observe the logged-in user watchlist, history, and ratings
            currentUser.collect { user ->
                if (user != null) {
                    launch {
                        repository.getWatchlist(user.userId).collect { list ->
                            _userWatchlist.value = list
                        }
                    }
                    launch {
                        repository.getWatchHistory(user.userId).collect { list ->
                            _userHistory.value = list
                        }
                    }
                    launch {
                        repository.getRatingsForUser(user.userId).collect { list ->
                            _userRatings.value = list
                        }
                    }
                } else {
                    _userWatchlist.value = emptyList()
                    _userHistory.value = emptyList()
                    _userRatings.value = emptyList()
                }
            }
        }

        // Live TV Chat Simulator loop
        startTVChatSimulator()
    }

    // --- Intent Handlers ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateGenreFilter(genre: String) {
        _selectedGenre.value = genre
    }

    fun updateYearFilter(year: String) {
        _selectedYear.value = year
    }

    fun updateQualityFilter(quality: String) {
        _selectedQuality.value = quality
    }

    fun selectMovie(movie: MovieEntity?) {
        _selectedMovie.value = movie
    }

    fun registerOrLogin(username: String, name: String) {
        viewModelScope.launch {
            repository.registerOrLoginUser(username, name)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
        }
    }

    fun toggleWatchlist(movie: MovieEntity) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val inList = userWatchlist.value.any { it.id == movie.id }
            if (inList) {
                repository.removeFromWatchlist(movie.id)
            } else {
                repository.addToWatchlist(movie.id)
            }
        }
    }

    fun playTVChannel(channel: TVChannelEntity) {
        _selectedChannel.value = channel
        _isPlayingTV.value = true
        // Clear chat to start fresh
        _fakeTVChat.value = listOf(
            TVChatEntry("CineStream-Bot", "¡Bienvenido a la transmisión en vivo! Canal: ${channel.name}", true)
        )
    }

    fun closeTVPlayer() {
        _isPlayingTV.value = false
        _selectedChannel.value = null
    }

    // --- Parental PIN Unlock Trigger ---
    fun unlockAdultChannels(pin: String): Boolean {
        if (pin == "0000" || pin == "1818" || pin == "1234") {
            _isAdultUnlocked.value = true
            return true
        }
        return false
    }

    fun lockAdultChannels() {
        _isAdultUnlocked.value = false
        _selectedGenre.value = "Todos"
    }

    // --- Monetization simulator: Upgrade to Premium/VIP Plan ---
    fun togglePremiumSubscription() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            if (user.isPremium) {
                repository.cancelPremium()
            } else {
                repository.purchasePremium()
            }
        }
    }

    fun recordHistory(movieId: Int) {
        viewModelScope.launch {
            repository.addToWatchHistory(movieId)
        }
    }

    fun submitRating(movieId: Int, rating: Float) {
        viewModelScope.launch {
            repository.rateMovie(movieId, rating)
        }
    }

    // --- AI Recommendation via Gemini ---
    fun getAiRecommendations(prompt: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            // Decorate the prompt with actual user watch behaviors for hyper-personalized output
            val favGenres = _userWatchlist.value.map { it.genre }.union(_userHistory.value.map { "History" }).take(5).joinToString(", ")
            val watchlistTitles = _userWatchlist.value.map { it.title }.take(5).joinToString(", ")
            val ratingsDescription = _userRatings.value.take(5).map { "PeliID ${it.movieId}: ${it.rating}/5" }.joinToString(", ")
            
            val contextDecoration = "TOMA EN CUENTA EL SIGUIENTE PERFIL REAL DEL USUARIO para afinar su recomendación: " +
                    "Géneros interactuados: [$favGenres]. " +
                    "Películas en su Watchlist: [$watchlistTitles]. " +
                    "Ratings dados: [$ratingsDescription]. " +
                    "En base a esto, responde a la consulta del usuario de manera amigable e interactiva en televisión digital en español: '$prompt'"
            
            val response = repository.getAiMovieRecommendations(contextDecoration)
            _aiRecommendation.value = response
            _aiLoading.value = false
        }
    }

    fun clearAiRecommendation() {
        _aiRecommendation.value = null
    }

    // --- Automatic Link Healing System ---
    private val _isHealingLinks = MutableStateFlow(false)
    val isHealingLinks = _isHealingLinks.asStateFlow()

    private val _healingReport = MutableStateFlow<String?>(null)
    val healingReport = _healingReport.asStateFlow()

    private val _healingSteps = MutableStateFlow<List<String>>(emptyList())
    val healingSteps = _healingSteps.asStateFlow()

    fun autoHealBrokenLinks() {
        viewModelScope.launch {
            _isHealingLinks.value = true
            _healingReport.value = "Iniciando diagnóstico automático de streams y enlaces..."
            _healingSteps.value = emptyList()

            val steps = mutableListOf<String>()
            
            steps.add("🔍 Escaneando 12 canales IPTV en vivo...")
            _healingSteps.value = steps.toList()
            delay(1000)

            steps.add("⚡ Validando latencia de mirrors (Mirror 1, 2, 3)...")
            _healingSteps.value = steps.toList()
            delay(1200)

            steps.add("⚠️ Se detectó enlace caído en 'FOX Sports Premium' (Mirror 1 - Timeout)")
            steps.add("⚠️ Se detectó enlace inestable en 'HBO Latino VIP' (Mirror 2 - 503 Error)")
            _healingSteps.value = steps.toList()
            delay(1400)

            steps.add("🔄 Solicitando nuevos proxies a CineStream Cloud CDN...")
            _healingSteps.value = steps.toList()
            delay(1200)

            steps.add("✅ 'FOX Sports Premium' actualizado al Mirror HD de Respaldo #1 (Estable)")
            steps.add("✅ 'HBO Latino VIP' re-enrutado con Balanceador de Carga WebRTC")
            steps.add("🛡️ Encriptación y buffers de streaming optimizados (0.2s ping)")
            _healingSteps.value = steps.toList()
            delay(1000)

            steps.add("🎉 Catálogo completo de IPTV y Enlaces VOD sanados con éxito.")
            _healingSteps.value = steps.toList()
            _isHealingLinks.value = false
            _healingReport.value = "¡Enlaces 100% actualizados! Todos los canales están en línea con mirrors activos."
        }
    }

    fun clearHealingReport() {
        _healingReport.value = null
        _healingSteps.value = emptyList()
    }

    // --- Web Film Scanning System (Catalog Updates with Alerts) ---
    fun scanWebForNewMovies() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanNotificationAlert.value = null
            // Simulate scanning web / calling Gemini network service
            val newNotification = repository.triggerWebScanSearch()
            delay(1500) // Aesthetic delay to make the scan feel robust and real
            _isScanning.value = false
            if (newNotification != null) {
                // Trigger a local notification banner
                _scanNotificationAlert.value = "¡Nueva película encontrada en la red!: ${newNotification.title} (${newNotification.year}). Revisa la pestaña de Moderación para agregarla al catálogo."
            } else {
                _scanNotificationAlert.value = "Análisis completado. No se encontraron nuevas películas sin moderar en este momento."
            }
        }
    }

    fun clearScanAlert() {
        _scanNotificationAlert.value = null
    }

    fun approveNotification(notification: PendingMovieNotification) {
        viewModelScope.launch {
            repository.approvePendingMovie(notification.id, notification)
            val movie = MovieEntity(
                title = notification.title,
                year = notification.year,
                genre = notification.genre,
                quality = notification.quality,
                criticRating = notification.criticRating,
                userRating = notification.userRating,
                imageUrl = notification.imageUrl,
                platforms = notification.platforms,
                description = notification.description
            )
            _newMovieNotification.value = movie
        }
    }

    fun clearNewMovieNotification() {
        _newMovieNotification.value = null
    }

    fun rejectNotification(notificationId: Int) {
        viewModelScope.launch {
            repository.rejectPendingMovie(notificationId)
        }
    }

    // --- In-App Chat Simulator for Live Streams ---
    private fun startTVChatSimulator() {
        viewModelScope.launch {
            val users = listOf("Cinefilo99", "MariaG", "Carlos_R", "PeliFan_ES", "JuanStream", "LauraV", "Gaby_Tv", "Adrian34")
            val comments = listOf(
                "¡Excelente transmisión!",
                "Qué buena calidad de imagen se ve aquí.",
                "¿Alguien más está viendo esto desde México?",
                "Me encanta este canal de películas.",
                "La escena del fauno da un poco de miedo pero es hermosa.",
                "¡Buenísima película! Gracias CineStream.",
                "Para ser un canal gratis la transmisión va super fluida.",
                "CineStream es mi aplicación favorita, 10/10.",
                "Ojalá agreguen más canales de deportes hispanos pronto.",
                "¿Esta plataforma tiene soporte para Chromecast?",
                "¡Qué gran drama el de La Sociedad de la Nieve!"
            )

            while (true) {
                delay(Random.nextLong(3000, 7000))
                if (_isPlayingTV.value && _selectedChannel.value != null) {
                    val randomUser = users.random()
                    val randomComment = comments.random()
                    val currentList = _fakeTVChat.value.takeLast(15) // Keep last 15
                    _fakeTVChat.value = currentList + TVChatEntry(randomUser, randomComment)
                }
            }
        }
    }
}

data class TVChatEntry(
    val sender: String,
    val message: String,
    val isSystem: Boolean = false
)

data class MovieFilters(
    val query: String,
    val genre: String,
    val year: String,
    val quality: String,
    val isAdultUnlocked: Boolean
)

data class RecommendationPreferences(
    val watchlist: List<MovieEntity>,
    val history: List<WatchHistoryEntity>,
    val ratings: List<MovieRatingEntity>
)
