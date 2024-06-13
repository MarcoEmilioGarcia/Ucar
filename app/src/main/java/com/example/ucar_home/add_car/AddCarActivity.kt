package com.example.ucar_home.add_car

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivityAddCar1Binding

class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCar1Binding
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCar1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            if (areFieldsValid()) {
                navigateToAddCarActivity2()
            } else {
                showValidationError()
            }
        }

        binding.imageBtnGoBack.setOnClickListener {
            finish()
        }

    }

    private fun areFieldsValid(): Boolean {
        return binding.editTextTitle.text.toString().isNotEmpty() &&
                binding.editTextbrand.text.toString().isNotEmpty() &&
                binding.editTextmodel.text.toString().isNotEmpty()
    }

    private fun navigateToAddCarActivity2() {
        Intent(this, AddCarActivity2::class.java).apply {
            putExtra("Title", binding.editTextTitle.text.toString())
            putExtra("Brand", binding.editTextbrand.text.toString())
            putExtra("Model", binding.editTextmodel.text.toString())

            startActivity(this)
        }
    }

    private fun showValidationError() {
        binding.textViewResult.apply {
            setTextColor(ContextCompat.getColor(this@AddCarActivity, R.color.warning))
            text = "Fill in all the fields."
        }
    }
}
