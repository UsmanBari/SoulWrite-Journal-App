package com.uh.smdprojectsoulwrite

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        titleTextView = findViewById(R.id.detail_title)
        dateTextView = findViewById(R.id.detail_date)
        contentTextView = findViewById(R.id.detail_content)
        imageView = findViewById(R.id.detail_image)

        // Get data from intent
        val title = intent.getStringExtra("journal_title") ?: ""
        val content = intent.getStringExtra("journal_content") ?: ""
        val imageUrl = intent.getStringExtra("journal_image") ?: ""
        val date = intent.getLongExtra("journal_date", 0)

        // Set data
        titleTextView.text = title
        contentTextView.text = content

        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        dateTextView.text = sdf.format(Date(date))

        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(imageView)
            imageView.visibility = ImageView.VISIBLE
        } else {
            imageView.visibility = ImageView.GONE
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}

