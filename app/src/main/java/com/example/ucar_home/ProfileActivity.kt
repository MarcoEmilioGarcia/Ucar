package com.example.ucar_home

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.auth.User


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }

    class ProfileActivity : AppCompatActivity() {

        private lateinit var databaseReference: DatabaseReference
        private lateinit var auth: FirebaseAuth

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_profile)

            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val email = prefs.getString("email",null)
            auth = FirebaseAuth.getInstance()
            databaseReference = FirebaseDatabase.getInstance().reference

            val currentUser: FirebaseUser? = auth.currentUser
            val uid: String = currentUser?.uid ?: ""

            val userReference: DatabaseReference = databaseReference.child("users").child(uid)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        val user = dataSnapshot.getValue(User::class.java)
                        // Aquí puedes utilizar los datos del usuario como lo necesites
                        Log.d(TAG, "Username: ${user?.username}")
                        Log.d(TAG, "Email: ${user?.email}")
                        Log.d(TAG, "Phone Number: ${user?.phoneNumber}")
                        Log.d(TAG, "Name: ${user?.name}")
                        Log.d(TAG, "Image URL: ${user?.imageUrl}")
                        Log.d(TAG, "Bibliography: ${user?.bibliography}")
                    } else {
                        Log.d(TAG, "User data does not exist")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${databaseError.message}")
                }
            })
        }

        companion object {
            private const val TAG = "ProfileActivity"
        }
    }



}