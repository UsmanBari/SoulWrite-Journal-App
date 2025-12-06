package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var editButton: ImageView
    private lateinit var deleteButton: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var apiHelper: ApiHelper

    // Like and Comment UI elements
    private lateinit var interactionSection: LinearLayout
    private lateinit var likeButton: LinearLayout
    private lateinit var likeIcon: ImageView
    private lateinit var likeCountText: TextView
    private lateinit var commentButton: LinearLayout
    private lateinit var commentCountText: TextView
    private lateinit var commentsRecycler: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendCommentButton: ImageView

    private var currentJournalId: String = ""
    private var currentUserId: String = ""
    private var journalOwnerId: String = ""
    private var journalTitle: String = ""
    private var journalOwnerName: String = ""
    private var journalOwnerFcmToken: String = ""
    private var isLiked: Boolean = false
    private var likeCount: Int = 0
    private var commentCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_detail)

            // Initialize
            apiHelper = ApiHelper(this)

            // Initialize views
            backButton = findViewById(R.id.back_button)
            editButton = findViewById(R.id.edit_button)
            deleteButton = findViewById(R.id.delete_button)
            titleTextView = findViewById(R.id.detail_title)
            dateTextView = findViewById(R.id.detail_date)
            contentTextView = findViewById(R.id.detail_content)
            imageView = findViewById(R.id.detail_image)

            // Initialize like/comment views
            interactionSection = findViewById(R.id.interaction_section)
            likeButton = findViewById(R.id.like_button)
            likeIcon = findViewById(R.id.like_icon)
            likeCountText = findViewById(R.id.like_count)
            commentButton = findViewById(R.id.comment_button)
            commentCountText = findViewById(R.id.comment_count)
            commentsRecycler = findViewById(R.id.comments_recycler)
            commentInput = findViewById(R.id.comment_input)
            sendCommentButton = findViewById(R.id.send_comment_button)

            // Get data from intent
            val journalId = intent.getStringExtra("journal_id") ?: ""
            val title = intent.getStringExtra("journal_title") ?: ""
            val content = intent.getStringExtra("journal_content") ?: ""
            val imageUrl = intent.getStringExtra("journal_image") ?: ""
            val thumbnailUrl = intent.getStringExtra("journal_thumbnail") ?: ""
            val date = intent.getLongExtra("journal_date", 0)
            val userId = intent.getStringExtra("journal_user_id") ?: ""
            val isPublic = intent.getBooleanExtra("journal_is_public", false)

            currentJournalId = journalId
            journalOwnerId = userId
            journalTitle = title
            journalOwnerName = intent.getStringExtra("journal_owner_name") ?: "Someone"
            journalOwnerFcmToken = intent.getStringExtra("journal_owner_fcm_token") ?: ""

            // Get like/comment counts from intent if available
            likeCount = intent.getIntExtra("journal_like_count", 0)
            commentCount = intent.getIntExtra("journal_comment_count", 0)
            isLiked = intent.getBooleanExtra("journal_is_liked", false)

            android.util.Log.d("DetailActivity", "Opening journal: id=$journalId, userId=$userId, title=$title")

            // Set data
            titleTextView.text = title
            contentTextView.text = content

            if (date > 0) {
                val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                dateTextView.text = sdf.format(Date(date))
            } else {
                dateTextView.text = "No date"
            }

            android.util.Log.d("DetailActivity", "Image URL: $imageUrl")
            android.util.Log.d("DetailActivity", "Thumbnail URL: $thumbnailUrl")

            if (imageUrl.isNotEmpty()) {
                try {
                    android.util.Log.d("DetailActivity", "Loading image from: $imageUrl")
                    Glide.with(this)
                        .load(imageUrl)
                        .error(R.drawable.image1)
                        .into(imageView)
                    imageView.visibility = ImageView.VISIBLE
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error loading image: ${e.message}")
                    imageView.visibility = ImageView.GONE
                }
            } else {
                android.util.Log.w("DetailActivity", "No image URL, hiding image view")
                imageView.visibility = ImageView.GONE
            }

        // Show edit and delete buttons only if user owns this journal
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPreferences.getString("userId", "") ?: ""

        if (currentUserId == userId && userId.isNotEmpty()) {
            editButton.visibility = ImageView.VISIBLE
            deleteButton.visibility = ImageView.VISIBLE

            editButton.setOnClickListener {
                val intent = Intent(this, EditJournalActivity::class.java)
                intent.putExtra("journal_id", journalId)
                intent.putExtra("journal_title", title)
                intent.putExtra("journal_content", content)
                intent.putExtra("journal_image", imageUrl)
                intent.putExtra("journal_thumbnail", thumbnailUrl)
                intent.putExtra("journal_is_public", isPublic)
                intent.putExtra("journal_user_id", userId)
                startActivityForResult(intent, 200)
            }

            deleteButton.setOnClickListener {
                showDeleteConfirmation(journalId)
            }
        }

        // Show interaction section for ALL public journals
        if (isPublic && userId.isNotEmpty()) {
            interactionSection.visibility = View.VISIBLE
            setupLikeAndComments()
            loadComments()
        }

            backButton.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            android.util.Log.e("DetailActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error loading journal: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showDeleteConfirmation(journalId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Journal")
            .setMessage("Are you sure you want to delete this journal?")
            .setPositiveButton("Delete") { _, _ ->
                deleteJournal(journalId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Journal was edited, return to refresh parent
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun deleteJournal(journalId: String) {
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "") ?: return

        apiHelper.deleteJournal(
            journalId = journalId,
            userId = userId,
            onSuccess = { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Journal deleted successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, response.optString("message", "Failed to delete"), Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupLikeAndComments() {
        // Check if this is the user's own journal
        val isOwnJournal = currentUserId == journalOwnerId

        // Always update like count display
        likeCountText.text = likeCount.toString()

        if (isOwnJournal) {
            // Hide like button for own journal, but show like count
            likeButton.visibility = View.GONE
        } else {
            // Show like button for other users' journals
            likeButton.visibility = View.VISIBLE
            updateLikeUI()

            // Like button click
            likeButton.setOnClickListener {
                toggleLike()
            }
        }

        // Always show comment count
        commentCountText.text = commentCount.toString()

        // Comment button click
        commentButton.setOnClickListener {
            commentInput.requestFocus()
        }

        // Send comment button click
        sendCommentButton.setOnClickListener {
            val commentText = commentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLikeUI() {
        likeCountText.text = likeCount.toString()
        if (isLiked) {
            likeIcon.setImageResource(R.drawable.ic_favorite)
        } else {
            likeIcon.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun toggleLike() {
        apiHelper.likeJournal(
            journalId = currentJournalId,
            userId = currentUserId,
            onSuccess = { response ->
                if (response.getBoolean("success")) {
                    isLiked = response.getBoolean("liked")
                    if (isLiked) {
                        likeCount++
                        // Send FCM notification for new like
                        sendLikeNotification()
                    } else {
                        likeCount = maxOf(0, likeCount - 1)
                    }
                    updateLikeUI()
                } else {
                    Toast.makeText(this, response.optString("message", "Failed to like"), Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun addComment(commentText: String) {
        android.util.Log.d("DetailActivity", "Adding comment for journal: $currentJournalId")
        apiHelper.addComment(
            journalId = currentJournalId,
            userId = currentUserId,
            commentText = commentText,
            onSuccess = { response ->
                android.util.Log.d("DetailActivity", "Comment response: $response")
                if (response.getBoolean("success")) {
                    commentInput.setText("")
                    commentCount++
                    commentCountText.text = commentCount.toString()
                    loadComments()
                    Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()

                    // Send FCM notification from app (bypasses AwardSpace HTTPS blocking)
                    sendCommentNotification()
                } else {
                    Toast.makeText(this, response.optString("message", "Failed to add comment"), Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                android.util.Log.e("DetailActivity", "Comment error: $error")
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadComments() {
        apiHelper.getComments(
            journalId = currentJournalId,
            onSuccess = { response ->
                try {
                    if (response.getBoolean("success")) {
                        val commentsArray = response.getJSONArray("comments")
                        val comments = mutableListOf<Comment>()

                        for (i in 0 until commentsArray.length()) {
                            val commentObj = commentsArray.getJSONObject(i)
                            comments.add(
                                Comment(
                                    id = commentObj.getInt("id"),
                                    journalId = commentObj.getInt("journal_id"),
                                    userId = commentObj.getInt("user_id"),
                                    userName = commentObj.getString("user_name"),
                                    commentText = commentObj.getString("comment_text"),
                                    createdAt = commentObj.getString("created_at")
                                )
                            )
                        }

                        commentsRecycler.layoutManager = LinearLayoutManager(this)
                        commentsRecycler.adapter = CommentAdapter(comments)
                    } else {
                        android.util.Log.d("DetailActivity", "Failed to load comments")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error parsing comments: ${e.message}")
                }
            },
            onError = { error ->
                android.util.Log.e("DetailActivity", "Error loading comments: $error")
            }
        )
    }

    /**
     * Send FCM notification for comment
     * This runs client-side to bypass AwardSpace HTTPS blocking
     */
    private fun sendCommentNotification() {
        // Only send if commenting on someone else's journal
        if (currentUserId == journalOwnerId || journalOwnerFcmToken.isEmpty()) {
            return
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUserName = sharedPreferences.getString("userName", "Someone") ?: "Someone"

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val notificationSender = NotificationSender(this@DetailActivity)
                notificationSender.sendNotification(
                    targetUserId = journalOwnerId,
                    fcmToken = journalOwnerFcmToken,
                    title = "New Comment",
                    message = "$currentUserName commented on your journal: $journalTitle",
                    data = mapOf(
                        "type" to "comment",
                        "journal_id" to currentJournalId,
                        "user_id" to currentUserId
                    )
                )
                android.util.Log.d("DetailActivity", "Comment notification sent")
            } catch (e: Exception) {
                android.util.Log.e("DetailActivity", "Error sending comment notification: ${e.message}")
            }
        }
    }

    /**
     * Send FCM notification for like
     * This runs client-side to bypass AwardSpace HTTPS blocking
     */
    private fun sendLikeNotification() {
        // Only send if liking someone else's journal
        if (currentUserId == journalOwnerId || journalOwnerFcmToken.isEmpty()) {
            return
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUserName = sharedPreferences.getString("userName", "Someone") ?: "Someone"

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val notificationSender = NotificationSender(this@DetailActivity)
                notificationSender.sendNotification(
                    targetUserId = journalOwnerId,
                    fcmToken = journalOwnerFcmToken,
                    title = "New Like",
                    message = "$currentUserName liked your journal: $journalTitle",
                    data = mapOf(
                        "type" to "like",
                        "journal_id" to currentJournalId,
                        "user_id" to currentUserId
                    )
                )
                android.util.Log.d("DetailActivity", "Like notification sent")
            } catch (e: Exception) {
                android.util.Log.e("DetailActivity", "Error sending like notification: ${e.message}")
            }
        }
    }
}
