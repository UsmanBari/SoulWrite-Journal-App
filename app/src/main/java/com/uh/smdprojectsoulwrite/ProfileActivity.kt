package com.uh.smdprojectsoulwrite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var homeIcon: android.widget.LinearLayout
    private lateinit var searchIcon: android.widget.LinearLayout
    private lateinit var alertsIcon: android.widget.LinearLayout
    private lateinit var profileIcon: android.widget.LinearLayout

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var apiHelper: ApiHelper
    private lateinit var journalAdapter: JournalAdapter
    private var journals = mutableListOf<Journal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)
        apiHelper = ApiHelper(this)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        nameTextView = findViewById(R.id.profile_name)
        emailTextView = findViewById(R.id.profile_email)
        phoneTextView = findViewById(R.id.profile_phone)
        editProfileButton = findViewById(R.id.edit_profile_button)
        changePasswordButton = findViewById(R.id.change_password_button)
        logoutButton = findViewById(R.id.logout_button)
        recyclerView = findViewById(R.id.journals_recycler_view)
        emptyView = findViewById(R.id.empty_view)
        homeIcon = findViewById(R.id.home_icon)
        searchIcon = findViewById(R.id.search_icon)
        alertsIcon = findViewById(R.id.alerts_icon)
        profileIcon = findViewById(R.id.profile_icon)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        journalAdapter = JournalAdapter(journals) { journal ->
            openJournalDetail(journal)
        }
        recyclerView.adapter = journalAdapter

        // Load user data and journals
        loadUserData()
        loadUserJournals()

        backButton.setOnClickListener {
            finish()
        }


        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        logoutButton.setOnClickListener {
            logout()
        }

        homeIcon.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        searchIcon.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        alertsIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        profileIcon.setOnClickListener {
            // Already on profile, refresh
            loadUserJournals()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        loadUserJournals()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("userName", "User") ?: "User"
        val email = sharedPreferences.getString("userEmail", "") ?: ""
        val phone = sharedPreferences.getString("userPhone", "") ?: ""

        nameTextView.text = name
        emailTextView.text = email
        phoneTextView.text = phone
    }

    private fun loadUserJournals() {
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "") ?: ""

        // Load from local database first
        val localJournals = dbHelper.getUserJournals(userId)
        journals.clear()
        journals.addAll(localJournals)
        journalAdapter.notifyDataSetChanged()
        updateEmptyView()

        // Then load from server
        apiHelper.getUserJournals(userId,
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
                        updateEmptyView()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onError = { error ->
                // Already showing local data
            }
        )
    }

    private fun updateEmptyView() {
        if (journals.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
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



    private fun logout() {

        // Clear SharedPreferences
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

