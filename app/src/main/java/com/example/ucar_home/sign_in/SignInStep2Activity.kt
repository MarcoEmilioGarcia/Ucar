package com.example.ucar_home.sign_in

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivitySignInStep2Binding
import com.google.firebase.auth.FirebaseAuth

class SignInStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep2Binding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val username = intent.getStringExtra("Username")
        val password = intent.getStringExtra("Password")

        // Go Back Button
        binding.imageBtnGoBack.setOnClickListener {
            finish()
        }

        // Next Button
        binding.btnNext.setOnClickListener {
            handleNextButtonClick(username, password)
        }
    }

    private fun handleNextButtonClick(username: String?, password: String?) {
        val email = binding.editTextEmail.text.toString().trim()
        if (isEmailValid(email)) {
            checkIfEmailExists(email, username, password)
        } else {
            showMessage(R.string.error_invalid_email)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkIfEmailExists(email: String, username: String?, password: String?) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        proceedToNextStep(email, username, password)
                    } else {
                        showMessage(R.string.error_email_exists)
                    }
                } else {
                    Log.e(ContentValues.TAG, "Error checking email existence", task.exception)
                    showMessage(R.string.error_checking_email)
                }
            }
    }

    private fun proceedToNextStep(email: String, username: String?, password: String?) {
        val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
        val intent = Intent(this, SignInStep3Activity::class.java).apply {
            putExtra("Username", username)
            putExtra("Password", password)
            putExtra("Email", email)
            putExtra("PhoneNumber", phoneNumber)
        }
        Log.d(ContentValues.TAG, "Moving to the next activity with email: $email and password: $password")
        startActivity(intent)
    }

    private fun showMessage(messageResId: Int) {
        binding.textViewResult.apply {
            setTextColor(ContextCompat.getColor(context, R.color.warning))
            text = getString(messageResId)
        }
    }
}
