package com.example.ucar_home.add_post

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
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
        const val REQUEST_IMAGE_PICK = 1
        const val REQUEST_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPost2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val carId = intent.getStringExtra("carId")
        carId?.let { Log.d(ContentValues.TAG, it) }

        // Verificar permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }

        binding.btnGallery.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnNext.setOnClickListener {
            if (imageUri != null) {
                uploadImageToFirebaseStorage(carId)
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

    private fun uploadImageToFirebaseStorage(carId: String?) {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}")
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()
        val description = intent.getStringExtra("Description")

        val uploadTask = storageReference.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                if (!variables.Email.isNullOrEmpty() && !variables.Password.isNullOrEmpty()) {
                    auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(ContentValues.TAG, "Auth successfully.")
                            savePostToDatabase(description, uri.toString(), 0, carId)
                            Log.d(ContentValues.TAG, "se supone que se subio")
                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.d(ContentValues.TAG, "User registration failed.")
                        }
                    }
                } else {
                    Log.d(ContentValues.TAG, "Email or password is empty.")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(ContentValues.TAG, "Upload failed", exception)
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

    private fun savePostToDatabase(description: String?, imageUrl: String?, likes: Int?, carId: String?) {
        Log.d(ContentValues.TAG, "Traza 1: Entrando en savePostToDatabase")

        if (carId == null) {
            Log.d(ContentValues.TAG, "Traza 1.1: carId es nulo")
        } else {
            Log.d(ContentValues.TAG, "Traza 1.1: carId = $carId")
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(ContentValues.TAG, "Traza 1.2: uid = $uid")

        val database = FirebaseDatabase.getInstance().reference
        if (description != null && imageUrl != null && likes != null && carId != null) {
            Log.d(ContentValues.TAG, "Traza 2: Todos los parámetros no son nulos")
            val postId = database.child("posts").push().key
            Log.d(ContentValues.TAG, "Traza 2.1: postId = $postId")

            if (postId != null) {
                val post = hashMapOf(
                    "idPost" to postId,
                    "idUser" to uid,
                    "idCar" to carId,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "likes" to likes
                )
                uid?.let {
                    Log.d(ContentValues.TAG, "Traza 3: uid no es nulo")
                    database.child("posts").child(postId).setValue(post)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Post data saved successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.d(ContentValues.TAG, "Error saving post data: ${e.message}")
                        }
                }
            } else {
                Log.d(ContentValues.TAG, "Error: postId es nulo")
            }
        } else {
            if (description == null) Log.d(ContentValues.TAG, "Traza 4: description es nulo")
            if (imageUrl == null) Log.d(ContentValues.TAG, "Traza 5: imageUrl es nulo")
            if (likes == null) Log.d(ContentValues.TAG, "Traza 6: likes es nulo")
            if (carId == null) Log.d(ContentValues.TAG, "Traza 7: carId es nulo")

            Log.d(ContentValues.TAG, "One of the parameters is null: description=$description, imageUrl=$imageUrl, likes=$likes, carId=$carId")
        }
    }
}
