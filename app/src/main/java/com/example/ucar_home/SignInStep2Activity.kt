package com.example.ucar_login

import SignInStep3Activity
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.ucar_login.databinding.ActivitySignInStep2Binding

class SignInStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep2Binding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_step2)

        binding = ActivitySignInStep2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val username = intent.getStringExtra("Username")
        val password = intent.getStringExtra("Password")




        //GO BACK BUTTON
        binding.imageBtnGoBack.setOnClickListener {
            val intent = Intent(this, SignInStep1Activity::class.java)
            startActivity(intent)
        }

        //NEXT BUTTON
        binding.btnNext.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            if (email != null && email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                val phoneNumber = binding.editTextPhoneNumber.text.toString()
                val intent = Intent(this, SignInStep3Activity::class.java)
                intent.putExtra("Username", username)
                intent.putExtra("Password", password)
                intent.putExtra("Email", email)
                intent.putExtra("PhoneNumber", phoneNumber)
                Log.d(ContentValues.TAG, "Pasando a la siguiente actividad con la variable: " + email+ " y "+ password)
                startActivity(intent)
            } else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this, R.color.warning))
                binding.textViewResult.text = "You must provide a valid email address."
            }

        }
    }
}