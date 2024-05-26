package com.example.ucar_home


import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.example.ucar_home.databinding.ActivityAddCar3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCarActivity3: AppCompatActivity() {

    private lateinit var binding: ActivityAddCar3Binding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCar3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val title = intent.getStringExtra("Title")
        val brand = intent.getStringExtra("Brand")
        val model = intent.getStringExtra("Model")
        val cv = intent.getIntExtra("Cv", 0)
        val cc = intent.getIntExtra("Cc", 0)
        val year = intent.getIntExtra("Year", 0)
        val fuel = intent.getStringExtra("Fuel")
        val imageUrl = intent.getStringExtra("ImageUrl")

        binding.btnCreate.setOnClickListener {
            if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()){

                auth.signInWithEmailAndPassword(variables.Email.toString(), variables.Password.toString()).addOnCompleteListener(this) { task ->
                    // Log.d(ContentValues.TAG, "efectivamente 2")

                    if (task.isSuccessful) {

                        val user = auth.currentUser
                        val idUser = user?.uid
                        val description = binding.editTextDescription.text.toString()
                        Log.d(ContentValues.TAG, "User registered successfully.")
                        saveCarToDatabase(title,brand,model, cv, cc, year,fuel, imageUrl, description )
                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                        } else {
                            Log.d(ContentValues.TAG, "User registration failed.")
                        }

                    }
            } else {
                Log.d(ContentValues.TAG, "Email or password is empty.")
            }
        }



    }

    private fun saveCarToDatabase(
        title: String?,
        brand: String?,
        model: String?,
        cv: Int?,
        cc: Int?,
        year: Int?,
        fuel: String?,
        imageUrl: String?,
        description: String?,

    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val car = CarObject(title!!, brand!!, model!!, cv!!, cc!!, year!!, fuel!!, imageUrl!!, description!!)
        uid?.let {
            database.child("cars").child(it).push().setValue(car)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "Car data saved successfully.")
                    // Proceed to next activity or whatever you need to do
                }
                .addOnFailureListener { e ->
                    Log.d(ContentValues.TAG, "Error saving car data: ${e.message}")
                }
        }
    }

}