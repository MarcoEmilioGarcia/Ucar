package com.example.ucar_home

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ucar_home.databinding.ActivitySignInStep3Binding
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class SignInStep3Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep3Binding
    private var imageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val PERMISSION_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInStep3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.getStringExtra("Username")
        val password = intent.getStringExtra("Password")
        val email = intent.getStringExtra("Email")
        val phoneNumber = intent.getStringExtra("PhoneNumber")

        // Configurar el botón Go Back
        binding.imageBtnGoBack.setOnClickListener {
            val intent = Intent(this, SignInStep2Activity::class.java)
            startActivity(intent)
        }

        binding.btnGallery.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnCamera.setOnClickListener {
            if (checkAndRequestPermissions()) {
                openCamera()
            }
        }

        binding.btnNext.setOnClickListener {
            val name = binding.editTextName.text.toString()

            if (name.isNotEmpty()) {
                uploadImageToFirebaseStorage(username, password, email, phoneNumber, name)
            } else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this, R.color.warning))
                binding.textViewResult.text = "You have to put your name."
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writeExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val listPermissionsNeeded = mutableListOf<String>()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (writeExternalPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }
        return true
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun uploadImageToFirebaseStorage(username: String?, password: String?, email: String?, phoneNumber: String?, name: String) {
        if (imageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}")
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            val uploadTask = storageReference.putBytes(imageData)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val intent = Intent(this, SignInStep4Activity::class.java).apply {
                        putExtra("Username", username)
                        putExtra("Password", password)
                        putExtra("Email", email)
                        putExtra("PhoneNumber", phoneNumber)
                        putExtra("Name", name)
                        putExtra("ImageUrl", uri.toString())
                    }
                    startActivity(intent)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            val intent = Intent(this, SignInStep4Activity::class.java).apply {
                putExtra("Username", username)
                putExtra("Password", password)
                putExtra("Email", email)
                putExtra("PhoneNumber", phoneNumber)
                putExtra("Name", name)
            }
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                binding.imageUser.setImageURI(uri)
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageUri = getImageUriFromBitmap(imageBitmap)
            binding.imageUser.setImageBitmap(imageBitmap)
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "IMG_${System.currentTimeMillis()}", null)
        return Uri.parse(path)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val perms = HashMap<String, Int>()
            perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
            perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED

            for (i in permissions.indices) {
                perms[permissions[i]] = grantResults[i]
            }

            if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Some permissions are not granted. Ask again.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
