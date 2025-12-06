package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var followButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private lateinit var apiHelper: ApiHelper
    private lateinit var journalAdapter: JournalAdapter
    private var journals = mutableListOf<Journal>()

    private var userId = ""
    private var userName = ""
    private var currentUserId = ""
    private var isFollowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        apiHelper = ApiHelper(this)

        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getString("userId", "") ?: ""

        userId = intent.getStringExtra("user_id") ?: ""
        userName = intent.getStringExtra("user_name") ?: ""

        // Initialize views
        backButton = findViewById(R.id.back_button)
        userNameTextView = findViewById(R.id.user_name)
        followButton = findViewById(R.id.follow_button)
        recyclerView = findViewById(R.id.journals_recycler_view)
        emptyView = findViewById(R.id.empty_view)

        userNameTextView.text = userName

        // Hide follow button if viewing own profile
        if (userId == currentUserId) {
            followButton.visibility = View.GONE
        }

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        journalAdapter = JournalAdapter(journals) { journal ->
            openJournalDetail(journal)
        }
        recyclerView.adapter = journalAdapter

        // Load user's journals
        loadUserJournals()

        backButton.setOnClickListener {
            finish()
        }

        followButton.setOnClickListener {
            toggleFollow()
        }
    }

    private fun loadUserJournals() {
        apiHelper.getUserJournals("$userId&current_user_id=$currentUserId",
            onSuccess = { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val journalsArray = response.getJSONArray("journals")
                        journals.clear()

                        for (i in 0 until journalsArray.length()) {
                            val journalObj = journalsArray.getJSONObject(i)
                            val journal = Journal(
                                id = journalObj.getString("id"),
                                userId = journalObj.getString("user_id"),
                                title = journalObj.getString("title"),
                                content = journalObj.getString("content"),
                                imageUrl = journalObj.optString("image_url", ""),
                                thumbnailUrl = journalObj.optString("thumbnail_url", ""),
                                date = journalObj.getLong("date"),
                                userName = journalObj.optString("user_name", ""),
                                isPublic = journalObj.optInt("is_public", 0) == 1
                            )
                            journals.add(journal)
                        }

                        journalAdapter.notifyDataSetChanged()

                        if (journals.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                        } else {
                            emptyView.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Failed to load journals: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun toggleFollow() {
        if (isFollowing) {
            // Unfollow
            apiHelper.unfollowUser(currentUserId, userId,
                onSuccess = { response ->
                    isFollowing = false
                    followButton.text = "Follow"
                    Toast.makeText(this, "Unfollowed $userName", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Follow
            apiHelper.followUser(currentUserId, userId,
                onSuccess = { response ->
                    isFollowing = true
                    followButton.text = "Unfollow"
                    Toast.makeText(this, "Following $userName", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
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
        startActivity(intent)
    }
}

