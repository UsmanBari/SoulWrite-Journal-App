package com.uh.smdprojectsoulwrite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Message: ${it.title} - ${it.body}")
            sendNotification(it.title ?: "SoulWrite", it.body ?: "", remoteMessage.data)
        }

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data Payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Send token to server
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Log.d(TAG, "User not logged in, token will be sent after login")
            // Save token locally to send after login
            sharedPreferences.edit().putString("fcm_token", token).apply()
            return
        }

        val url = "${ApiHelper.BASE_URL}update_fcm_token.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.d(TAG, "FCM token sent to server: $response")
            },
            { error ->
                Log.e(TAG, "Failed to send FCM token: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "user_id" to userId.toString(),
                    "fcm_token" to token
                )
            }
        }

        queue.add(request)
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"] ?: ""

        when (type) {
            "like", "comment" -> {
                val journalId = data["journal_id"]?.toIntOrNull() ?: -1
                if (journalId != -1) {
                    sendNotification(
                        data["title"] ?: "New Notification",
                        data["body"] ?: "",
                        data
                    )
                }
            }
            "follow" -> {
                val userId = data["user_id"]?.toIntOrNull() ?: -1
                if (userId != -1) {
                    sendNotification(
                        data["title"] ?: "New Follower",
                        data["body"] ?: "",
                        data
                    )
                }
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        val intent = when (data["type"]) {
            "like", "comment" -> {
                Intent(this, DetailActivity::class.java).apply {
                    putExtra("journal_id", data["journal_id"]?.toIntOrNull() ?: -1)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
            "follow" -> {
                Intent(this, ProfileActivity::class.java).apply {
                    putExtra("user_id", data["user_id"]?.toIntOrNull() ?: -1)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
            else -> {
                Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "soulwrite_notifications"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SoulWrite Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for likes, comments, and follows"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FCMService"
    }
}

