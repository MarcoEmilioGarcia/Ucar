package com.example.ucar_login

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ucar_login.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        //GO BACK BUTTON
        binding.imageBtnGoBack.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }

        //SEND BUTTON
        binding.btnSend.setOnClickListener {
            if(binding.editTextEmail.text.toString().isNotEmpty()){
                binding.textViewResult.text = "We already send you a new password, check you email account. This can take few minutes."

                //Insertar aqui funcion que envie un correo con la nueva contraseña.-------------------------------------------------------------------------

            } else {
                binding.textViewResult.text = "There cannot be empty fields."
            }
        }
    }
}