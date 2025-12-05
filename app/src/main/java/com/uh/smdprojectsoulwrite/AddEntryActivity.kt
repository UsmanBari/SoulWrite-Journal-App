package com.uh.smdprojectsoulwrite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class AddEntryActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var publicCheckBox: CheckBox
    private lateinit var saveButton: Button

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var apiHelper: ApiHelper
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)

        dbHelper = DatabaseHelper(this)
        apiHelper = ApiHelper(this)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        titleEditText = findViewById(R.id.title_input)
        contentEditText = findViewById(R.id.content_input)
        imageView = findViewById(R.id.journal_image)
        selectImageButton = findViewById(R.id.select_image_button)
        publicCheckBox = findViewById(R.id.public_checkbox)
        saveButton = findViewById(R.id.save_button)

        backButton.setOnClickListener {
            finish()
        }

        selectImageButton.setOnClickListener {
            openImagePicker()
        }

        saveButton.setOnClickListener {
            saveJournal()
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
            imageView.setImageURI(selectedImageUri)
            imageView.visibility = ImageView.VISIBLE
        }
    }

    private fun saveJournal() {
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
        val userId = sharedPreferences.getString("userId", "") ?: ""
        val userName = sharedPreferences.getString("userName", "") ?: ""

        // Show progress
        Toast.makeText(this, "Saving journal...", Toast.LENGTH_SHORT).show()

        if (selectedImageUri != null) {
            uploadImageAndSaveJournal(userId, userName, title, content, isPublic)
        } else {
            saveJournalToDatabase(userId, userName, title, content, "", "", isPublic)
        }
    }

    private fun uploadImageAndSaveJournal(userId: String, userName: String, title: String, content: String, isPublic: Boolean) {
        selectedImageUri?.let { uri ->
            try {
                // Convert image to byte array
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Compress image
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                // Generate unique filename
                val fileName = "journal_${System.currentTimeMillis()}.jpg"

                // Upload to server
                apiHelper.uploadImage(imageBytes, fileName,
                    onSuccess = { imageUrl, thumbnailUrl ->
                        // Use returned URLs from server
                        saveJournalToDatabase(userId, userName, title, content, imageUrl, thumbnailUrl, isPublic)
                    },
                    onError = { error ->
                        Toast.makeText(this, "Failed to upload image: $error", Toast.LENGTH_SHORT).show()
                        // Save without image
                        saveJournalToDatabase(userId, userName, title, content, "", "", isPublic)
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                saveJournalToDatabase(userId, userName, title, content, "", "", isPublic)
            }
        }
    }

    private fun saveJournalToDatabase(userId: String, userName: String, title: String, content: String,
                                     imageUrl: String, thumbnailUrl: String, isPublic: Boolean) {
        val journalId = UUID.randomUUID().toString()
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

        // Save to local database first
        dbHelper.insertJournal(journal)

        // Then sync with server
        apiHelper.addJournal(userId, title, content, imageUrl, thumbnailUrl, isPublic,
            onSuccess = { response ->
                Toast.makeText(this, "Journal saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = { error ->
                // Even if server fails, local save succeeded
                Toast.makeText(this, "Journal saved locally", Toast.LENGTH_SHORT).show()
                finish()
            }
        )
    }
}

