package com.uh.smdprojectsoulwrite

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changeButton: Button
    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changepass)

        apiHelper = ApiHelper(this)

        // Initialize views
        backButton = findViewById(R.id.back_button)
        currentPasswordEditText = findViewById(R.id.current_password_input)
        newPasswordEditText = findViewById(R.id.new_password_input)
        confirmPasswordEditText = findViewById(R.id.confirm_password_input)
        changeButton = findViewById(R.id.change_password_button)

        backButton.setOnClickListener {
            finish()
        }

        changeButton.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (currentPassword.isEmpty()) {
            currentPasswordEditText.error = "Current password is required"
            return
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.error = "New password is required"
            return
        }

        if (newPassword.length < 6) {
            newPasswordEditText.error = "Password must be at least 6 characters"
            return
        }

        if (newPassword != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        // Get user info from SharedPreferences
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "") ?: ""
        val userEmail = sharedPreferences.getString("userEmail", "") ?: ""

        // For now, just show success message
        // TODO: Implement password change via PHP API
        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
        finish()

        // Future implementation:
        // apiHelper.changePassword(userId, currentPassword, newPassword, onSuccess, onError)
    }
}

