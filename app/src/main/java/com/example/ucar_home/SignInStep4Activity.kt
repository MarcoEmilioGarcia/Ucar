package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        binding.btnCreate.setOnClickListener {
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                auth.createUserWithEmailAndPassword(email.toString(), password.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val bibliography = binding.editTextBibliography.text.toString()
                            Log.d(ContentValues.TAG, "User registered successfully.")
                            saveUserToDatabase(username, email, phoneNumber, name, imageUrl, bibliography,0,0)
                            variables.Email = email
                            variables.Password = password



                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.d(ContentValues.TAG, "User registration failed.")
                        }
                    }
            } else {
                Log.d(ContentValues.TAG, "Email or password is empty.")
            }
        }
    }

    private fun saveUserToDatabase(
        username: String?,
        email: String?,
        phoneNumber: String?,
        name: String?,
        imageUrl: String?,
        bibliography: String?,
        followers: Int?,
        following: Int?
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference

        // Inicializa las listas de seguidores y seguidos vacías
        val followersList = emptyList<String>()
        val followingList = emptyList<String>()

        val user = User(
            username = username!!,
            email = email!!,
            phoneNumber = phoneNumber!!,
            name = name!!,
            imageUrl = imageUrl!!,
            bibliography = bibliography!!,
            followers = followers ?: 0,
            following = following ?: 0,
            followersList = followersList,
            followingList = followingList
        )

        uid?.let {
            database.child("users").child(it).setValue(user)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "User data saved successfully.")
                    // Proceed to next activity or whatever you need to do
                }
                .addOnFailureListener { e ->
                    Log.d(ContentValues.TAG, "Error saving user data: ${e.message}")
                }
        }
    }



}
