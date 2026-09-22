package com.verbatim.studio.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.verbatim.studio.model.GeminiModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun callGemini(apiKey: String, preferredModel: String, prompt: String): Result<String> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Missing Gemini API Key. Go to Settings."))
            }

            val modelsToTry = mutableListOf(preferredModel).apply {
                addAll(GeminiModels.allModels.filter { it != preferredModel })
            }

            val payloadJson = gson.toJson(
                mapOf(
                    "contents" to listOf(
                        mapOf(
                            "parts" to listOf(
                                mapOf("text" to prompt)
                            )
                        )
                    )
                )
            )

            var lastError: Exception? = null

            for (model in modelsToTry) {
                try {
                    val result = tryModel(apiKey, model, payloadJson)
                    return@withContext Result.success(result)
                } catch (e: Exception) {
                    lastError = e
                }
            }

            Result.failure(lastError ?: Exception("All Gemini models failed after retries"))
        }

    private suspend fun tryModel(apiKey: String, model: String, payloadJson: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        var attempt = 1

        while (attempt <= 2) {
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", apiKey)
                .post(payloadJson.toRequestBody(jsonMediaType))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        val status = response.code
                        val errorMsg = try {
                            val obj = gson.fromJson(responseBody, JsonObject::class.java)
                            obj.getAsJsonObject("error")?.get("message")?.asString ?: "HTTP $status"
                        } catch (e: Exception) {
                            "HTTP $status"
                        }

                        val retryable = status == 429 || status >= 500
                        if (attempt == 1 && retryable) {
                            delay(1000L * attempt)
                            attempt++
                            return@use
                        }
                        throw Exception("$model failed ($status): $errorMsg")
                    }

                    val json = gson.fromJson(responseBody, JsonObject::class.java)
                    val candidate = json.getAsJsonArray("candidates")?.firstOrNull()?.asJsonObject
                    val text = candidate?.getAsJsonObject("content")
                        ?.getAsJsonArray("parts")?.firstOrNull()?.asJsonObject
                        ?.get("text")?.asString

                    if (text.isNullOrBlank()) {
                        throw Exception("Empty response payload")
                    }

                    return text
                }
            } catch (e: Exception) {
                if (attempt == 2) throw e
                delay(1000L * attempt)
                attempt++
            }
        }

        throw Exception("Failed model: $model")
    }

    suspend fun testConnection(apiKey: String, model: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Missing API Key"))
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
            val payload = gson.toJson(
                mapOf(
                    "contents" to listOf(
                        mapOf("parts" to listOf(mapOf("text" to "Ping test")))
                    )
                )
            )

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", apiKey)
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        val errorMsg = try {
                            val obj = gson.fromJson(body, JsonObject::class.java)
                            obj.getAsJsonObject("error")?.get("message")?.asString ?: "HTTP ${response.code}"
                        } catch (e: Exception) {
                            "HTTP ${response.code}"
                        }
                        return@withContext Result.failure(Exception(errorMsg))
                    }

                    val json = gson.fromJson(body, JsonObject::class.java)
                    val text = json.getAsJsonArray("candidates")?.firstOrNull()?.asJsonObject
                        ?.getAsJsonObject("content")
                        ?.getAsJsonArray("parts")?.firstOrNull()?.asJsonObject
                        ?.get("text")?.asString

                    if (!text.isNullOrBlank()) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception("Invalid response"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
