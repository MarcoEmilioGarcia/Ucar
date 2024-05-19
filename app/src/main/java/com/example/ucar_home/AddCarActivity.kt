package com.example.ucar_home

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.ucar_home.databinding.ActivityAddCar1Binding



class AddCarActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAddCar1Binding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCar1Binding.inflate(layoutInflater)
        setContentView(binding.root)


        //NEXT BUTTON
        binding.btnNext.setOnClickListener {

            if(binding.editTextTitle.text.toString().isNotEmpty() && binding.editTextbrand.text.toString().isNotEmpty() && binding.editTextmodel.text.toString().isNotEmpty()){

                        val intent = Intent(this, AddCarActivity2::class.java)
                        intent.putExtra("Title",binding.editTextTitle.text.toString())
                        intent.putExtra("Brand",binding.editTextbrand.text.toString())
                        intent.putExtra("Model",binding.editTextmodel.text.toString())
                        startActivity(intent)


            }else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this,R.color.warning))
                binding.textViewResult.text = "Fill in all the fields."
            }
        }
    }
}
