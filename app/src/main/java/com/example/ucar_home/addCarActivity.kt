package com.example.ucar_home

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

class addCarActivity : AppCompatActivity() {

    private lateinit var binding: addCarActivity
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_car)


/*

    //RECOGER IMAGENES
    //RECOGER TEXTO DESCRIPTIVO

    //SUBIR A BBDD REMOTA


        binding = activity_add_car.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGallery.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnNext.setOnClickListener {
            if (imageUri != null && binding.editTextName.text.toString().isNotEmpty()) {
                uploadImageToFirebaseStorage()
            } else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this, R.color.warning))
                binding.textViewResult.text = "You have to select an image and put your name."
            }
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

        val uploadTask = storageReference.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val username = intent.getStringExtra("Username")
                val password = intent.getStringExtra("Password")
                val email = intent.getStringExtra("Email")
                val phoneNumber = intent.getStringExtra("PhoneNumber")
                val name = binding.editTextName.text.toString()

                val intent = Intent(this, SignInStep4Activity::class.java)
                intent.putExtra("Username", username)
                intent.putExtra("Password", password)
                intent.putExtra("Email", email)
                intent.putExtra("PhoneNumber", phoneNumber)
                intent.putExtra("Name", name)
                intent.putExtra("ImageUrl", uri.toString())
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

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }*/
}
}