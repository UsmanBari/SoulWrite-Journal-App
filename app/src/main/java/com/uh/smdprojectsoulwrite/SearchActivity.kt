package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher

class SearchActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView

    private lateinit var apiHelper: ApiHelper
    private lateinit var journalAdapter: JournalAdapter
    private var journals = mutableListOf<Journal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        apiHelper = ApiHelper(this)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        searchEditText = findViewById(R.id.search_input)
        recyclerView = findViewById(R.id.search_results_recycler_view)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        journalAdapter = JournalAdapter(journals) { journal ->
            openJournalDetail(journal)
        }
        recyclerView.adapter = journalAdapter

        // Setup search
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 3) {
                    searchJournals(query)
                }
            }
        })

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun searchJournals(query: String) {
        apiHelper.searchJournals(query,
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
                        }

                        journalAdapter.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onError = { error ->
                // Handle error
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

