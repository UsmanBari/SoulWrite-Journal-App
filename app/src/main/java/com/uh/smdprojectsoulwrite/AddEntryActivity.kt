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
        private const val PERMISSION_REQUEST_CODE = 100
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
        // Check for permission on Android 6.0+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
                return
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6-12 uses READ_EXTERNAL_STORAGE
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
                return
            }
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show()
            }
        }
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

                if (bitmap == null) {
                    Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show()
                    saveJournalToDatabase(userId, userName, title, content, "", "", isPublic)
                    return
                }

                // Compress image
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                android.util.Log.d("AddEntry", "Image size: ${imageBytes.size} bytes")

                // Generate unique filename
                val fileName = "journal_${System.currentTimeMillis()}.jpg"

                Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

                // Upload to server
                apiHelper.uploadImage(imageBytes, fileName,
                    onSuccess = { imageUrl, thumbnailUrl ->
                        android.util.Log.d("AddEntry", "Upload success: $imageUrl")
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                        // Use returned URLs from server
                        saveJournalToDatabase(userId, userName, title, content, imageUrl, thumbnailUrl, isPublic)
                    },
                    onError = { error ->
                        android.util.Log.e("AddEntry", "Upload failed: $error")
                        Toast.makeText(this, "Image upload failed. Saving without image.", Toast.LENGTH_LONG).show()
                        // Save without image
                        saveJournalToDatabase(userId, userName, title, content, "", "", isPublic)
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AddEntry", "Error processing image: ${e.message}")
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

