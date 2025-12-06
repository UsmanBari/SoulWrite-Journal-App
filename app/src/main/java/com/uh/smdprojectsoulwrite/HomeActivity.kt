package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {

    private lateinit var searchIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var homeIcon: View
    private lateinit var searchIconBottom: View
    private lateinit var alertsIcon: View
    private lateinit var profileIcon: View

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var apiHelper: ApiHelper
    private lateinit var journalAdapter: JournalAdapter
    private var journals = mutableListOf<Journal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dbHelper = DatabaseHelper(this)
        apiHelper = ApiHelper(this)

        // Clear Glide image cache to force reload
        try {
            Glide.get(this).clearMemory()
            Thread {
                Glide.get(applicationContext).clearDiskCache()
            }.start()
            android.util.Log.d("HomeActivity", "Cleared Glide image cache")
        } catch (e: Exception) {
            android.util.Log.e("HomeActivity", "Error clearing cache: ${e.message}")
        }

        // Clear old cached data to get fresh URLs from server
        val prefs = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val lastClearTime = prefs.getLong("lastDbClear", 0)
        val currentTime = System.currentTimeMillis()
        // Clear cache every 24 hours or on first run
        if (currentTime - lastClearTime > 24 * 60 * 60 * 1000 || lastClearTime == 0L) {
            android.util.Log.d("HomeActivity", "Clearing old database cache")
            dbHelper.clearAllJournals()
            prefs.edit().putLong("lastDbClear", currentTime).apply()
        }

        // Initialize views
        searchIcon = findViewById(R.id.search_icon)
        val notificationIcon = findViewById<ImageView>(R.id.notification_icon)
        recyclerView = findViewById(R.id.journals_recycler_view)
        fab = findViewById(R.id.fab_add)
        homeIcon = findViewById(R.id.home_icon)
        searchIconBottom = findViewById(R.id.search_icon_bottom)
        alertsIcon = findViewById(R.id.alerts_icon)
        profileIcon = findViewById(R.id.profile_icon)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        journalAdapter = JournalAdapter(journals) { journal ->
            openJournalDetail(journal)
        }
        recyclerView.adapter = journalAdapter

        // Load journals
        loadJournals()

        // Setup click listeners
        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        searchIcon.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        fab.setOnClickListener {
            startActivity(Intent(this, AddEntryActivity::class.java))
        }

        searchIconBottom.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        alertsIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadJournals()
    }

    private fun loadJournals() {
        // First load from local database
        val localJournals = dbHelper.getAllJournals()
        journals.clear()
        journals.addAll(localJournals)
        journalAdapter.notifyDataSetChanged()

        // Then load feed from server (own journals + followed users' public journals)
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "") ?: ""

        android.util.Log.d("HomeActivity", "Loading feed for user: $userId")

        apiHelper.getFeed(userId,
            onSuccess = { response ->
                try {
                    android.util.Log.d("HomeActivity", "Feed response: $response")
                    val success = response.getBoolean("success")
                    if (success) {
                        val journalsArray = response.getJSONArray("journals")
                        android.util.Log.d("HomeActivity", "Found ${journalsArray.length()} journals in feed")
                        journals.clear()

                        for (i in 0 until journalsArray.length()) {
                            val journalObj = journalsArray.getJSONObject(i)

                            val imageUrl = journalObj.optString("image_url", "")
                            val thumbnailUrl = journalObj.optString("thumbnail_url", "")

                            android.util.Log.d("HomeActivity", "Journal ${i}: ${journalObj.getString("title")}")
                            android.util.Log.d("HomeActivity", "  User: ${journalObj.optString("author_name", "Unknown")}")
                            android.util.Log.d("HomeActivity", "  Public: ${journalObj.optInt("is_public", 0)}")

                            val journal = Journal(
                                id = journalObj.optInt("id", 0).toString(),
                                userId = journalObj.optInt("user_id", 0).toString(),
                                title = journalObj.optString("title", ""),
                                content = journalObj.optString("content", ""),
                                imageUrl = imageUrl,
                                thumbnailUrl = thumbnailUrl,
                                date = journalObj.optLong("date", 0),
                                userName = journalObj.optString("user_name", ""),
                                userFcmToken = journalObj.optString("user_fcm_token", ""),
                                isPublic = journalObj.optInt("is_public", 0) == 1,
                                authorName = journalObj.optString("author_name", ""),
                                likeCount = journalObj.optInt("like_count", 0),
                                commentCount = journalObj.optInt("comment_count", 0),
                                isLiked = journalObj.optBoolean("is_liked", false)
                            )
                            journals.add(journal)
                            // Save to local database
                            dbHelper.insertJournal(journal)
                        }

                        journalAdapter.notifyDataSetChanged()
                        android.util.Log.d("HomeActivity", "Feed loaded successfully with ${journals.size} journals")
                    } else {
                        val message = response.optString("message", "Unknown error")
                        android.util.Log.e("HomeActivity", "Feed error: $message")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HomeActivity", "Exception parsing feed: ${e.message}", e)
                    e.printStackTrace()
                }
            },
            onError = { error ->
                android.util.Log.e("HomeActivity", "Feed network error: $error")
                // Already showing local data, no need to show error
            }
        )
    }

    private fun openJournalDetail(journal: Journal) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("journal_id", journal.id)
        intent.putExtra("journal_title", journal.title)
        intent.putExtra("journal_content", journal.content)
        intent.putExtra("journal_image", journal.imageUrl)
        intent.putExtra("journal_thumbnail", journal.thumbnailUrl)
        intent.putExtra("journal_date", journal.date)
        intent.putExtra("journal_user_id", journal.userId)
        intent.putExtra("journal_is_public", journal.isPublic)
        intent.putExtra("journal_like_count", journal.likeCount)
        intent.putExtra("journal_comment_count", journal.commentCount)
        intent.putExtra("journal_is_liked", journal.isLiked)
        intent.putExtra("journal_owner_name", journal.userName)
        intent.putExtra("journal_owner_fcm_token", journal.userFcmToken)
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh the journals list after delete
            loadJournals()
        }
    }
}

