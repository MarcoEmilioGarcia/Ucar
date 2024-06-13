package com.example.ucar_home.add_car

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ucar_home.CarObject
import com.example.ucar_home.CarProfileActivity
import com.example.ucar_home.ProfileActivity
import com.example.ucar_home.databinding.ActivityAddCar3Binding
import com.example.ucar_home.variables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class AddCarActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityAddCar3Binding
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCar3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val title = intent.getStringExtra("Title")
        val brand = intent.getStringExtra("Brand")
        val model = intent.getStringExtra("Model")
        val cv = intent.getStringExtra("Hp")?.toIntOrNull() ?: 0
        val cc = intent.getStringExtra("Cc")?.toIntOrNull() ?: 0
        val year = intent.getStringExtra("Year")?.toIntOrNull() ?: 0
        val fuel = intent.getStringExtra("Fuel")
        val imageUrl = intent.getStringExtra("ImageUrl")

        binding.btnCamera.setOnClickListener {
            openCamera()
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.imageBtnGoBack.setOnClickListener {
            finish()
        }

        binding.btnCreate.setOnClickListener {
            if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(variables.Email, variables.Password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val description = binding.editTextCarDescription.text.toString()
                            saveCarToDatabase(title, brand, model, cv, cc, year, fuel, imageUrl, description)
                            val intent = Intent(this, CarProfileActivity::class.java)
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

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "New Picture")
                put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            }
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            resultLauncher.launch(cameraIntent)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.data != null) {
                imageUri = data.data
                binding.imageUser.setImageURI(imageUri)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    private fun saveCarToDatabase(
        title: String?,
        brand: String?,
        model: String?,
        cv: Int?,
        cc: Int?,
        year: Int?,
        fuel: String?,
        imageUrl: String?,
        description: String?
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference

        // Verifica que el UID no sea nulo antes de continuar
        if (uid != null) {
            val car = CarObject(
                idUser = uid,
                title = title ?: "",
                brand = brand ?: "",
                model = model ?: "",
                cv = cv ?: 0,
                cc = cc ?: 0,
                year = year ?: 0,
                fuel = fuel ?: "",
                imageUrl = imageUrl ?: "",
                description = description ?: ""
            )

            // Guarda el objeto de coche en la base de datos bajo el nodo "cars" con el UID del usuario
            database.child("cars").child(uid).push().setValue(car)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "Car data saved successfully.")
                    // Proceed to next activity or whatever you need to do
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error saving car data", e)
                }
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 101
    }
}
