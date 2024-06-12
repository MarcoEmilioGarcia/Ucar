package com.example.ucar_home.sign_in

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.ucar_home.LogInActivity
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivitySignInStep1Binding

class SignInStep1Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Go Back Button
        binding.imageBtnGoBack1.setOnClickListener {
            finish()
        }

        // Next Button
        binding.btnNext.setOnClickListener {
            handleNextButtonClick()
        }
    }

    private fun handleNextButtonClick() {
        val username = binding.editTextUsername.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val repeatPassword = binding.editTextRepeatPassword.text.toString().trim()

        if (!areFieldsValid(username, password, repeatPassword)) return

        val intent = Intent(this, SignInStep2Activity::class.java).apply {
            putExtra("Username", username)
            putExtra("Password", password)
        }
        startActivity(intent)
    }

    private fun areFieldsValid(username: String, password: String, repeatPassword: String): Boolean {
        return when {
            username.isEmpty() || password.isEmpty() -> {
                showMessage(R.string.error_empty_fields)
                false
            }
            password != repeatPassword -> {
                showMessage(R.string.error_password_mismatch)
                false
            }
            !isPasswordValid(password) -> {
                showMessage(R.string.error_invalid_password)
                false
            }
            else -> true
        }
    }

    private fun showMessage(messageResId: Int) {
        binding.textViewResult.apply {
            setTextColor(ContextCompat.getColor(context, R.color.warning))
            text = getString(messageResId)
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isLowerCase() } &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() }
    }
}
