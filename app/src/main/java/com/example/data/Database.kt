package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ---------------- ENTITIES ----------------

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val year: Int,
    val genre: String,
    val quality: String, // 4K, 1080p, 720p
    val criticRating: Float, // out of 5.0 or 100
    val userRating: Float, // out of 5.0 or 10.0
    val imageUrl: String,
    val platforms: String, // Comma separated, e.g. "Pluto TV, RTVE Play, Local Stream"
    val description: String,
    val isPremium: Boolean = false
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String, // Username
    val displayName: String,
    val isPremium: Boolean = false // Monetization simulation: VIP subscription
)

@Entity(tableName = "watchlist")
data class WatchlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val movieId: Int
)

@Entity(tableName = "tv_channels")
data class TVChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val logoUrl: String,
    val category: String, // "Cine", "Comedia", "Deportes", "Noticias"
    val isPaid: Boolean, // Paid or Free
    val viewerCount: Int = 120,
    val fakeStreamDescription: String = "Transmisión en directo en Español"
)

@Entity(tableName = "pending_movies")
data class PendingMovieNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val year: Int,
    val genre: String,
    val quality: String,
    val platforms: String,
    val description: String,
    val criticRating: Float,
    val userRating: Float,
    val imageUrl: String,
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val movieId: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "movie_ratings")
data class MovieRatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val movieId: Int,
    val rating: Float,
    val ratedAt: Long = System.currentTimeMillis()
)

// ---------------- DAOS ----------------

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY title ASC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getMoviesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: Int)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    fun getUsersCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isPremium = :isPremium WHERE userId = :userId")
    suspend fun updateUserSubscription(userId: String, isPremium: Boolean)
}

@Dao
interface WatchlistDao {
    @Query("SELECT m.* FROM movies m INNER JOIN watchlist w ON m.id = w.movieId WHERE w.userId = :userId")
    fun getWatchlistForUser(userId: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToWatchlist(item: WatchlistItemEntity)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND movieId = :movieId")
    suspend fun removeFromWatchlist(userId: String, movieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND movieId = :movieId)")
    suspend fun isInWatchlist(userId: String, movieId: Int): Boolean
}

@Dao
interface TVChannelDao {
    @Query("SELECT * FROM tv_channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<TVChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<TVChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: TVChannelEntity)
}

@Dao
interface PendingMovieDao {
    @Query("SELECT * FROM pending_movies WHERE status = 'PENDING' ORDER BY id DESC")
    fun getPendingNotifications(): Flow<List<PendingMovieNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: PendingMovieNotification)

    @Query("UPDATE pending_movies SET status = :status WHERE id = :id")
    suspend fun updateNotificationStatus(id: Int, status: String)
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE userId = :userId ORDER BY watchedAt DESC")
    fun getWatchHistory(userId: String): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE userId = :userId")
    suspend fun clearHistory(userId: String)
}

@Dao
interface MovieRatingDao {
    @Query("SELECT * FROM movie_ratings WHERE userId = :userId")
    fun getRatingsForUser(userId: String): Flow<List<MovieRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: MovieRatingEntity)

    @Query("SELECT * FROM movie_ratings WHERE userId = :userId AND movieId = :movieId LIMIT 1")
    suspend fun getRatingForMovie(userId: String, movieId: Int): MovieRatingEntity?
}

// ---------------- DATABASE ----------------

@Database(
    entities = [
        MovieEntity::class,
        UserEntity::class,
        WatchlistItemEntity::class,
        TVChannelEntity::class,
        PendingMovieNotification::class,
        WatchHistoryEntity::class,
        MovieRatingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun userDao(): UserDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun tvChannelDao(): TVChannelDao
    abstract fun pendingMovieDao(): PendingMovieDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun movieRatingDao(): MovieRatingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cinestream_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Populate Movies
            val initialMovies = listOf(
                MovieEntity(
                    title = "La Sociedad de la Nieve",
                    year = 2023,
                    genre = "Acción y Aventura",
                    quality = "1080p",
                    criticRating = 4.8f,
                    userRating = 4.9f,
                    imageUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1",
                    platforms = "Netflix, RTVE Play",
                    description = "En 1972, el vuelo 571 de la Fuerza Aérea Uruguaya, fletado para llevar a un equipo de rugby a Chile, se estrella catastróficamente en los Andes."
                ),
                MovieEntity(
                    title = "Relatos Salvajes",
                    year = 2014,
                    genre = "Comedia",
                    quality = "1080p",
                    criticRating = 4.5f,
                    userRating = 4.7f,
                    imageUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728",
                    platforms = "Pluto TV, RTVE Play, Local Stream",
                    description = "Seis relatos independientes que alternan la intriga, la comedia y la violencia. Sus personajes se ven empujados hacia el abismo."
                ),
                MovieEntity(
                    title = "Parásitos",
                    year = 2019,
                    genre = "Drama",
                    quality = "4K",
                    criticRating = 4.9f,
                    userRating = 4.8f,
                    imageUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf",
                    platforms = "Prime Video, RTVE",
                    description = "Tanto el jardín como la casa de la adinerada familia Park son magníficos, pero Ki-taek y su familia desempleada pronto se infiltran."
                ),
                MovieEntity(
                    title = "El Secreto de sus Ojos",
                    year = 2009,
                    genre = "Gore y Terror",
                    quality = "1080p",
                    criticRating = 4.7f,
                    userRating = 4.8f,
                    imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5",
                    platforms = "Pluto TV, Local Stream, Canal AR",
                    description = "Benjamín Espósito, un agente judicial retirado, decide escribir una novela sobre un asesinato no resuelto que ocurrió hace 25 años."
                ),
                MovieEntity(
                    title = "El Laberinto del Fauno",
                    year = 2006,
                    genre = "Ciencia Ficción",
                    quality = "1080p",
                    criticRating = 4.8f,
                    userRating = 4.6f,
                    imageUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c",
                    platforms = "Prime Video, Local Stream",
                    description = "En España en 1944, la joven Ofelia conoce a un misterioso fauno en las ruinas de un laberinto mítico que le impone tres pruebas secretas."
                ),
                MovieEntity(
                    title = "Argentina, 1985",
                    year = 2022,
                    genre = "Drama",
                    quality = "4K",
                    criticRating = 4.6f,
                    userRating = 4.7f,
                    imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23",
                    platforms = "Prime Video",
                    description = "Inspirada en la historia real de los fiscales Julio Strassera y Luis Moreno Ocampo, que investigaron y enjuiciaron a la dictadura militar."
                ),
                MovieEntity(
                    title = "Volver",
                    year = 2006,
                    genre = "Comedia",
                    quality = "720p",
                    criticRating = 4.4f,
                    userRating = 4.3f,
                    imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba",
                    platforms = "RTVE Play, Pluto TV Español",
                    description = "Raimunda vive en Madrid con su hija adolescente y su marido borracho, lidiando con fantasmas del pasado familiar de La Mancha."
                ),
                MovieEntity(
                    title = "Playboy: El Legado de la Mansión",
                    year = 2021,
                    genre = "Adultos +18",
                    quality = "1080p",
                    criticRating = 4.2f,
                    userRating = 4.0f,
                    imageUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf",
                    platforms = "Playboy, VOD Premium",
                    description = "Duración: 52 minutos. Documental cronológico que recorre la historia de la revista más influyente de entretenimiento nocturno clásico, estilo de vida sofisticado y retro."
                ),
                MovieEntity(
                    title = "Late Night Cinema: Comedia de Mediana Edad",
                    year = 2019,
                    genre = "Adultos +18",
                    quality = "720p",
                    criticRating = 3.9f,
                    userRating = 3.5f,
                    imageUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c",
                    platforms = "XVideos, XNXX",
                    description = "Duración: 85 minutos. Divertido drama cómico de enredos románticos y picaresca en la televisión europea de medianoche, apto para un público maduro."
                ),
                MovieEntity(
                    title = "PornHub Masterclass: Historia del Entretenimiento",
                    year = 2023,
                    genre = "Adultos +18",
                    quality = "4K",
                    criticRating = 4.5f,
                    userRating = 4.3f,
                    imageUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1",
                    platforms = "PornHub, VOD Premium",
                    description = "Duración: 64 minutos. Análisis sociológico e informativo sobre el auge de la videografía en internet y la evolución del streaming masivo erótico."
                ),
                MovieEntity(
                    title = "XNXX Indias: Secretos bajo la Luna",
                    year = 2022,
                    genre = "Adultos +18",
                    quality = "1080p",
                    criticRating = 4.0f,
                    userRating = 3.8f,
                    imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba",
                    platforms = "XNXX, Local Stream",
                    description = "Duración: 48 minutos. Cortometraje dramático de misterio romántico, exploración afectiva profunda y dilemas de pareja contemporáneos."
                )
            )
            db.movieDao().insertMovies(initialMovies)

            // Populate Spanish channels (Free & Paid VIP)
            val initialChannels = listOf(
                TVChannelEntity(
                    name = "Pluto TV Cine",
                    logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba",
                    category = "Cine",
                    isPaid = false,
                    viewerCount = 1450,
                    fakeStreamDescription = "Transmitiendo Directores Clásicos en Español las 24 horas del día"
                ),
                TVChannelEntity(
                    name = "Butaca TV Gratis",
                    logoUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728",
                    category = "Cine",
                    isPaid = false,
                    viewerCount = 820,
                    fakeStreamDescription = "Las mejores producciones de cine independiente latinoamericano."
                ),
                TVChannelEntity(
                    name = "Comedia Central Live",
                    logoUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c",
                    category = "Comedia",
                    isPaid = false,
                    viewerCount = 1110,
                    fakeStreamDescription = "Comedia stand-up, sketches hilarantes y talk-shows en español."
                ),
                TVChannelEntity(
                    name = "Noticias 24/7",
                    logoUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5",
                    category = "Noticias",
                    isPaid = false,
                    viewerCount = 420,
                    fakeStreamDescription = "Informando sobre el acontecer mundial con objetividad e inmediatez."
                ),
                // Paid (VIP Monetized) Channels
                TVChannelEntity(
                    name = "HBO Latino VIP",
                    logoUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf",
                    category = "Cine",
                    isPaid = true,
                    viewerCount = 3800,
                    fakeStreamDescription = "Estrenos exclusivos, taquillazos de Hollywood y series premiadas (Canal Premium)"
                ),
                TVChannelEntity(
                    name = "FOX Sports Premium",
                    logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23",
                    category = "Deportes",
                    isPaid = true,
                    viewerCount = 5900,
                    fakeStreamDescription = "La emoción del fútbol, automovilismo y copas internacionales en directo (Canal Premium)"
                ),
                TVChannelEntity(
                    name = "Cinecanal Ultra",
                    logoUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1",
                    category = "Cine",
                    isPaid = true,
                    viewerCount = 2100,
                    fakeStreamDescription = "Colección selecta de cine moderno doblada al español en alta definición (Canal Premium)"
                ),
                TVChannelEntity(
                    name = "Discovery en Español VIP",
                    logoUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5",
                    category = "Noticias",
                    isPaid = true,
                    viewerCount = 1850,
                    fakeStreamDescription = "Documentales extraordinarios sobre ciencia, naturaleza e historia (Canal Premium)"
                ),
                TVChannelEntity(
                    name = "Playboy TV Latino (+18)",
                    logoUrl = "https://images.unsplash.com/photo-1594909122845-11baa439b7bf",
                    category = "Adultos (*)",
                    isPaid = true,
                    viewerCount = 1250,
                    fakeStreamDescription = "Canal premium dedicado al modelaje retro, estilo de vida sofisticado y contenido erótico romántico clásico las 24 horas."
                ),
                TVChannelEntity(
                    name = "XVideos Classic Gold (+18)",
                    logoUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1",
                    category = "Adultos (*)",
                    isPaid = false,
                    viewerCount = 3400,
                    fakeStreamDescription = "Películas independientes e historias amateur de largo formato (+30 minutos de duración garantizada)"
                ),
                TVChannelEntity(
                    name = "PornHub Cinema Live (+18)",
                    logoUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c",
                    category = "Adultos (*)",
                    isPaid = false,
                    viewerCount = 4200,
                    fakeStreamDescription = "Transmisión en directo en español con los títulos más célebres de la industria de largo formato"
                ),
                TVChannelEntity(
                    name = "XNXX Premium Cinema (+18)",
                    logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba",
                    category = "Adultos (*)",
                    isPaid = true,
                    viewerCount = 1800,
                    fakeStreamDescription = "Drama erótico, directores de culto y cine para adultos +18 doblado en español neutro premium"
                )
            )
            db.tvChannelDao().insertChannels(initialChannels)

            // Populate some initial pending search updates (simulating background scanning)
            val initialNotifications = listOf(
                PendingMovieNotification(
                    title = "Robot Dreams",
                    year = 2023,
                    genre = "Acción y Aventura",
                    quality = "1080p",
                    criticRating = 4.9f,
                    userRating = 4.8f,
                    imageUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728",
                    platforms = "RTVE Play, Local Stream, FilmIn",
                    description = "DOG vive en Manhattan y para no estar solo decide construirse un robot. Se vuelven amigos inseparables."
                ),
                PendingMovieNotification(
                    title = "La Estrella Azul",
                    year = 2024,
                    genre = "Drama",
                    quality = "1080p",
                    criticRating = 4.4f,
                    userRating = 4.5f,
                    imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23",
                    platforms = "RTVE Play, Local Stream",
                    description = "Un famoso rockero viaja al interior de Argentina para reencontrarse con su vocación y con comunidades folclóricas santafesinas."
                )
            )
            initialNotifications.forEach {
                db.pendingMovieDao().insertNotification(it)
            }

            // Create default user profile to start with
            db.userDao().insertUser(UserEntity("cine_lover", "Espectador CineStream", false))
        }
    }
}
