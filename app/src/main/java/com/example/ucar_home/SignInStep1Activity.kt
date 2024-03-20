package com.example.ucar_login

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.ucar_login.databinding.ActivitySignInStep1Binding

class SignInStep1Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep1Binding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_step1)

        binding = ActivitySignInStep1Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        //GO BACK BUTTON
        binding.imageBtnGoBack1.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }

        //NEXT BUTTON
        binding.btnNext.setOnClickListener {

            if(binding.editTextPassword.text.toString()==binding.editTextRepeatPassword.text.toString()){

                if(binding.editTextUsername.text.toString().isNotEmpty() && binding.editTextPassword.text.toString().isNotEmpty()){

                    if (verificarCriterios(binding.editTextPassword.text.toString())) {

                        val intent = Intent(this, SignInStep2Activity::class.java)
                        intent.putExtra("Username",binding.editTextUsername.text.toString())
                        intent.putExtra("Password",binding.editTextPassword.text.toString())
                        startActivity(intent)

                    } else {
                        binding.textViewResult.setTextColor(ContextCompat.getColor(this,R.color.warning))
                        binding.textViewResult.text = "Password must have 8 caracters, numbers and capital letters on it."
                    }
                } else {
                    binding.textViewResult.setTextColor(ContextCompat.getColor(this,R.color.warning))
                    binding.textViewResult.text = "There cannot be empty fields."
                }
            }else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this,R.color.warning))
                binding.textViewResult.text = "Password must be the same."
            }
        }
    }

    private fun verificarCriterios(cadena: String): Boolean {
        // Verificar longitud mayor a 8 caracteres
        if (cadena.length < 8) {
            return false
        }
        // Verificar si contiene al menos una letra minúscula
        if (!cadena.any { it.isLowerCase() }) {
            return false
        }
        // Verificar si contiene al menos una letra mayúscula
        if (!cadena.any { it.isUpperCase() }) {
            return false
        }
        // Verificar si contiene al menos un dígito
        if (!cadena.any { it.isDigit() }) {
            return false
        }

        return true
    }
}