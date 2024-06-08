package com.example.ucar_home.add_post

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivityAddPostBinding

class AddPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnNext.setOnClickListener {

            if(binding.editTextDescription.text.toString().isNotEmpty() ){

                val intent = Intent(this, AddPostActivity2::class.java)
                intent.putExtra("Desescription",binding.editTextDescription.text.toString())
                startActivity(intent)

            }else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this, R.color.warning))
                binding.textViewResult.text = "Fill in all the fields."
            }
        }



    }
}
