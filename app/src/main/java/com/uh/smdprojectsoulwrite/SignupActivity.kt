package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var loginTextView: TextView

    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize API Helper
        apiHelper = ApiHelper(this)

        // Initialize views
        nameEditText = findViewById(R.id.name)
        emailEditText = findViewById(R.id.email)
        phoneEditText = findViewById(R.id.phonenumber)
        passwordEditText = findViewById(R.id.password)
        signupButton = findViewById(R.id.login_button)
        loginTextView = findViewById(R.id.login)

        signupButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInputs(name, email, phone, password)) {
                registerUser(name, email, phone, password)
            }
        }

        loginTextView.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(name: String, email: String, phone: String, password: String): Boolean {
        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            return false
        }

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid email format"
            return false
        }

        if (phone.isEmpty()) {
            phoneEditText.error = "Phone number is required"
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun registerUser(name: String, email: String, phone: String, password: String) {
        // Register with MySQL database only
        apiHelper.registerUser(name, email, phone, password,
            onSuccess = { response ->
                val success = response.getBoolean("success")
                if (success) {
                    val userId = response.getString("user_id")

                    // Save to SharedPreferences
                    val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putBoolean("isLoggedIn", true)
                        putString("userId", userId)
                        putString("userName", name)
                        putString("userEmail", email)
                        putString("userPhone", phone)
                        apply()
                    }

                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}

