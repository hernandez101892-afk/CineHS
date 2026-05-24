package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepository(private val db: AppDatabase) {

    // --- Active Flows from Room ---
    val allMovies: Flow<List<MovieEntity>> = db.movieDao().getAllMovies()
    val allChannels: Flow<List<TVChannelEntity>> = db.tvChannelDao().getAllChannels()
    val pendingNotifications: Flow<List<PendingMovieNotification>> = db.pendingMovieDao().getPendingNotifications()
    val totalDbUsers: Flow<Int> = db.userDao().getUsersCountFlow()

    // --- Active User State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Log in as the default user during start
        // To allow seamless demo of lists and profiles.
    }

    suspend fun autoLoginDefaultUser() {
        withContext(Dispatchers.IO) {
            val defaultUser = db.userDao().getUser("cine_lover")
            if (defaultUser != null) {
                _currentUser.value = defaultUser
            } else {
                val newUser = UserEntity("cine_lover", "Espectador CineStream", false)
                db.userDao().insertUser(newUser)
                _currentUser.value = newUser
            }
        }
    }

    // --- Registration / Management ---
    suspend fun registerOrLoginUser(userId: String, displayName: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (userId.isBlank() || displayName.isBlank()) return@withContext false
            val cleanId = userId.trim().lowercase()
            val existing = db.userDao().getUser(cleanId)
            val user = if (existing != null) {
                existing
            } else {
                val newUser = UserEntity(userId = cleanId, displayName = displayName, isPremium = false)
                db.userDao().insertUser(newUser)
                newUser
            }
            _currentUser.value = user
            true
        }
    }

    suspend fun logoutUser() {
        _currentUser.value = null
    }

    fun getWatchlist(userId: String): Flow<List<MovieEntity>> {
        return db.watchlistDao().getWatchlistForUser(userId)
    }

    suspend fun addToWatchlist(movieId: Int): Boolean {
        val user = _currentUser.value ?: return false
        withContext(Dispatchers.IO) {
            db.watchlistDao().addToWatchlist(WatchlistItemEntity(userId = user.userId, movieId = movieId))
        }
        return true
    }

    suspend fun removeFromWatchlist(movieId: Int): Boolean {
        val user = _currentUser.value ?: return false
        withContext(Dispatchers.IO) {
            db.watchlistDao().removeFromWatchlist(userId = user.userId, movieId = movieId)
        }
        return true
    }

    suspend fun checkIfInWatchlist(movieId: Int): Boolean {
        val user = _currentUser.value ?: return false
        return withContext(Dispatchers.IO) {
            db.watchlistDao().isInWatchlist(user.userId, movieId)
        }
    }

    // --- Persisted Ratings and Watch History (Smart Recommendations Support) ---
    fun getWatchHistory(userId: String): Flow<List<WatchHistoryEntity>> {
        return db.watchHistoryDao().getWatchHistory(userId)
    }

    suspend fun addToWatchHistory(movieId: Int) {
        val user = _currentUser.value ?: return
        withContext(Dispatchers.IO) {
            db.watchHistoryDao().insertHistoryItem(WatchHistoryEntity(userId = user.userId, movieId = movieId))
        }
    }

    fun getRatingsForUser(userId: String): Flow<List<MovieRatingEntity>> {
        return db.movieRatingDao().getRatingsForUser(userId)
    }

    suspend fun rateMovie(movieId: Int, rating: Float) {
        val user = _currentUser.value ?: return
        withContext(Dispatchers.IO) {
            db.movieRatingDao().insertRating(MovieRatingEntity(userId = user.userId, movieId = movieId, rating = rating))
        }
    }

    // --- VIP Subscription (Monetization Engine) ---
    suspend fun purchasePremium(): Boolean {
        val user = _currentUser.value ?: return false
        return withContext(Dispatchers.IO) {
            db.userDao().updateUserSubscription(user.userId, true)
            // Refresh currentUser state
            val updated = db.userDao().getUser(user.userId)
            _currentUser.value = updated
            true
        }
    }

    suspend fun cancelPremium(): Boolean {
        val user = _currentUser.value ?: return false
        return withContext(Dispatchers.IO) {
            db.userDao().updateUserSubscription(user.userId, false)
            // Refresh currentUser state
            val updated = db.userDao().getUser(user.userId)
            _currentUser.value = updated
            true
        }
    }

    // --- Web Scanner for Movies (Notifications Approval System) ---
    suspend fun triggerWebScanSearch(): PendingMovieNotification? {
        return withContext(Dispatchers.IO) {
            try {
                // Call Gemini or local fallback generator
                val scanResult = GeminiClient.scanWebForMovie()
                
                // Construct notification entity
                val notification = PendingMovieNotification(
                    title = scanResult.title,
                    year = scanResult.year,
                    genre = scanResult.genre,
                    quality = scanResult.quality,
                    platforms = scanResult.platforms,
                    description = scanResult.description,
                    criticRating = scanResult.criticRating,
                    userRating = scanResult.userRating,
                    imageUrl = when (scanResult.genre) {
                        "Drama" -> "https://images.unsplash.com/photo-1509198397868-475647b2a1e5"
                        "Comedia" -> "https://images.unsplash.com/photo-1485846234645-a62644f84728"
                        "Acción y Aventura" -> "https://images.unsplash.com/photo-1536440136628-849c177e76a1"
                        "Ciencia Ficción" -> "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c"
                        else -> "https://images.unsplash.com/photo-1594909122845-11baa439b7bf"
                    },
                    status = "PENDING"
                )
                
                // Save in notification table
                db.pendingMovieDao().insertNotification(notification)
                notification
            } catch (e: Exception) {
                Log.e("MovieRepository", "Web scan failed", e)
                null
            }
        }
    }

    suspend fun approvePendingMovie(notificationId: Int, notification: PendingMovieNotification) {
        withContext(Dispatchers.IO) {
            // Update notification status to approved
            db.pendingMovieDao().updateNotificationStatus(notificationId, "APPROVED")
            
            // Insert into active movies catalog so it displays in searches and grids
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
            db.movieDao().insertMovie(movie)
        }
    }

    suspend fun rejectPendingMovie(notificationId: Int) {
        withContext(Dispatchers.IO) {
            db.pendingMovieDao().updateNotificationStatus(notificationId, "REJECTED")
        }
    }

    suspend fun updateChannel(channel: TVChannelEntity) {
        withContext(Dispatchers.IO) {
            db.tvChannelDao().insertChannel(channel)
        }
    }

    suspend fun updateChannels(channels: List<TVChannelEntity>) {
        withContext(Dispatchers.IO) {
            db.tvChannelDao().insertChannels(channels)
        }
    }

    // --- AI Chat Recommendation Feature ---
    suspend fun getAiMovieRecommendations(userPrompt: String): String {
        return withContext(Dispatchers.IO) {
            GeminiClient.askGemini(userPrompt)
        }
    }
}
