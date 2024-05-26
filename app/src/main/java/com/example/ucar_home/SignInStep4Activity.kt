package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ucar_home.databinding.ActivitySignInStep4Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignInStep4Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep4Binding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInStep4Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val username = intent.getStringExtra("Username")
        val password = intent.getStringExtra("Password")
        val email = intent.getStringExtra("Email")
        val phoneNumber = intent.getStringExtra("PhoneNumber")
        val name = intent.getStringExtra("Name")
        val imageUrl = intent.getStringExtra("ImageUrl")

        // Verificar que los datos obligatorios no sean nulos
        if (username.isNullOrEmpty() || password.isNullOrEmpty() || email.isNullOrEmpty() || name.isNullOrEmpty()) {
            showToast("Some data is missing. Please go back and fill all the fields.")
            val intent = Intent(this, SignInStep3Activity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Configurar el botón Go Back
        binding.imageBtnGoBack.setOnClickListener {
            val intent = Intent(this, SignInStep3Activity::class.java)
            startActivity(intent)
        }

        binding.btnCreate.setOnClickListener {
            if (isInputValid(email, password)) {
                registerUser(email, password, username, phoneNumber, name, imageUrl)
            } else {
                showToast("Email or password is empty.")
            }
        }
    }

    private fun isInputValid(email: String?, password: String?): Boolean {
        return !email.isNullOrEmpty() && !password.isNullOrEmpty()
    }

    private fun registerUser(
        email: String,
        password: String,
        username: String,
        phoneNumber: String?,
        name: String,
        imageUrl: String?
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val bibliography = binding.editTextBibliography.text.toString()
                    Log.d(TAG, "User registered successfully.")
                    saveUserToDatabase(username, email, phoneNumber, name, imageUrl, bibliography)
                    navigateToMainActivity(email, password)
                } else {
                    Log.d(TAG, "User registration failed.", task.exception)
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserToDatabase(
        username: String,
        email: String,
        phoneNumber: String?,
        name: String,
        imageUrl: String?,
        bibliography: String?
    ) {
        val uid = auth.currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val user = User(username, email, phoneNumber.orEmpty(), name, imageUrl.orEmpty(), bibliography.orEmpty())
        uid?.let {
            database.child("users").child(it).setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG, "User data saved successfully.")
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Error saving user data: ${e.message}")
                    showToast("Error saving user data: ${e.message}")
                }
        }
    }

    private fun navigateToMainActivity(email: String, password: String) {
        variables.Email = email
        variables.Password = password
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "SignInStep4Activity"
    }
}
