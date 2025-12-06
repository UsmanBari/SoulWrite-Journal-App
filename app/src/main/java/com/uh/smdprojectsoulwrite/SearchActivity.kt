package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class SearchActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    private lateinit var apiHelper: ApiHelper
    private lateinit var journalAdapter: JournalAdapter
    private lateinit var userAdapter: UserAdapter
    private var journals = mutableListOf<Journal>()
    private var users = mutableListOf<User>()
    private var currentTab = 0 // 0 for journals, 1 for users
    private var currentUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        apiHelper = ApiHelper(this)

        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getString("userId", "") ?: ""

        // Initialize views
        backButton = findViewById(R.id.back_button)
        searchEditText = findViewById(R.id.search_input)
        searchButton = findViewById(R.id.search_button)
        tabLayout = findViewById(R.id.search_tabs)
        recyclerView = findViewById(R.id.search_results_recycler_view)
        emptyView = findViewById(R.id.empty_view)

        // Setup tabs
        tabLayout.addTab(tabLayout.newTab().setText("Journals"))
        tabLayout.addTab(tabLayout.newTab().setText("Users"))

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup journal adapter
        journalAdapter = JournalAdapter(journals) { journal ->
            openJournalDetail(journal)
        }

        // Setup user adapter
        userAdapter = UserAdapter(users,
            onUserClick = { user -> openUserProfile(user) },
            onFollowClick = { user -> toggleFollow(user) }
        )

        recyclerView.adapter = journalAdapter

        // Tab change listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                if (currentTab == 0) {
                    recyclerView.adapter = journalAdapter
                } else {
                    recyclerView.adapter = userAdapter
                }
                // Clear search when changing tabs
                journals.clear()
                users.clear()
                journalAdapter.notifyDataSetChanged()
                userAdapter.notifyDataSetChanged()
                showEmptyView()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        searchButton.setOnClickListener {
            performSearch()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()

        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
            return
        }

        if (query.length < 2) {
            Toast.makeText(this, "Please enter at least 2 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentTab == 0) {
            searchJournals(query)
        } else {
            searchUsers(query)
        }
    }

    private fun searchJournals(query: String) {
        emptyView.visibility = View.GONE
        apiHelper.searchJournals(query, currentUserId,
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
                            showEmptyView()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Search failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun searchUsers(query: String) {
        emptyView.visibility = View.GONE
        apiHelper.searchUsers("$query&current_user_id=$currentUserId",
            onSuccess = { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val usersArray = response.getJSONArray("users")
                        users.clear()

                        for (i in 0 until usersArray.length()) {
                            val userObj = usersArray.getJSONObject(i)
                            val user = User(
                                id = userObj.getString("id"),
                                name = userObj.getString("name"),
                                email = userObj.getString("email"),
                                phone = userObj.optString("phone", ""),
                                profileImageUrl = userObj.optString("profile_image_url", ""),
                                isFollowing = userObj.optBoolean("is_following", false)
                            )
                            users.add(user)
                        }

                        userAdapter.notifyDataSetChanged()

                        if (users.isEmpty()) {
                            showEmptyView()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Search failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun toggleFollow(user: User) {
        if (user.isFollowing) {
            // Unfollow
            apiHelper.unfollowUser(currentUserId, user.id,
                onSuccess = { response ->
                    user.isFollowing = false
                    userAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Unfollowed ${user.name}", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Follow
            apiHelper.followUser(currentUserId, user.id,
                onSuccess = { response ->
                    user.isFollowing = true
                    userAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Following ${user.name}", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun openUserProfile(user: User) {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra("user_id", user.id)
        intent.putExtra("user_name", user.name)
        startActivity(intent)
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
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh search results after delete
            performSearch()
        }
    }

    private fun showEmptyView() {
        if (currentTab == 0 && journals.isEmpty()) {
            emptyView.text = "No journals found"
            emptyView.visibility = View.VISIBLE
        } else if (currentTab == 1 && users.isEmpty()) {
            emptyView.text = "No users found"
            emptyView.visibility = View.VISIBLE
        }
    }
}

