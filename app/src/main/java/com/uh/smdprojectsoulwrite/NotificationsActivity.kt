package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class NotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var apiHelper: ApiHelper
    private val notifications = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        supportActionBar?.title = "Notifications"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        apiHelper = ApiHelper(this)

        recyclerView = findViewById(R.id.notifications_recycler)
        emptyText = findViewById(R.id.empty_text)
        progressBar = findViewById(R.id.progress_bar)

        adapter = NotificationAdapter(notifications) { notification ->
            handleNotificationClick(notification)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE

        val url = "${ApiHelper.GET_NOTIFICATIONS_URL}?user_id=$userId"
        apiHelper.makeGetRequest(
            url = url,
            onSuccess = { response ->
                progressBar.visibility = View.GONE
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        val notificationsArray = jsonResponse.getJSONArray("notifications")
                        notifications.clear()

                        for (i in 0 until notificationsArray.length()) {
                            val notifObj = notificationsArray.getJSONObject(i)

                            // Parse the data field correctly - it comes as a string from server
                            val dataString = notifObj.optString("data", "{}")
                            val dataJson = try {
                                if (dataString.isNotEmpty()) JSONObject(dataString) else null
                            } catch (e: Exception) {
                                null
                            }

                            val notification = NotificationItem(
                                id = notifObj.getInt("id"),
                                type = notifObj.getString("type"),
                                title = notifObj.getString("title"),
                                message = notifObj.getString("message"),
                                fromUserName = notifObj.optString("from_user_name", "Someone"),
                                isRead = notifObj.getInt("is_read") == 1,
                                createdAt = notifObj.getString("created_at"),
                                data = dataJson
                            )
                            notifications.add(notification)
                        }

                        if (notifications.isEmpty()) {
                            emptyText.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyText.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.notifyDataSetChanged()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing notifications", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        when (notification.type) {
            "like", "comment" -> {
                val journalId = notification.data?.optString("journal_id", "") ?: ""
                if (journalId.isNotEmpty()) {
                    val intent = Intent(this, DetailActivity::class.java)
                    intent.putExtra("journal_id", journalId)
                    startActivity(intent)
                }
            }
            "follow" -> {
                val userId = notification.data?.optString("user_id", "") ?: ""
                if (userId.isNotEmpty()) {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
