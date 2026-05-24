package com.example

// Canal de streaming y CineStream Applet MainActivity

import android.content.Context
import androidx.compose.foundation.BorderStroke
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.MovieEntity
import com.example.data.PendingMovieNotification
import com.example.data.TVChannelEntity
import com.example.data.UserEntity
import com.example.ui.MovieViewModel
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.NewReleases

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: MovieViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("cine") }

    // State collections
    val filteredMovies by viewModel.filteredMovies.collectAsState()
    val allChannels by viewModel.allChannels.collectAsState(initial = emptyList())
    val pendingNotifications by viewModel.pendingNotifications.collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedMovie by viewModel.selectedMovie.collectAsState()
    val isPlayingTV by viewModel.isPlayingTV.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val scanNotificationAlert by viewModel.scanNotificationAlert.collectAsState()

    // Filter properties
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedQuality by viewModel.selectedQuality.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isPlayingTV) {
                NavigationBar(
                    containerColor = Color(0xFF0F0F16),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    val items = listOf(
                        Triple("cine", "Cine", Icons.Default.Movie),
                        Triple("tv", "TV en Vivo", Icons.Default.LiveTv),
                        Triple("ai", "Sugerencias AI", Icons.Default.AutoAwesome),
                        Triple("moderacion", "Moderación", Icons.Default.ScreenSearchDesktop),
                        Triple("perfil", "Mi Perfil", Icons.Default.AccountCircle)
                    )

                    items.forEach { (tab, label, icon) ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Box {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (tab == "moderacion" && pendingNotifications.isNotEmpty()) {
                                        // Badge for pending notifications
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = (-4).dp)
                                                .size(16.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = pendingNotifications.size.toString(),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B0B0F))
                .padding(innerPadding)
        ) {
            // Main Screen content switcher
            when (currentTab) {
                "cine" -> CineTabContent(
                    movies = filteredMovies,
                    searchQuery = searchQuery,
                    selectedGenre = selectedGenre,
                    selectedYear = selectedYear,
                    selectedQuality = selectedQuality,
                    currentUser = currentUser,
                    viewModel = viewModel
                )
                "tv" -> TVTabContent(
                    channels = allChannels,
                    currentUser = currentUser,
                    viewModel = viewModel
                )
                "ai" -> AISuggestionsContent(
                    currentUser = currentUser,
                    viewModel = viewModel
                )
                "moderacion" -> ModerationContent(
                    notifications = pendingNotifications,
                    viewModel = viewModel
                )
                "perfil" -> ProfileContent(
                    currentUser = currentUser,
                    viewModel = viewModel
                )
            }

            // Global Web Discovery Toast-Banner Alert
            scanNotificationAlert?.let { alertMessage ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(bottom = 8.dp)
                        .statusBarsPadding()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .align(Alignment.Center)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerta",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notificación de CineStream",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = alertMessage,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(onClick = { viewModel.clearScanAlert() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // In-App New Movie Alert Banner
            val newMovieNotification by viewModel.newMovieNotification.collectAsState()
            newMovieNotification?.let { movie ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 90.dp)
                        .statusBarsPadding()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2843)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .align(Alignment.Center)
                            .clickable {
                                viewModel.selectMovie(movie)
                                viewModel.clearNewMovieNotification()
                            }
                            .border(1.dp, Color(0xFF00D2FF), RoundedCornerShape(12.dp)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NewReleases,
                                contentDescription = "Nuevo Lanzamiento",
                                tint = Color(0xFF00D2FF),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¡NUEVA PELÍCULA AGREGADA AL CATÁLOGO!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF00D2FF)
                                )
                                Text(
                                    text = "${movie.title} (${movie.year}) ya se puede reproducir.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Género: ${movie.genre} • Toca para ver detalles",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                            IconButton(onClick = { viewModel.clearNewMovieNotification() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // TV Live Screen Overlay Player
            AnimatedVisibility(
                visible = isPlayingTV && selectedChannel != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut()
            ) {
                selectedChannel?.let { channel ->
                    LiveTVPlayerScreen(channel = channel, viewModel = viewModel)
                }
            }

            // Movie Detail Bottom Sheet Overlay Dialog
            selectedMovie?.let { movie ->
                MovieDetailDialog(
                    movie = movie,
                    currentUser = currentUser,
                    onDismiss = { viewModel.selectMovie(null) },
                    viewModel = viewModel
                )
            }
        }
    }
}

// ---------------- CINE TAB (GRID & ADVANCED FILTERS) ----------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CineTabContent(
    movies: List<MovieEntity>,
    searchQuery: String,
    selectedGenre: String,
    selectedYear: String,
    selectedQuality: String,
    currentUser: UserEntity?,
    viewModel: MovieViewModel
) {
    val context = LocalContext.current
    val isAdultUnlocked by viewModel.isAdultUnlocked.collectAsState()
    val genres = remember(isAdultUnlocked) {
        if (isAdultUnlocked) {
            listOf("Todos", "Acción y Aventura", "Comedia", "Drama", "Ciencia Ficción", "Gore y Terror", "Adultos +18")
        } else {
            listOf("Todos", "Acción y Aventura", "Comedia", "Drama", "Ciencia Ficción", "Gore y Terror")
        }
    }
    val qualities = listOf("Todos", "4K", "1080p", "720p")
    val years = listOf("Todos", "2024", "2023", "2022", "Década 2010", "Más Antiguas")

    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App header & Monetization Premium offer banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CineStream Discover",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Canales gratis, locales y en la red",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier
                    .background(Color(0xFF161622), CircleShape)
                    .testTag("toggle_filters_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (showFilters || selectedGenre != "Todos" || selectedYear != "Todos" || selectedQuality != "Todos") MaterialTheme.colorScheme.secondary else Color.LightGray
                )
            }
        }

        // Sponsored Ad / Monetization Demo Banner (For Free accounts only)
        if (currentUser?.isPremium != true) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1400)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.togglePremiumSubscription() }
                    .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFB300), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text("SPONSOR", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mira sin anuncios e integra Full 4K. Toca para ser VIP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "VIP",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Busca por título, plataforma (ej: Pluto) o categoría...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF16161F),
                unfocusedContainerColor = Color(0xFF16161F),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF2C2C35)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("movie_search_input"),
            shape = RoundedCornerShape(24.dp)
        )

        // Dropdown Expanded Filters
        AnimatedVisibility(visible = showFilters) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("GÉNERO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        genres.forEach { genre ->
                            val isSelected = selectedGenre == genre
                            InputChip(
                                selected = isSelected,
                                onClick = { viewModel.updateGenreFilter(genre) },
                                label = { Text(genre, fontSize = 11.sp) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E1E28),
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("CALIDAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        qualities.forEach { quality ->
                            val isSelected = selectedQuality == quality
                            InputChip(
                                selected = isSelected,
                                onClick = {
                                    if (quality == "4K" && currentUser?.isPremium != true) {
                                        Toast.makeText(context, "El filtro 4K requiere suscripción VIP de CineStream", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateQualityFilter(quality)
                                    }
                                },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(quality, fontSize = 11.sp)
                                        if (quality == "4K" && currentUser?.isPremium != true) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(Icons.Default.Lock, "Premium", tint = Color(0xFFFFA000), modifier = Modifier.size(10.dp))
                                        }
                                    }
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E1E28),
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("AÑO DE ESTRENO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        years.forEach { year ->
                            val isSelected = selectedYear == year
                            InputChip(
                                selected = isSelected,
                                onClick = { viewModel.updateYearFilter(year) },
                                label = { Text(year, fontSize = 11.sp) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E1E28),
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active filters summary
        if (selectedGenre != "Todos" || selectedYear != "Todos" || selectedQuality != "Todos") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros activos: $selectedGenre • $selectedQuality • $selectedYear",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        viewModel.updateGenreFilter("Todos")
                        viewModel.updateQualityFilter("Todos")
                        viewModel.updateYearFilter("Todos")
                    },
                    modifier = Modifier.height(24.dp).wrapContentHeight()
                ) {
                    Text("Limpiar", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Grid Display
        if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "vacio",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No se encontraron películas en la red",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Intenta buscar en otra plataforma o elimina filtros activos.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val recomlist by viewModel.smartRecommendations.collectAsState()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (recomlist.isNotEmpty() && searchQuery.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Personalizado",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "TE RECOMENDAMOS (IA & RECIENTES)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0F253F), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Smart Engine", fontSize = 8.sp, color = Color(0xFF00D2FF), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().height(160.dp)
                            ) {
                                items(recomlist) { recomMovie ->
                                    RecommendationMovieCard(recomMovie, onClick = { viewModel.selectMovie(recomMovie) })
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                items(movies) { movie ->
                    MovieGridCard(movie = movie, onClick = { viewModel.selectMovie(movie) })
                }
            }
        }
    }
}

@Composable
fun RecommendationMovieCard(movie: MovieEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.imageUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = null
            )
            // Tint and title bottom overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )
            
            // Match indicator or critical score
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(Color(0xFF00C853), RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "${90 + (movie.criticRating.toInt() % 10)}% Match",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomCenter),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MovieGridCard(movie: MovieEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("movie_card_${movie.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Movie Poster / Backdrop Simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            ) {
                // Background Gradient visual backup
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF222230), Color(0xFF121218))
                            )
                        )
                )

                // Optional Async Image loading
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = null // fallbacks to gradient design cleanly
                )

                // HD/4K Quality Badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            if (movie.quality == "4K") Color(0xFFFFB300) else Color.DarkGray.copy(alpha = 0.8f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = movie.quality,
                        color = if (movie.quality == "4K") Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Streaming / Local Source label bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(8.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = movie.platforms.split(",").firstOrNull() ?: "Web",
                        color = Color(0xFF00D2FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Ratings & Metadata
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = movie.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movie.year.toString(),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = movie.genre.split(" ").firstOrNull() ?: movie.genre,
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dual Ratings: Critics & Users (MANDATORY REQUIREMENT)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0F16), RoundedCornerShape(6.dp))
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Critic index
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Críticos",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${movie.criticRating} C",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Divider segment
                    Box(modifier = Modifier.width(1.dp).height(10.dp).background(Color.Gray.copy(alpha = 0.4f)))

                    // User index
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Usuarios",
                            tint = Color(0xFF00D2FF),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${movie.userRating} U",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ---------------- MOVIE DETAILS DIALOG ----------------

@Composable
fun MovieDetailDialog(
    movie: MovieEntity,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    viewModel: MovieViewModel
) {
    val context = LocalContext.current
    val watchlist by viewModel.userWatchlist.collectAsState()
    val isSaved = watchlist.any { it.id == movie.id }

    var adPlayedByPremiumSimulation by remember { mutableStateOf(false) }

    // Interstitial Ad simulation for non-premium accounts
    if (currentUser?.isPremium != true && !adPlayedByPremiumSimulation) {
        // Overlay ad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { /* Block clicks */ }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFB300), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("PUBLICIDAD PATROCINADA", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Icon(Icons.Default.Devices, "CineStream Ad", modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dile adiós a los anuncios y disfruta de transmisión VIP ilimitada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Suscripción CineStream VIP por solo $4.99 USD/mes.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = {
                        viewModel.togglePremiumSubscription()
                        adPlayedByPremiumSimulation = true
                        Toast.makeText(context, "Suscripción Simula Realizada Exitosamente!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, "VIP", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adquirir Membresía VIP", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { adPlayedByPremiumSimulation = true }) {
                    Text("Cerrar anuncio e ir al Cine", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    } else {
        // Movie Information Sheet
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onDismiss)
                .statusBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* block dismiss clicks inside the card */ }
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = movie.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                        }
                    }

                    // Metadata Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = movie.year.toString(),
                            fontSize = 13.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "•", color = Color.Gray)
                        Text(
                            text = movie.genre,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(text = "•", color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .background(Color.DarkGray, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(movie.quality, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Platforms Watch-links (MANDATORY REQUIREMENT)
                    Text(
                        text = "DISPONIBLE EN LA RED EN:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        movie.platforms.split(",").forEach { platform ->
                            val clean = platform.trim()
                            val isPH = clean.contains("PornHub", ignoreCase = true)
                            val isXV = clean.contains("XVideos", ignoreCase = true)
                            val isXN = clean.contains("XNXX", ignoreCase = true)
                            val isPB = clean.contains("Playboy", ignoreCase = true)

                            val (borderColor, iconColor) = when {
                                isPH -> Color(0xFFFF9900) to Color(0xFFFF9900)
                                isXV -> Color(0xFFFF3333) to Color(0xFFFF3333)
                                isXN -> Color(0xFFFFD700) to Color(0xFFFFD700)
                                isPB -> Color(0xFFFF4081) to Color(0xFFFF4081)
                                else -> Color(0xFF00D2FF) to Color(0xFF00D2FF)
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF0F0F1A), RoundedCornerShape(8.dp))
                                    .border(1.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LiveTv, "Link", tint = iconColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(clean, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description text
                    Text(
                        text = "SINOPSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = movie.description,
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Ratings layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0F16), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CRÍTICOS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, "Critics", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${movie.criticRating} / 5.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Divider
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.DarkGray))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("USUARIOS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, "Users", tint = Color(0xFF00D2FF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${movie.userRating} / 5.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.toggleWatchlist(movie)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaved) Color.DarkGray else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("toggle_watchlist_details_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = "Watchlist"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSaved) "En mi Watchlist" else "Agregar a Watchlist",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.recordHistory(movie.id)
                                Toast.makeText(context, "Reproduciendo ${movie.title} - Agregado a tu Historial de Visualización", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Ver Ahora", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic User Movie Rating Section
                    Text(
                        text = "TU CALIFICACIÓN (Influye en recomendaciones):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val userRatings by viewModel.userRatings.collectAsState()
                        val currentMovieRating = userRatings.find { it.movieId == movie.id }?.rating ?: 0f
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = {
                                    viewModel.submitRating(movie.id, star.toFloat())
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $star",
                                    tint = if (star <= currentMovieRating) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // VOD Affiliate Association
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D253F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Redirigiendo a Alquiler en Prime Video (Comisión por Referido CineStream)", Toast.LENGTH_LONG).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, "VOD Promo", tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("ALQUILAR COMPLEMENTO PREMIUM (VOD)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Disponible en UHD por $2.99 en Prime Video. Recibe reembolso de 15%", color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ---------------- TELEVISIÓN TAB (PLUTO TV / LIVE CHANNELS) ----------------

@Composable
fun TVTabContent(
    channels: List<TVChannelEntity>,
    currentUser: UserEntity?,
    viewModel: MovieViewModel
) {
    val context = LocalContext.current
    val isAdultUnlocked by viewModel.isAdultUnlocked.collectAsState()
    val categories = remember(isAdultUnlocked) {
        if (isAdultUnlocked) {
            listOf("Todos", "Cine", "Comedia", "Deportes", "Noticias", "Adultos (*)")
        } else {
            listOf("Todos", "Cine", "Comedia", "Deportes", "Noticias")
        }
    }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedCatalog by remember { mutableStateOf("Gratis") } // "Gratis" or "Premium"

    val filteredChannels = remember(channels, selectedCategory, selectedCatalog) {
        val baseList = if (selectedCatalog == "Gratis") {
            channels.filter { !it.isPaid }
        } else {
            channels.filter { it.isPaid }
        }
        
        if (selectedCategory == "Todos") baseList
        else baseList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val isHealingLinks by viewModel.isHealingLinks.collectAsState()
    val healingReport by viewModel.healingReport.collectAsState()
    val healingSteps by viewModel.healingSteps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TV en Vivo en Español",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Streaming gratuito (tipo Pluto TV) y Canales Premium",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            
            Button(
                onClick = { viewModel.autoHealBrokenLinks() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHealingLinks) Color(0xFFFF9900) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isHealingLinks) "Sanando..." else "Auto-Sanar ⚡",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (healingReport != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF1F2F4E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isHealingLinks) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "OK",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IPTV Link Healer Auto-Diagnóstico CLI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.clearHealingReport() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        healingSteps.forEach { step ->
                            Text(
                                text = step,
                                color = if (step.startsWith("⚠️")) Color(0xFFFF9900) else if (step.startsWith("✅") || step.startsWith("🎉")) Color(0xFF00E676) else Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = healingReport ?: "",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented catalog controller - MagisTV Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF12121A), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedCatalog == "Gratis") MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { selectedCatalog = "Gratis" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = "Gratis",
                        tint = if (selectedCatalog == "Gratis") Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🆓 SEÑAL GRATUITA",
                        color = if (selectedCatalog == "Gratis") Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedCatalog == "Premium") Color(0xFFFFB300) else Color.Transparent)
                    .clickable { selectedCatalog = "Premium" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Premium",
                        tint = if (selectedCatalog == "Premium") Color.Black else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "💎 PREMIUM VIP",
                        color = if (selectedCatalog == "Premium") Color.Black else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ad Banner or Premium callout row for non-Premium users exploring Premium TV catalog
        if (selectedCatalog == "Premium" && currentUser?.isPremium != true) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1F00)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.togglePremiumSubscription()
                        Toast.makeText(context, "Suscripción VIP Activada! Disfruta de todos los canales.", Toast.LENGTH_SHORT).show()
                    }
                    .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LockOpen, "Lock", tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DESBLOQUEAR TODOS LOS CANALES VIP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("Acceso ilimitado a señales de paga exclusivas. Toca para suscribirte.", color = Color.LightGray, fontSize = 10.sp)
                    }
                    Text("ACTIVAR", color = Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else if (currentUser?.isPremium != true) {
            // Free users watching free channels see non-obtrusive dynamic ads (commissions / advertising model!)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "Oferta Especial Cine_Referido: Obtén 3 meses de HBO Max con 50% de descuento!", Toast.LENGTH_LONG).show()
                    }
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF5252), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("AD", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Patrocinado: Promociones exclusivas de streaming en Prime Video y Pluto TV.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go", tint = Color.Gray, modifier = Modifier.size(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Categories Chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                InputChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 11.sp) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF161622),
                        labelColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay canales disponibles en esta categoría.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredChannels) { channel ->
                    TVChannelCard(
                        channel = channel,
                        currentUser = currentUser,
                        onPlay = {
                            if (channel.isPaid && currentUser?.isPremium != true) {
                                // Block and ask to unlock
                                Toast.makeText(context, "El canal ${channel.name} requiere suscripción Premium VIP.", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.playTVChannel(channel)
                            }
                        },
                        onUpgrade = {
                            viewModel.togglePremiumSubscription()
                            Toast.makeText(context, "Suscripción Premium VIP Simulada!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TVChannelCard(
    channel: TVChannelEntity,
    currentUser: UserEntity?,
    onPlay: () -> Unit,
    onUpgrade: () -> Unit
) {
    val isLocked = channel.isPaid && currentUser?.isPremium != true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tv_channel_${channel.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFF1E1616) else Color(0xFF161622)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLocked) Color(0xFFFF5252).copy(alpha = 0.4f) else Color.DarkGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated Channel Logo Placeholder/Async Image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF232330)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = null
                )
                // Symbol Overlay
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LiveTv,
                    contentDescription = "Logo",
                    tint = if (isLocked) Color(0xFFFF5252) else Color(0xFF00D2FF).copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Channel description
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0B0B0F), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(channel.category, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
                
                Text(
                    text = channel.fakeStreamDescription,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isLocked) Color.Gray else Color.Red, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLocked) "Canal de Pago (VIP)" else "${channel.viewerCount} viendo en vivo",
                        fontSize = 10.sp,
                        color = if (isLocked) Color(0xFFFF5252) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Play command
            if (isLocked) {
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, "Lock", tint = Color.Black, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VIP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Ver",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ---------------- LIVE TV LAYERED OVERLAY PLAYER & CHAT ----------------

@Composable
fun LiveTVPlayerScreen(channel: TVChannelEntity, viewModel: MovieViewModel) {
    val chatEntries by viewModel.fakeTVChat.collectAsState()
    val context = LocalContext.current

    val activeMirrorIndex = remember { mutableStateOf(1) }
    val isStreamBroken = remember { mutableStateOf(false) }
    val countdownSeconds = remember { mutableStateOf(3) }
    val channels by viewModel.allChannels.collectAsState(initial = emptyList())

    // Auto failover loop
    LaunchedEffect(isStreamBroken.value) {
        if (isStreamBroken.value) {
            countdownSeconds.value = 3
            while (countdownSeconds.value > 0) {
                delay(1000)
                countdownSeconds.value -= 1
            }
            // Failover to next channel
            val currentIndex = channels.indexOfFirst { it.id == channel.id }
            if (currentIndex != -1 && channels.isNotEmpty()) {
                val nextIndex = (currentIndex + 1) % channels.size
                val nextChannel = channels[nextIndex]
                viewModel.playTVChannel(nextChannel)
                Toast.makeText(context, "Sección Caída: Reanudando en canal: ${nextChannel.name}", Toast.LENGTH_LONG).show()
            }
            activeMirrorIndex.value = 1
            isStreamBroken.value = false
        }
    }

    LaunchedEffect(channel.id) {
        activeMirrorIndex.value = 1
        isStreamBroken.value = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07070A))
            .clickable { /* Block actions */ }
            .navigationBarsPadding()
    ) {
        // Simulated Video Canvas Back
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.77f)
                .background(Color.Black)
        ) {
            if (isStreamBroken.value) {
                // All mirrors failed count down screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF261010)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Buscando backup",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "¡CONEXIÓN ENLACE FALLIDA! (503 ERROR)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                        Text(
                            text = "Error de reproducción en ${channel.name}.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Auto-Invocando conmutador al SIGUIENTE CANAL en ${countdownSeconds.value}...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val currentIndex = channels.indexOfFirst { it.id == channel.id }
                                if (currentIndex != -1 && channels.isNotEmpty()) {
                                    val nextIndex = (currentIndex + 1) % channels.size
                                    viewModel.playTVChannel(channels[nextIndex])
                                }
                                activeMirrorIndex.value = 1
                                isStreamBroken.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SALTAR CANAL AHORA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                // Background pulsing stream mockup animation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = when (activeMirrorIndex.value) {
                                    1 -> listOf(Color(0xFF1E3C72).copy(alpha = 0.5f), Color.Black)
                                    2 -> listOf(Color(0xFF2C3E50).copy(alpha = 0.5f), Color.Black)
                                    else -> listOf(Color(0xFF0F2027).copy(alpha = 0.5f), Color.Black)
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = when (activeMirrorIndex.value) {
                                1 -> MaterialTheme.colorScheme.primary
                                2 -> Color(0xFFFF9900)
                                else -> Color(0xFF00E676)
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (activeMirrorIndex.value) {
                                1 -> "Conectando al Mirror 1 (Principal)..."
                                2 -> "Cambiando a Mirror 2 de Respaldo HD..."
                                else -> "Cargando Mirror de Emergencia 3 (Modo Seguro)..."
                            },
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Señal Enlace #${activeMirrorIndex.value} • Reproducción Dinámica",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Foreground controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.closeTVPlayer() }) {
                    Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("EN VIVO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(channel.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                IconButton(onClick = {
                    // Force break this stream to test failover
                    if (activeMirrorIndex.value < 3) {
                        activeMirrorIndex.value += 1
                        Toast.makeText(context, "⚠️ Link Caído. Conmutando a Mirror ${activeMirrorIndex.value}...", Toast.LENGTH_SHORT).show()
                    } else {
                        isStreamBroken.value = true
                        activeMirrorIndex.value = 4
                        Toast.makeText(context, "❌ Todos los mirrors caídos. Iniciando conmutación de canal...", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Warning, "Simular Error", tint = Color(0xFFFF5252))
                }
            }

            // Bottom Player controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Pause, "Play/Pausa", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                LinearProgressIndicator(
                    progress = { 0.85f },
                    color = Color.Red,
                    trackColor = Color.DarkGray,
                    modifier = Modifier.weight(1f).height(4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Volumen", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Fullscreen, "Pantalla Completa", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        val channels by viewModel.allChannels.collectAsState(initial = emptyList())
        val currentUser by viewModel.currentUser.collectAsState()

        // Easy Channel Switching strip (MagisTV Style)
        Text(
            text = "CAMBIO RÁPIDO DE CANAL (Estilo MagisTV)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C0C12))
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(channels) { ch ->
                val isSelected = ch.id == channel.id
                val isLocked = ch.isPaid && currentUser?.isPremium != true
                Card(
                    modifier = Modifier
                        .width(135.dp)
                        .height(55.dp)
                        .clickable {
                            if (isLocked) {
                                Toast.makeText(context, "El canal ${ch.name} requiere VIP. Desbloquéalo en la pestaña Perfil.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.playTVChannel(ch)
                            }
                        },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E1E28)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFF00D2FF) else if (isLocked) Color.Red.copy(alpha = 0.4f) else Color.DarkGray
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = if (isLocked) Color(0xFFFF5252) else if (isSelected) Color.White else Color(0xFF00D2FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ch.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Live Chat Feed Panel (Pluto TV stream chat simulation)
        Text(
            text = "CHAT EN VIVO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0F0F14))
                .padding(horizontal = 16.dp)
        ) {
            items(chatEntries) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${entry.sender}: ",
                        color = if (entry.isSystem) MaterialTheme.colorScheme.primary else Color(0xFF00D2FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = entry.message,
                        color = if (entry.isSystem) Color.LightGray else Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Send simulated message bar
        var localMessage by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16161F))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = localMessage,
                onValueChange = { localMessage = it },
                placeholder = { Text("Escribe algo en el chat en directo...", fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0B0B0F),
                    unfocusedContainerColor = Color(0xFF0B0B0F)
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    if (localMessage.isNotBlank()) {
                        Toast.makeText(context, "Mensaje enviado al chat directo!", Toast.LENGTH_SHORT).show()
                        localMessage = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Enviar", fontSize = 12.sp)
            }
        }
    }
}

// ---------------- AI SUGGESTIONS (GEMINI MOVIE EXPLORER) ----------------

@Composable
fun AISuggestionsContent(
    currentUser: UserEntity?,
    viewModel: MovieViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputQuery by remember { mutableStateOf("") }
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()

    val suggestions = listOf(
        "¿Qué películas gratuitas me recomiendas para el fin de semana?",
        "Busco un drama galardonado que esté disponible en Pluto TV o RTVE Play",
        "Recomiéndame comedias del 2024 con calificación alta en la red",
        "Sugiéreme terror local disponible gratis sin suscripciones"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recomendador CineStream AI",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Conversa con el cerebro de CineStream para descubrir qué ver en la red.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large output response scroll area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF161622), RoundedCornerShape(12.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            if (aiLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Gemini analizando películas en la red...", color = Color.Gray, fontSize = 13.sp)
                }
            } else if (aiRecommendation != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, "Gemini", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sugerencias Personalizadas", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = aiRecommendation ?: "",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AutoAwesome, "AI", modifier = Modifier.size(56.dp), tint = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Pregúntale a la Inteligencia Artificial",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Escribe tus gustos abajo o selecciona un ejemplo de búsqueda rápida.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Suggestion Quick Chips
        Text("TEMAS DE CONVERSACIÓN SUGERIDOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier.height(100.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(suggestions) { suggestPrompt ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F16)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            inputQuery = suggestPrompt
                            keyboardController?.hide()
                            viewModel.getAiRecommendations(suggestPrompt)
                        },
                    border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, "Prompt", tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(suggestPrompt, fontSize = 11.sp, color = Color.LightGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Query input box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Quiero películas de...", fontSize = 13.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF16161F),
                    unfocusedContainerColor = Color(0xFF16161F)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (inputQuery.isNotBlank()) {
                        keyboardController?.hide()
                        viewModel.getAiRecommendations(inputQuery)
                    }
                }),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        keyboardController?.hide()
                        viewModel.getAiRecommendations(inputQuery)
                    }
                },
                enabled = inputQuery.isNotBlank() && !aiLoading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Buscar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------- BUDGET & WEB SCANNING MODERATION CONTENT ----------------

@Composable
fun ModerationContent(
    notifications: List<PendingMovieNotification>,
    viewModel: MovieViewModel
) {
    val isScanning by viewModel.isScanning.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Centro de Moderación y Escaneo",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Escanea la red por nuevas películas. Recibe alertas y apruébalas antes de agregarlas al catálogo.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced AI scan trigger
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("scan_trigger_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Devices, "Scan", tint = Color(0xFF00D2FF), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Detector de Estrenos en la Red",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Busca de forma proactiva utilizando el analizador de Gemini para indexar películas libres o de streaming.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isScanning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analizando fuentes en la red...", color = Color(0xFF00D2FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.scanWebForNewMovies() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("trigger_scan_now_button")
                    ) {
                        Text("Escanear la Red Ahora", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notifications Inbox label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ALERTA DE NUEVAS PELÍCULAS EN LA RED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF222230), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("${notifications.size} pendientes", fontSize = 10.sp, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF161622), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, "Vacío", modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay películas pendientes", fontWeight = FontWeight.Bold, color = Color.LightGray, fontSize = 14.sp)
                    Text("Toca en 'Escanear la Red' para encontrar nuevas cintas.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications) { item ->
                    PendingFilmCard(item = item, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PendingFilmCard(item: PendingMovieNotification, viewModel: MovieViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${item.year} • ${item.genre} • ${item.quality}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // AI alert source icon
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1B0033), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("AI FIND", color = Color(0xFFD0BCFF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fuentes encontradas: ${item.platforms}",
                fontSize = 11.sp,
                color = Color(0xFF00D2FF),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = Color.LightGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action triggers (Aprobar / rechazar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.rejectNotification(item.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, "reject", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { viewModel.approveNotification(item) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, "approve", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar y Agregar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------- USER PROFILE TAB (WATCHLIST & USER ACTIONS) ----------------

@Composable
fun ProfileContent(
    currentUser: UserEntity?,
    viewModel: MovieViewModel
) {
    var isSigningUp by remember { mutableStateOf(false) }
    var inputUser by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }

    val savedMovies by viewModel.userWatchlist.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Mi Perfil CineStream",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Accede a características avanzadas, control parental y monitor de red latino.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile State Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (currentUser != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.displayName.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Usuario: @${currentUser.userId}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        // VIP Plan status Tag (Monetization display)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (currentUser.isPremium) Color(0xFFFFB300) else Color.DarkGray,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (currentUser.isPremium) "VIP PREMIERE" else "GRATUITO",
                                color = if (currentUser.isPremium) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Monetization Button: Subscribe / Downgrade for testing
                    Button(
                        onClick = {
                            viewModel.togglePremiumSubscription()
                            val nowSubscribed = !currentUser.isPremium
                            if (nowSubscribed) {
                                Toast.makeText(context, "¡Felicitaciones! Ahora tienes acceso 4K y Canales VIP Premium.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Suscripción cancelada. Volviendo al modo gratuito.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentUser.isPremium) Color.DarkGray else Color(0xFFFFB300)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentUser.isPremium) Icons.Default.Info else Icons.Default.Lock,
                                contentDescription = "VIP",
                                tint = if (currentUser.isPremium) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentUser.isPremium) "Administrar Premium VIP (Simulado)" else "Suscribirse por $4.99/mes",
                                color = if (currentUser.isPremium) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Control Parental widget (+18 content mode)
                    val isAdultUnlocked by viewModel.isAdultUnlocked.collectAsState()
                    var showPinDialog by remember { mutableStateOf(false) }
                    var enteredPin by remember { mutableStateOf("") }
                    var pinError by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAdultUnlocked) Color(0xFF1E1313) else Color(0xFF111E11)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(
                            1.dp,
                            if (isAdultUnlocked) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFF00C853).copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isAdultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = "Lock",
                                        tint = if (isAdultUnlocked) Color(0xFFFF5252) else Color(0xFF00C853),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "MODO ADULTO (+18)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (isAdultUnlocked) "Desbloqueado (XVIDEOS, PORNHUB, PLAYBOY, XNXX)" else "Bloqueado por Control Parental",
                                            fontSize = 9.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                Switch(
                                    checked = isAdultUnlocked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            enteredPin = ""
                                            pinError = false
                                            showPinDialog = true
                                        } else {
                                            viewModel.lockAdultChannels()
                                            Toast.makeText(context, "Control Parental Activado. Contenido oculto.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFFF5252),
                                        checkedTrackColor = Color(0xFFFF5252).copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color.DarkGray
                                    )
                                )
                            }
                        }
                    }

                    if (showPinDialog) {
                        AlertDialog(
                            onDismissRequest = { showPinDialog = false },
                            containerColor = Color(0xFF161622),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, "PIN", tint = Color(0xFFFFB300))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Control Parental +18", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            text = {
                                Column {
                                    Text(
                                        text = "Para activar el canal y catálogo de adultos con películas de XVideos, XNXX, PornHub y Playboy (duración superior a 30 mins), ingresa el PIN de seguridad (Pista: usa el pin 0000 o 1818).",
                                        fontSize = 12.sp,
                                        color = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedTextField(
                                        value = enteredPin,
                                        onValueChange = {
                                            enteredPin = it
                                            pinError = false
                                        },
                                        placeholder = { Text("Introduce el PIN de 4 dígitos", fontSize = 12.sp) },
                                        singleLine = true,
                                        isError = pinError,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = Color(0xFF0F0F1A),
                                            unfocusedContainerColor = Color(0xFF0F0F1A)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (pinError) {
                                        Text(
                                            text = "PIN Incorrecto. Ingrese 0000 o 1818.",
                                            color = Color(0xFFFF5252),
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val success = viewModel.unlockAdultChannels(enteredPin)
                                        if (success) {
                                            showPinDialog = false
                                            Toast.makeText(context, "Modo Adulto Desbloqueado. Canales listados.", Toast.LENGTH_LONG).show()
                                        } else {
                                            pinError = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                                ) {
                                    Text("DESBLOQUEAR", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPinDialog = false }) {
                                    Text("CANCELAR", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Logout trigger
                    TextButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cerrar Sesión", color = Color.Gray, fontSize = 13.sp)
                    }

                } else {
                    // TABBED LOGIN / REGISTRATION WITH EMAIL AND GOOGLE SIGN IN
                    var loginTab by remember { mutableStateOf("email") }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0F16), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (loginTab == "email") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { loginTab = "email" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Correo Electrónico", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (loginTab == "google") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { loginTab = "google" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Google Sign-In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (loginTab == "email") {
                        Text("REGISTRO POR CORREO ELECTRÓNICO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        var inputEmail by remember { mutableStateOf("") }
                        var inputEmailName by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            placeholder = { Text("ejemplo@correo.com", fontSize = 13.sp) },
                            label = { Text("Correo Electrónico") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF101015),
                                unfocusedContainerColor = Color(0xFF101015)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputEmailName,
                            onValueChange = { inputEmailName = it },
                            placeholder = { Text("Escribe tu nombre completo...", fontSize = 13.sp) },
                            label = { Text("Nombre Completo") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF101015),
                                unfocusedContainerColor = Color(0xFF101015)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (inputEmail.isNotBlank() && inputEmailName.isNotBlank() && inputEmail.contains("@")) {
                                    val alias = inputEmail.substringBefore("@")
                                    viewModel.registerOrLogin(alias.trim(), inputEmailName.trim())
                                    inputEmail = ""
                                    inputEmailName = ""
                                    Toast.makeText(context, "¡Sesión iniciada con éxito!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Por favor completa el correo válido y tu nombre.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar Sesión / Registrarse", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Google One-Tap Premium", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Vincula tu Smart TV utilizando tu celular y cuenta de Google.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            var showGooglePickDialog by remember { mutableStateOf(false) }
                            
                            Button(
                                onClick = { showGooglePickDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continuar con Google", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            
                            if (showGooglePickDialog) {
                                AlertDialog(
                                    onDismissRequest = { showGooglePickDialog = false },
                                    containerColor = Color(0xFF10101B),
                                    title = {
                                        Text("Iniciar sesión con Google", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                    },
                                    text = {
                                        Column {
                                            Text("Elige una cuenta de Google para registrarte en CineStream:", fontSize = 11.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF1C1C29), RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.registerOrLogin("hernandez.101892", "Hernandez CineStreamer")
                                                        showGooglePickDialog = false
                                                        Toast.makeText(context, "Google Sign-In Exitoso", Toast.LENGTH_LONG).show()
                                                    }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(Color(0xFF4285F4), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("H", color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("Hernandez CineStreamer", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("hernandez.101892@gmail.com", color = Color.Gray, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showGooglePickDialog = false }) {
                                            Text("Cerrar", color = Color.Gray)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // REAL-TIME CHARTS AND TRAFFIC GRAPHS CARD
        ChartSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Watchlist header (MANDATORY REQUIREMENT)
        Text(
            text = "MI LISTA DE SEGUIMIENTO (WATCHLIST)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF161622), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Por favor regístrate para ver tu Watchlist.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        } else if (savedMovies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF161622), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Movie, "empty", modifier = Modifier.size(36.dp), tint = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu lista de seguimiento está vacía.",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Agrega películas desde la cartelera principal.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                savedMovies.forEach { movie ->
                    WatchlistRow(
                        movie = movie,
                        onRemove = { viewModel.toggleWatchlist(movie) },
                        onClick = { viewModel.selectMovie(movie) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChartSection(viewModel: MovieViewModel) {
    // Live fluctuations for simulated online watchers
    val connectedByNetwork = remember { mutableStateOf(1442) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(kotlin.random.Random.nextLong(2000, 4500))
            connectedByNetwork.value += kotlin.random.Random.nextInt(-6, 7)
        }
    }

    val totalUsersDb by viewModel.totalDbUsers.collectAsState(initial = 1)
    val totalAccountsGlobal = remember(totalUsersDb) { 8740 + totalUsersDb }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11111E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222238), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 TRÁFICO Y CONTROL DE RED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00D2FF),
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0D251D), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Glowing pulsing bullet
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF00E676), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ONLINE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Connected counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("USUARIOS CONECTADOS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("${connectedByNetwork.value}", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Black)
                    Text("Canal IPTV + VOD Activo", fontSize = 9.sp, color = Color(0xFF00E676))
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.DarkGray))
                Column(modifier = Modifier.weight(1f)) {
                    Text("CUENTAS CREADAS", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("$totalAccountsGlobal", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Black)
                    Text("Sincronización Cloud", fontSize = 9.sp, color = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart Title 1
            Text(
                text = "CINE MAS VISTO (VOD - Reproducciones Semanales)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Bars for Movies list
            val moviesStat = listOf(
                Triple("La Sociedad de la Nieve", "94K vistas", 0.95f),
                Triple("The Substance (Gore)", "71K vistas", 0.72f),
                Triple("Dune: Part Two", "62K vistas", 0.63f),
                Triple("Playboy Legacy", "45K vistas", 0.46f)
            )

            moviesStat.forEach { (title, subtitle, progress) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFF1E1E2E), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC))
                                    ),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chart Title 2
            Text(
                text = "CANALES IPTV MAS VISTOS (Mando a Distancia / Horas)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Bars for Channels list
            val channelsStat = listOf(
                Triple("Telecinco HD Latino", "154K hrs", 0.92f),
                Triple("Deportes Live HD", "121K hrs", 0.78f),
                Triple("Playboy TV Cinema", "94K hrs", 0.64f),
                Triple("Antena 3 Cine", "71K hrs", 0.48f)
            )

            channelsStat.forEach { (title, subtitle, progress) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFF1E1E2E), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                                    ),
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistRow(movie: MovieEntity, onRemove: () -> Unit, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF2E2E3C)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = null
                )
                Icon(Icons.Default.Movie, "peli", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${movie.year} • ${movie.genre}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    }
}
