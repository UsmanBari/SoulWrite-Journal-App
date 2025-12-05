package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private lateinit var homeIcon: android.widget.LinearLayout
    private lateinit var alertsIcon: android.widget.LinearLayout
    private lateinit var profileIcon: android.widget.LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        // Initialize views
        backButton = findViewById(R.id.back_button)
        profileImage = findViewById(R.id.profile_image)
        nameTextView = findViewById(R.id.profile_name)
        emailTextView = findViewById(R.id.profile_email)
        phoneTextView = findViewById(R.id.profile_phone)
        editProfileButton = findViewById(R.id.edit_profile_button)
        changePasswordButton = findViewById(R.id.change_password_button)
        logoutButton = findViewById(R.id.logout_button)
        homeIcon = findViewById(R.id.home_icon)
        alertsIcon = findViewById(R.id.alerts_icon)
        profileIcon = findViewById(R.id.profile_icon)

        // Load user data
        loadUserData()

        backButton.setOnClickListener {
            finish()
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        logoutButton.setOnClickListener {
            logout()
        }

        homeIcon.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        alertsIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        profileIcon.setOnClickListener {
            // Already on profile, do nothing or refresh
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("userName", "User") ?: "User"
        val email = sharedPreferences.getString("userEmail", "") ?: ""
        val phone = sharedPreferences.getString("userPhone", "") ?: ""

        nameTextView.text = name
        emailTextView.text = email
        phoneTextView.text = phone
    }

    private fun logout() {

        // Clear SharedPreferences
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

