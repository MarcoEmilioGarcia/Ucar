package com.example.ucar_home.add_post

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.ucar_home.MainActivity
import com.example.ucar_home.PostObject
import com.example.ucar_home.profile.ProfileActivity
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivityAddPost2Binding
import com.example.ucar_home.variables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddPostActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityAddPost2Binding
    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth

    companion object {
        const val REQUEST_IMAGE_PICK = 1 // Definir constante para la solicitud de selección de imagen
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPost2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()



        binding.btnGallery.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnNext.setOnClickListener {
            if (imageUri != null) {
                uploadImageToFirebaseStorage()
            } else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this, R.color.warning))
                binding.textViewResult.text = "You have to put your name."
            }
        }

        binding.imageBtnGoBack.setOnClickListener {
            finish()
        }

    }
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun uploadImageToFirebaseStorage() {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}")
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()
        val desescription = intent.getStringExtra("Desescription")

        val uploadTask = storageReference.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageReference.downloadUrl.addOnSuccessListener { uri ->

                if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()){

                    auth.signInWithEmailAndPassword(variables.Email.toString(), variables.Password.toString()).addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {



                            Log.d(ContentValues.TAG, "Auth successfully.")
                            savePostToDatabase(desescription , uri.toString(), 0 )
                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.d(ContentValues.TAG, "User registration failed.")
                        }

                    }
                } else {
                    Log.d(ContentValues.TAG, "Email or password is empty.")
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }.addOnFailureListener { exception ->
            // Handle failure
            // You can display a toast or log the error
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                binding.imageUser.setImageURI(uri)
            }
        }
    }
    private fun savePostToDatabase(
        description: String?,
        imageUrl: String?,
        likes: Int?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        if (description != null && imageUrl != null && likes != null) {
            val postId = database.child("posts").child(uid!!).push().key // Genera un ID único
            val post = PostObject(postId!!, uid, description, imageUrl, likes)
            uid?.let {
                database.child("posts").child(it).child(postId).setValue(post)
                    .addOnSuccessListener {
                        Log.d(ContentValues.TAG, "Post data saved successfully.")
                        // Proceed to next activity or whatever you need to do
                    }
                    .addOnFailureListener { e ->
                        Log.d(ContentValues.TAG, "Error saving car data: ${e.message}")
                    }
            }
        } else {
            Log.d(ContentValues.TAG, "One of the parameters is null: description=$description, imageUrl=$imageUrl, likes=$likes")
        }
    }



}