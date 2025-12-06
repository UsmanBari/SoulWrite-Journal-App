package com.uh.smdprojectsoulwrite

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class NotificationSender(private val context: Context) {

    companion object {
        private const val TAG = "NotificationSender"
        private const val FCM_URL = "https://fcm.googleapis.com/v1/projects/smdprojectsoulwrite/messages:send"
    }

    /**
     * Send FCM notification from Android app
     * This bypasses the AwardSpace HTTPS blocking issue
     */
    suspend fun sendNotification(
        targetUserId: String,
        fcmToken: String,
        title: String,
        message: String,
        data: Map<String, String>
    ) {
        try {
            Log.d(TAG, "Sending FCM notification to user $targetUserId")

            // Get OAuth2 access token (runs in background thread)
            val accessToken = getAccessToken()

            if (accessToken == null) {
                Log.e(TAG, "Failed to get access token")
                return
            }

            Log.d(TAG, "Got access token, sending FCM...")

            // Build FCM message
            val fcmMessage = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", fcmToken)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", message)
                    })
                    put("data", JSONObject(data))
                    put("android", JSONObject().apply {
                        put("priority", "high")
                        put("notification", JSONObject().apply {
                            put("sound", "default")
                            put("channel_id", "default")
                        })
                    })
                })
            }

            // Send via Volley (must be on main thread)
            withContext(Dispatchers.Main) {
                sendFCMRequest(accessToken, fcmMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification: ${e.message}", e)
        }
    }

    /**
     * Get OAuth2 access token using service account
     */
    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val stream = context.assets.open("services_json.json")

            val credentials = GoogleCredentials.fromStream(stream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

            credentials.refreshIfExpired()
            val token = credentials.accessToken.tokenValue

            Log.d(TAG, "Access token obtained: ${token.take(30)}...")
            token

        } catch (e: IOException) {
            Log.e(TAG, "Failed to load service account: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token: ${e.message}", e)
            null
        }
    }

    /**
     * Send FCM HTTP request
     */
    private fun sendFCMRequest(accessToken: String, message: JSONObject) {
        val request = object : JsonObjectRequest(
            Request.Method.POST,
            FCM_URL,
            message,
            { response ->
                Log.d(TAG, "✅ FCM sent successfully: $response")
            },
            { error ->
                Log.e(TAG, "❌ FCM error: ${error.message}")
                error.networkResponse?.let {
                    Log.e(TAG, "Response code: ${it.statusCode}")
                    Log.e(TAG, "Response data: ${String(it.data)}")
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $accessToken",
                    "Content-Type" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
}

