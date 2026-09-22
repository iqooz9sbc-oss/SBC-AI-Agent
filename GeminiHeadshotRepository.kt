package com.yourapp.features.headshot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Thin abstraction so the ViewModel/tests don't depend on OkHttp or the Gemini
 * wire format directly.
 */
interface HeadshotRepository {
    suspend fun generateHeadshot(inputImage: Bitmap, style: HeadshotStyle): Result<Bitmap>
}

/**
 * NOTE ON MODEL / ENDPOINT:
 * This targets a Gemini multimodal image-generation model (image-in, image-out),
 * i.e. the "Nano Banana" family exposed through generateContent, as opposed to a
 * pure text-to-image Imagen endpoint (which does not accept an input photo to
 * preserve identity from). Model IDs on this API move fast — before shipping,
 * confirm the current model string and endpoint path against Google's Gemini API
 * docs (ai.google.dev) and swap GEMINI_MODEL below if it has changed.
 */
class GeminiHeadshotRepository(
    private val apiKey: String,
    private val client: OkHttpClient = defaultClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : HeadshotRepository {

    companion object {
        private const val TAG = "GeminiHeadshotRepo"
        private const val GEMINI_MODEL = "gemini-2.5-flash-image" // verify against current docs
        private const val ENDPOINT_BASE = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val MAX_INPUT_DIMENSION = 1536 // downscale before upload; keeps payload sane

        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS) // image generation is slow; don't time out early
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun generateHeadshot(
        inputImage: Bitmap,
        style: HeadshotStyle
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(style)
            val base64Input = bitmapToBase64(downscaleIfNeeded(inputImage))

            val requestBody = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(
                                inlineData = InlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Input
                                )
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseModalities = listOf("IMAGE")
                )
            )

            val bodyJson = json.encodeToString(requestBody)
            val url = "$ENDPOINT_BASE/$GEMINI_MODEL:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API error ${response.code}: $responseBody")
                    return@withContext Result.failure(
                        GeminiApiException(
                            userMessage = mapHttpErrorToUserMessage(response.code),
                            httpCode = response.code,
                            rawBody = responseBody
                        )
                    )
                }

                val parsed = json.decodeFromString<GenerateContentResponse>(responseBody)
                val imagePart = parsed.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull { it.inlineData != null }
                    ?.inlineData

                if (imagePart == null) {
                    Log.e(TAG, "No image returned. Raw response: $responseBody")
                    return@withContext Result.failure(
                        GeminiApiException(
                            userMessage = "The model didn't return an image. Please try again.",
                            httpCode = response.code,
                            rawBody = responseBody
                        )
                    )
                }

                val outputBytes = Base64.decode(imagePart.data, Base64.DEFAULT)
                val outputBitmap = BitmapFactory.decodeByteArray(outputBytes, 0, outputBytes.size)
                    ?: return@withContext Result.failure(
                        GeminiApiException(
                            userMessage = "Received an unreadable image from the server.",
                            httpCode = response.code,
                            rawBody = null
                        )
                    )

                Result.success(outputBitmap)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during headshot generation", e)
            Result.failure(
                GeminiApiException(
                    userMessage = "Network error. Check your connection and try again.",
                    httpCode = null,
                    rawBody = null,
                    cause = e
                )
            )
        } catch (e: SerializationException) {
            Log.e(TAG, "Failed to parse Gemini response", e)
            Result.failure(
                GeminiApiException(
                    userMessage = "Unexpected server response. Please try again.",
                    httpCode = null,
                    rawBody = null,
                    cause = e
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during headshot generation", e)
            Result.failure(
                GeminiApiException(
                    userMessage = "Something went wrong generating your headshot.",
                    httpCode = null,
                    rawBody = null,
                    cause = e
                )
            )
        }
    }

    /**
     * Prompt is deliberately built server/repo-side from a closed enum (HeadshotStyle),
     * never from raw free-text user input, to avoid prompt injection into the image
     * model and to keep output consistent with the "professional headshot" brief.
     */
    private fun buildPrompt(style: HeadshotStyle): String = """
        Transform the person in the provided photo into an ultra-realistic, 8K,
        cinematic professional studio headshot.

        Strict requirements:
        - Preserve the person's exact facial identity: face shape, eyes, nose, mouth,
          skin tone, skin texture, and any distinguishing features. Do not beautify,
          reshape, or change ethnicity, age, or gender presentation.
        - Change clothing to professional attire: ${style.promptFragment}.
        - Replace the background with a softly blurred, neutral professional studio
          backdrop (subtle bokeh, gray or muted gradient).
        - Apply soft, even studio lighting typical of corporate headshot photography,
          with natural catchlights in the eyes and no harsh shadows.
        - Output should look like a real photograph taken with a professional camera
          and portrait lens (e.g., 85mm f/1.8), not an illustration, painting, or
          cartoon, and not overly airbrushed or plastic-looking.
        - Keep the framing as a head-and-shoulders portrait, centered, facing camera
          or at a natural slight angle matching the original pose where reasonable.

        Return only the final edited photograph.
    """.trimIndent()

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun downscaleIfNeeded(bitmap: Bitmap): Bitmap {
        val largestSide = maxOf(bitmap.width, bitmap.height)
        if (largestSide <= MAX_INPUT_DIMENSION) return bitmap
        val scale = MAX_INPUT_DIMENSION.toFloat() / largestSide
        val newWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val newHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun mapHttpErrorToUserMessage(code: Int): String = when (code) {
        400 -> "The photo couldn't be processed. Try a clearer, well-lit photo."
        401, 403 -> "Authentication with the image service failed. Please try again later."
        429 -> "Too many requests right now. Please wait a moment and try again."
        in 500..599 -> "The image service is temporarily unavailable. Please try again shortly."
        else -> "Couldn't generate your headshot right now. Please try again."
    }
}

/** Carries both a safe user-facing message and diagnostic detail for logging. */
class GeminiApiException(
    val userMessage: String,
    val httpCode: Int?,
    val rawBody: String?,
    cause: Throwable? = null
) : Exception(userMessage, cause)

// --- Gemini wire format (minimal subset needed for image-in/image-out calls) ---

@Serializable
private data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
private data class Content(
    val parts: List<Part>
)

@Serializable
private data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
private data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
private data class GenerationConfig(
    val responseModalities: List<String>? = null
)

@Serializable
private data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
private data class Candidate(
    val content: Content? = null
)
