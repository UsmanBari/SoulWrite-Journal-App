package com.uh.smdprojectsoulwrite

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        recyclerView = findViewById(R.id.notifications_recycler_view)
        emptyTextView = findViewById(R.id.empty_notifications_text)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // For now, show empty state
        emptyTextView.visibility = TextView.VISIBLE

        backButton.setOnClickListener {
            finish()
        }
    }
}

