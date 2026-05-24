package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request/Response Models using Moshi ---

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

// --- Retrofit API Service ---

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    /**
     * Ask Gemini for custom movie suggestions in Spanish or to parse user request.
     */
    suspend fun askGemini(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is placeholder or blank.")
            return "No se ha configurado la clave API de Gemini. Agrega GEMINI_API_KEY en el Panel de Secretos de AI Studio."
        }

        val requestBody = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(
                "Eres un sumiller o recomendador experto en películas dobladas al español o de habla hispana en CineStream. " +
                "Tus sugerencias siempre deben ser amables, enfocarse en plataformas gratuitas (Pluto TV, RTVE Play, Local Stream) " +
                "o servicios populares de streaming, y ser sumamente descriptivas de manera concisa."
            )))
        )

        return try {
            val response = api.generateContent(apiKey, requestBody)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No se obtuvo respuesta del recomendador de IA."
        } catch (e: Exception) {
            Log.e(TAG, "Error in askGemini: ${e.message}", e)
            "Error al consultar al recomendador. El recomendador local sugiere ver 'La Sociedad de la Nieve' o 'Relatos Salvajes' en CineStream."
        }
    }

    /**
     * Scan the web to find a new movie in Spanish.
     */
    suspend fun scanWebForMovie(): NewScannedMovieResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Simulated local discovery
            return getLocalSimulatedMovie()
        }

        val prompt = "Genera exactamente UN registro de una película real de estreno reciente (2023-2026) que sea famosa o interesante y esté disponible en plataformas de habla hispana como Pluto TV, RTVE Play, Prime Video o Netflix. Devuelve tu respuesta única y exclusivamente en formato JSON estructurado que siga el siguiente patrón. No agregues formatos de markdown ```json ni nada por el estilo, solo el texto plano JSON: " +
                "{\n" +
                "  \"title\": \"Nombre de la película\",\n" +
                "  \"year\": 2024,\n" +
                "  \"genre\": \"Acción y Aventura\" o \"Comedia\" o \"Drama\" o \"Ciencia Ficción\" o \"Gore y Terror\",\n" +
                "  \"quality\": \"1080p\" o \"4K\" o \"720p\",\n" +
                "  \"platforms\": \"Pluto TV, RTVE Play, Local Stream\",\n" +
                "  \"description\": \"Sinopsis atractiva en español.\",\n" +
                "  \"criticRating\": 4.5,\n" +
                "  \"userRating\": 4.3\n" +
                "}"

        val requestBody = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
        )

        return try {
            val response = api.generateContent(apiKey, requestBody)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: return getLocalSimulatedMovie()
            
            // Basic extraction in case of surrounding wrappers
            val jsonStart = rawText.indexOf("{")
            val jsonEnd = rawText.lastIndexOf("}")
            val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                rawText.substring(jsonStart, jsonEnd + 1)
            } else {
                rawText
            }

            val adapter = moshi.adapter(NewScannedMovieResult::class.java)
            adapter.fromJson(jsonString) ?: getLocalSimulatedMovie()
        } catch (e: Exception) {
            Log.e(TAG, "Error in scanWebForMovie: ${e.message}", e)
            getLocalSimulatedMovie()
        }
    }

    private val simulatedList = listOf(
        NewScannedMovieResult(
            title = "Segundo Premio",
            year = 2024,
            genre = "Drama",
            quality = "1080p",
            platforms = "RTVE Play, Cine local gratis",
            description = "En Granada a finales de los noventa, un grupo de música indie vive su momento más convulso al borde de la disolución, recreando la leyenda de Los Planetas.",
            criticRating = 4.6f,
            userRating = 4.4f
        ),
        NewScannedMovieResult(
            title = "Robot Dreams",
            year = 2023,
            genre = "Ciencia Ficción",
            quality = "1080p",
            platforms = "Pluto TV, Cine local",
            description = "DOG es un perro solitario de Manhattan que decide armar un robot amigo en un mundo nostálgico de los años 80.",
            criticRating = 4.9f,
            userRating = 4.8f
        ),
        NewScannedMovieResult(
            title = "As Bestas",
            year = 2022,
            genre = "Gore y Terror",
            quality = "4K",
            platforms = "RTVE Play, FilmIn",
            description = "Una pareja francesa se instala en una aldea gallega buscando conectar con la naturaleza de forma ecológica, pero desatan la hostilidad de sus vecinos.",
            criticRating = 4.8f,
            userRating = 4.6f
        ),
        NewScannedMovieResult(
            title = "La sociedad de la nieve",
            year = 2023,
            genre = "Acción y Aventura",
            quality = "4K",
            platforms = "Netflix, RTVE Play",
            description = "Supervivientes del accidente aéreo en los Andes luchan contra el frío extremo y el hambre.",
            criticRating = 4.9f,
            userRating = 4.9f
        ),
        NewScannedMovieResult(
            title = "Cerrar los Ojos",
            year = 2023,
            genre = "Drama",
            quality = "1080p",
            platforms = "RTVE Play, Movistar+",
            description = "Un célebre actor español desaparece durante un rodaje de película. Años después, un programa de TV revive el misterio.",
            criticRating = 4.7f,
            userRating = 4.5f
        )
    )

    private var simulatedIndex = 0

    private fun getLocalSimulatedMovie(): NewScannedMovieResult {
        val entry = simulatedList[simulatedIndex % simulatedList.size]
        simulatedIndex++
        return entry
    }
}

@JsonClass(generateAdapter = true)
data class NewScannedMovieResult(
    val title: String,
    val year: Int,
    val genre: String,
    val quality: String,
    val platforms: String,
    val description: String,
    val criticRating: Float,
    val userRating: Float
)
