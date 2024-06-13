package com.example.ucar_home.add_car

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivityAddCar2Binding
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddCarActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityAddCar2Binding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCar2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from intent
        val title = intent.getStringExtra("Title") ?: ""
        val brand = intent.getStringExtra("Brand") ?: ""
        val model = intent.getStringExtra("Model") ?: ""
        val hp = intent.getStringExtra("Hp") ?: ""
        val fuel = intent.getStringExtra("Fuel") ?: ""
        val cyclindrated = intent.getStringExtra("Cyclindrated") ?: ""
        val year = intent.getStringExtra("Year") ?: ""




        binding.btnNext.setOnClickListener {
            if (areFieldsValid()) {
                Intent(this, AddCarActivity3::class.java).apply {
                    putExtra("Title", title)
                    putExtra("Brand", brand)
                    putExtra("Model", model)
                    putExtra("Hp", binding.editCV.text.toString())
                    putExtra("Cc", binding.editcyclindrated.text.toString())
                    putExtra("Fuel", binding.editFuel.text.toString())
                    putExtra("Year", binding.edityear.text.toString())

                    startActivity(this)
                }
            } else {
                showValidationError()
            }
        }

        binding.imageBtnGoBack.setOnClickListener {
            finish()
        }
    }

    private fun areFieldsValid(): Boolean {
        return binding.editCV.text.toString().isNotEmpty() &&
                binding.editcyclindrated.text.toString().isNotEmpty() &&
                binding.editFuel.text.toString().isNotEmpty() &&
                binding.edityear.text.toString().isNotEmpty()
    }

    private fun showValidationError() {
        binding.textViewResult.apply {
            setTextColor(ContextCompat.getColor(this@AddCarActivity2, R.color.warning))
            text = "Fill in all the fields."
        }
    }
}
