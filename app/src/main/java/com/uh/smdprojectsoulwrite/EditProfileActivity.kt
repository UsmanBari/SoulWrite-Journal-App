package com.uh.smdprojectsoulwrite

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        apiHelper = ApiHelper(this)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        nameEditText = findViewById(R.id.name_input)
        phoneEditText = findViewById(R.id.phone_input)
        saveButton = findViewById(R.id.save_button)

        // Load current data
        loadUserData()

        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("userName", "") ?: ""
        val phone = sharedPreferences.getString("userPhone", "") ?: ""

        nameEditText.setText(name)
        phoneEditText.setText(phone)
    }

    private fun saveProfile() {
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            return
        }

        // Save to SharedPreferences
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("userName", name)
            putString("userPhone", phone)
            apply()
        }

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}

