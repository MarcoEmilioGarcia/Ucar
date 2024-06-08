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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Go Back Button
        binding.imageBtnGoBack1.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
        }

        // Next Button
        binding.btnNext.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()
            val repeatPassword = binding.editTextRepeatPassword.text.toString()

            when {
                username.isEmpty() || password.isEmpty() -> {
                    showMessage(R.string.error_empty_fields)
                }
                password != repeatPassword -> {
                    showMessage(R.string.error_password_mismatch)
                }
                !isPasswordValid(password) -> {
                    showMessage(R.string.error_invalid_password)
                }
                else -> {
                    val intent = Intent(this, SignInStep2Activity::class.java).apply {
                        putExtra("Username", username)
                        putExtra("Password", password)
                    }
                    startActivity(intent)
                }
            }
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
