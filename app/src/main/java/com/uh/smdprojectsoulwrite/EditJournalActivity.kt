package com.uh.smdprojectsoulwrite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditJournalActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var saveButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var publicCheckBox: CheckBox
    private lateinit var imagePreview: ImageView
    private lateinit var uploadButton: Button
    private lateinit var removeImageButton: Button

    private lateinit var apiHelper: ApiHelper
    private lateinit var dbHelper: DatabaseHelper

    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String = ""
    private var currentThumbnailUrl: String = ""
    private var journalId: String = ""
    private var userId: String = ""
    private var removeImage: Boolean = false

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_journal)

        apiHelper = ApiHelper(this)
        dbHelper = DatabaseHelper(this)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        saveButton = findViewById(R.id.save_button)
        titleEditText = findViewById(R.id.title_input)
        contentEditText = findViewById(R.id.content_input)
        publicCheckBox = findViewById(R.id.public_checkbox)
        imagePreview = findViewById(R.id.image_preview)
        uploadButton = findViewById(R.id.upload_image_button)
        removeImageButton = findViewById(R.id.remove_image_button)

        // Get journal data from intent
        journalId = intent.getStringExtra("journal_id") ?: ""
        val title = intent.getStringExtra("journal_title") ?: ""
        val content = intent.getStringExtra("journal_content") ?: ""
        currentImageUrl = intent.getStringExtra("journal_image") ?: ""
        currentThumbnailUrl = intent.getStringExtra("journal_thumbnail") ?: ""
        val isPublic = intent.getBooleanExtra("journal_is_public", false)
        userId = intent.getStringExtra("journal_user_id") ?: ""

        // Set existing data
        titleEditText.setText(title)
        contentEditText.setText(content)
        publicCheckBox.isChecked = isPublic

        if (currentImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(currentImageUrl)
                .into(imagePreview)
            imagePreview.visibility = ImageView.VISIBLE
            removeImageButton.visibility = Button.VISIBLE
        }

        backButton.setOnClickListener {
            finish()
        }

        uploadButton.setOnClickListener {
            openImagePicker()
        }

        removeImageButton.setOnClickListener {
            removeImage = true
            selectedImageUri = null
            currentImageUrl = ""
            currentThumbnailUrl = ""
            imagePreview.visibility = ImageView.GONE
            removeImageButton.visibility = Button.GONE
            Toast.makeText(this, "Image will be removed when you save", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            updateJournal()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                imagePreview.setImageURI(it)
                imagePreview.visibility = ImageView.VISIBLE
                removeImageButton.visibility = Button.VISIBLE
                removeImage = false
            }
        }
    }

    private fun updateJournal() {
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val isPublic = publicCheckBox.isChecked

        if (title.isEmpty()) {
            titleEditText.error = "Title is required"
            return
        }

        if (content.isEmpty()) {
            contentEditText.error = "Content is required"
            return
        }

        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("userId", "") ?: ""
        val userName = sharedPreferences.getString("userName", "") ?: ""

        if (currentUserId != userId) {
            Toast.makeText(this, "You can only edit your own journals", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Updating journal...", Toast.LENGTH_SHORT).show()

        if (selectedImageUri != null) {
            // New image selected, upload it first
            uploadImageAndUpdateJournal(userId, userName, title, content, isPublic)
        } else if (removeImage) {
            // Image removed, update with empty URLs
            saveUpdatedJournal(userId, userName, title, content, "", "", isPublic)
        } else {
            // No change to image, keep existing URLs
            saveUpdatedJournal(userId, userName, title, content, currentImageUrl, currentThumbnailUrl, isPublic)
        }
    }

    private fun uploadImageAndUpdateJournal(userId: String, userName: String, title: String, content: String, isPublic: Boolean) {
        selectedImageUri?.let { uri ->
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                val fileName = "journal_${System.currentTimeMillis()}.jpg"

                apiHelper.uploadImage(imageBytes, fileName,
                    onSuccess = { imageUrl, thumbnailUrl ->
                        saveUpdatedJournal(userId, userName, title, content, imageUrl, thumbnailUrl, isPublic)
                    },
                    onError = { error ->
                        Toast.makeText(this, "Failed to upload image: $error", Toast.LENGTH_SHORT).show()
                        // Save with old image URLs
                        saveUpdatedJournal(userId, userName, title, content, currentImageUrl, currentThumbnailUrl, isPublic)
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                saveUpdatedJournal(userId, userName, title, content, currentImageUrl, currentThumbnailUrl, isPublic)
            }
        }
    }

    private fun saveUpdatedJournal(userId: String, userName: String, title: String, content: String,
                                    imageUrl: String, thumbnailUrl: String, isPublic: Boolean) {
        val currentTime = System.currentTimeMillis()

        val journal = Journal(
            id = journalId,
            userId = userId,
            title = title,
            content = content,
            imageUrl = imageUrl,
            thumbnailUrl = thumbnailUrl,
            date = currentTime,
            userName = userName,
            isPublic = isPublic
        )

        // Update local database
        dbHelper.updateJournal(journal)

        // Update on server
        apiHelper.updateJournal(journalId, userId, title, content, imageUrl, thumbnailUrl, isPublic,
            onSuccess = { response ->
                Toast.makeText(this, "Journal updated successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            },
            onError = { error ->
                Toast.makeText(this, "Updated locally", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        )
    }
}

//..
