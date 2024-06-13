package com.example.ucar_home.sign_in

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ucar_home.LogInActivity
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivitySignInStep3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class SignInStep3Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep3Binding
    private var imageUri: Uri? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInStep3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestPermissions()

        setupUI()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    private fun setupUI() {
        binding.imageBtnGoBack.setOnClickListener {
            finish()
        }

        binding.btnGallery.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnCamera.setOnClickListener {
            captureImageFromCamera()
        }

        binding.btnNext.setOnClickListener {
            handleNextButtonClick()
        }
    }

    private fun handleNextButtonClick() {
        if (binding.editTextName.text.toString().isNotEmpty()) {
            if (imageUri == null) {
                // Set default image if no image is selected
                imageUri = Uri.parse("android.resource://${packageName}/${R.drawable.image_photo}")
            }
            uploadImageToFirebaseStorage()
        } else {
            showErrorMessage(R.string.error_name_required)
        }
    }

    private fun showErrorMessage(messageResId: Int) {
        binding.textViewResult.apply {
            setTextColor(ContextCompat.getColor(this@SignInStep3Activity, R.color.warning))
            text = getString(messageResId)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun captureImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
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
                proceedToNextStep(uri.toString())
            }
        }.addOnFailureListener { exception ->
            Log.e(ContentValues.TAG, "Error al cargar la imagen", exception)
        }
    }

    private fun proceedToNextStep(imageUrl: String) {
        val username = intent.getStringExtra("Username")
        val password = intent.getStringExtra("Password")
        val email = intent.getStringExtra("Email")
        val phoneNumber = intent.getStringExtra("PhoneNumber")
        val name = binding.editTextName.text.toString()

        val intent = Intent(this, SignInStep4Activity::class.java).apply {
            putExtra("Username", username)
            putExtra("Password", password)
            putExtra("Email", email)
            putExtra("PhoneNumber", phoneNumber)
            putExtra("Name", name)
            putExtra("ImageUrl", imageUrl)
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let { uri ->
                        imageUri = uri
                        binding.imageUser.setImageURI(uri)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imageUri = getImageUriFromBitmap(imageBitmap)
                    binding.imageUser.setImageBitmap(imageBitmap)
                }
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_PERMISSIONS = 102
    }
}
