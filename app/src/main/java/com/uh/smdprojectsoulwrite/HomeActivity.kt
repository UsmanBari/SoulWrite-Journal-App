package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // Initialize views
        searchIcon = findViewById(R.id.search_icon)
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

        // Then sync with server
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "") ?: ""

        apiHelper.getJournals(userId,
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
                                imageUrl = journalObj.getString("image_url"),
                                thumbnailUrl = journalObj.getString("thumbnail_url"),
                                date = journalObj.getLong("date"),
                                userName = journalObj.optString("user_name", "")
                            )
                            journals.add(journal)
                            // Save to local database
                            dbHelper.insertJournal(journal)
                        }

                        journalAdapter.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onError = { error ->
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
        intent.putExtra("journal_date", journal.date)
        startActivity(intent)
    }
}

