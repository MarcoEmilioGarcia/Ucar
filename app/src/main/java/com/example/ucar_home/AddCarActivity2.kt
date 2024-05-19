package com.example.ucar_home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ucar_home.databinding.ActivityAddCar2Binding
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddCarActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityAddCar2Binding
    private var imageUri: Uri? = null

    companion object {
        const val REQUEST_IMAGE_PICK = 1 // Definir constante para la solicitud de selección de imagen
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCar2Binding.inflate(layoutInflater)
        setContentView(binding.root)

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

                val title = intent.getStringExtra("Title")
                val brand = intent.getStringExtra("Brand")
                val model = intent.getStringExtra("Model")
                val cv = binding.editCV.text.toString().toIntOrNull() ?: 0
                val cc = binding.editcyclindrated.text.toString().toIntOrNull() ?: 0
                val fuel = binding.editFuel.text.toString()
                val year = binding.edityear.text.toString().toIntOrNull() ?: 0

                val intent = Intent(this, AddCarActivity3::class.java)
                intent.putExtra("Title", title)
                intent.putExtra("Brand", brand)
                intent.putExtra("Model", model)
                intent.putExtra("Cv", cv)
                intent.putExtra("Cc", cc)
                intent.putExtra("Fuel", fuel)
                intent.putExtra("Year", year)
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
}
