package com.example.projectplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.projectplanner.ui.home.HomeFragment
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    // Temporary storage for demo purposes
    private var registeredEmail: String? = null
    private var registeredPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Make sure this matches your XML file name

        // Initialize views
        val btnLoginForm = findViewById<Button>(R.id.btnLoginForm)
        val btnSignupForm = findViewById<Button>(R.id.btnSignupForm)
        val loginForm = findViewById<LinearLayout>(R.id.loginForm)
        val signupForm = findViewById<LinearLayout>(R.id.signupForm)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        // Set initial state (login form visible)
        btnLoginForm.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        btnSignupForm.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Toggle between login and signup forms
        btnLoginForm.setOnClickListener {
            loginForm.visibility = View.VISIBLE
            signupForm.visibility = View.GONE
            btnLoginForm.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            btnSignupForm.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        btnSignupForm.setOnClickListener {
            loginForm.visibility = View.GONE
            signupForm.visibility = View.VISIBLE
            btnLoginForm.setTextColor(ContextCompat.getColor(this, R.color.black))
            btnSignupForm.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }

        // Handle login button click
        btnLogin.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.etLoginEmail).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.etLoginPassword).text.toString().trim()

            if (validateLogin(email, password)) {
                navigateToHome()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle signup button click
        btnSignup.setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.etSignupName).text.toString().trim()
            val email = findViewById<TextInputEditText>(R.id.etSignupEmail).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.etSignupPassword).text.toString().trim()
            val confirmPassword = findViewById<TextInputEditText>(R.id.etSignupConfirmPassword).text.toString().trim()

            if (validateSignup(name, email, password, confirmPassword)) {
                // Store credentials (in a real app, use SharedPreferences or database)
                registeredEmail = email
                registeredPassword = password

                // Show success message and switch to login form
                Toast.makeText(this, "Registration successful! Please login", Toast.LENGTH_SHORT).show()
                loginForm.visibility = View.VISIBLE
                signupForm.visibility = View.GONE
                btnLoginForm.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                btnSignupForm.setTextColor(ContextCompat.getColor(this, R.color.black))

                // Auto-fill the login form
                findViewById<TextInputEditText>(R.id.etLoginEmail).setText(email)
                findViewById<TextInputEditText>(R.id.etLoginPassword).setText(password)
            }
        }
    }

    private fun validateLogin(email: String, password: String): Boolean {
        // For demo purposes, check against registered credentials
        // In a real app, you would verify against a database
        if (email.isEmpty() || password.isEmpty()) {
            return false
        }

        // Check if credentials match registered ones (or demo credentials)
        return (email == registeredEmail && password == registeredPassword) ||
                (email == "demo@example.com" && password == "password") // Demo credentials
    }

    private fun validateSignup(name: String, email: String, password: String, confirmPassword: String): Boolean {
        // Basic validation
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun navigateToHome() {
        // Start the home activity (which hosts your fragment_home)
        val intent = Intent(this, navigate::class.java)
        startActivity(intent)
    }
}