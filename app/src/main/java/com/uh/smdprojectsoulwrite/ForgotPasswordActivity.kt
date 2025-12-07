package com.uh.smdprojectsoulwrite

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var backButton: ImageView
    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        apiHelper = ApiHelper(this)

        emailEditText = findViewById(R.id.email)
        resetButton = findViewById(R.id.reset_button)
        backButton = findViewById(R.id.back_button)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Invalid email format"
                return@setOnClickListener
            }

            resetPassword(email)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun resetPassword(email: String) {
        // Show message - implement server-side email sending via PHP
        Toast.makeText(
            this,
            "Password reset instructions will be sent to $email",
            Toast.LENGTH_LONG
        ).show()
        finish()

        // TODO: Implement server-side password reset email via PHP
    }
}

//..
