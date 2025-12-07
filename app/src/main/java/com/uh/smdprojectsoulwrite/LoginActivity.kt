package com.uh.smdprojectsoulwrite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupTextView: TextView
    private lateinit var forgotPasswordTextView: TextView

    private lateinit var apiHelper: ApiHelper
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize helpers
        apiHelper = ApiHelper(this)
        dbHelper = DatabaseHelper(this)

        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences("SoulWritePrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            navigateToHome()
            return
        }

        // Initialize views
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)
        signupTextView = findViewById(R.id.signup)
        forgotPasswordTextView = findViewById(R.id.forgot_password)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        signupTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid email format"
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

    private fun loginUser(email: String, password: String) {
        // Authenticate with MySQL database only
        apiHelper.loginUser(email, password,
            onSuccess = { response ->
                val success = response.getBoolean("success")
                if (success) {
                    val userData = response.getJSONObject("user")
                    val userId = userData.getString("id")
                    val name = userData.getString("name")
                    val phone = userData.optString("phone", "")

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

                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    // Get and send FCM token
                    sendFCMTokenToServer(userId.toInt())

                    navigateToHome()
                } else {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, "Login failed: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun sendFCMTokenToServer(userId: Int) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("FCM", "Token: $token")

                val url = ApiHelper.UPDATE_FCM_TOKEN_URL
                val request = object : com.android.volley.toolbox.StringRequest(
                    com.android.volley.Request.Method.POST, url,
                    { response ->
                        android.util.Log.d("FCM", "Token sent to server: $response")
                    },
                    { error ->
                        android.util.Log.e("FCM", "Failed to send token: ${error.message}")
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        return mapOf(
                            "user_id" to userId.toString(),
                            "fcm_token" to token
                        )
                    }
                }
                com.android.volley.toolbox.Volley.newRequestQueue(this).add(request)
            } else {
                android.util.Log.e("FCM", "Failed to get token")
            }
        }
    }
}

//..
