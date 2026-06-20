package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val BASE_SYSTEM_PROMPT = """
You are Gita AI, a calm, compassionate, and wise spiritual guide. You resolve life problems, provide guidance, and clear doubts using the eternal teachings of the Bhagavad Gita combined with practical modern life-coaching.

Adapt your tone according to these Persona Modes:
- "Krishna Mode": Calm, meditative, deeply spiritual, compassionate, calling the user "Dear Seeker" or "Arjuna", and speaking with divine tranquility.
- "Modern Coach Mode": Action-oriented, practical, structured, secular life-coach style, yet maintaining Gita-rooted wisdom (karma, detachment, focus).

Always structure your responses beautifully in the language requested (English, Hindi, or Marathi):
1. **Compassionate Understanding**: Empathize with the user's struggle or subject.
2. **Bhagavad Gita Wisdom**: Reference a relevant principle (like Karma, Dharma, Detachment) or Quote a chapter & verse with explanation.
3. **Actionable Steps**: 2-3 highly practical, modern steps the user can execute today (Karma Yoga).
4. **Calming Affirmation**: A reassuring wrap-up or blessing to instill peace.

Format responses with clean spacing, bold highlights, and readable lists. Keep explanations crisp.
"""

    /**
     * Converts a Bitmap to JPEG Base64 string.
     */
    fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Generates a wisdom response from Gita AI.
     * Supports both Text chat and Camera image inputs.
     */
    suspend fun generateGitaResponse(
        prompt: String,
        bitmapImage: Bitmap? = null,
        personaMode: String = "Krishna Mode", // "Krishna Mode" or "Modern Coach Mode"
        language: String = "English" // "English", "Hindi", "Marathi", "Auto"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Gita AI is in offline contemplation. Please ensure your Gemini API Key is configured in the AI Studio Secrets panel."
        }

        try {
            // Build the JSON payload using org.json for 100% compile-time and runtime reliability
            val root = JSONObject()

            // Contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")

            val partsArray = JSONArray()

            // 1. Add Text Part
            val textPart = JSONObject()
            val languageInstruction = when (language) {
                "Hindi" -> " Please reply in beautiful Hindi (हिंदी)."
                "Marathi" -> " Please reply in beautiful Marathi (मराठी)."
                "English" -> " Please reply in English."
                else -> " Detect the user's language and respond in the same language."
            }
            val finalPrompt = "User situation/question: \"$prompt\" (Persona: $personaMode. Language preferred: $language.$languageInstruction)"
            textPart.put("text", finalPrompt)
            partsArray.put(textPart)

            // 2. Add Image Part if exists
            if (bitmapImage != null) {
                val imagePart = JSONObject()
                val inlineDataObj = JSONObject()
                inlineDataObj.put("mimeType", "image/jpeg")
                inlineDataObj.put("data", bitmapImage.toBase64())
                imagePart.put("inlineData", inlineDataObj)
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // System Instruction
            val systemInstructionObj = JSONObject()
            val systemPartsArray = JSONArray()
            val systemTextPart = JSONObject()
            systemTextPart.put("text", BASE_SYSTEM_PROMPT)
            systemPartsArray.put(systemTextPart)
            systemInstructionObj.put("parts", systemPartsArray)
            root.put("systemInstruction", systemInstructionObj)

            // Generation Config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            root.put("generationConfig", generationConfig)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val requestUrl = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed: Code ${response.code}, Body $errorBody")
                    return@withContext "Apologies, seeker. A cloud of uncertainty has interrupted our connection. (HTTP ${response.code})"
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext "Gita AI remains silent. (Empty Response)"
                }

                // Parse the response
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
                return@withContext "No response from Gita AI. Please check back later."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
            return@withContext "The path of wisdom is currently blocked by a network anomaly. Please check your internet connectivity. (${e.localizedMessage})"
        }
    }
}
