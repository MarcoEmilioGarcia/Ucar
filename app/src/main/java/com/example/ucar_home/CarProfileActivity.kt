package com.example.ucar_home

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ucar_home.databinding.ActivityCarProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage


class CarProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val carId = intent.getStringExtra("carId")
        Log.d(ContentValues.TAG, "El ID del coche es!: $carId")

        if (carId != null) {
            val carsReference = FirebaseDatabase.getInstance().getReference("cars")
            Log.d(ContentValues.TAG, "Referencia a Firebase: ${carsReference.child("8e4cfd88-e498-4d34-b0cc-3f9195984ee3")}")
            carsReference.child(carId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d(ContentValues.TAG, "Datos recibidos del coche: ${dataSnapshot.value}")

                    if (dataSnapshot.exists()) {
                        val car = dataSnapshot.getValue(CarObject::class.java)
                        if (car != null) {
                            Log.d(ContentValues.TAG, "Coche obtenido: $car")
                            binding.textViewName.text = car.title
                            binding.textViewBrand.text = car.brand
                            binding.textViewModel.text = car.model
                            binding.textViewHp.text = car.cv.toString()
                            binding.textViewCc.text = car.cc.toString()
                            binding.textViewYear.text = car.year.toString()
                            binding.textViewFuel.text = car.fuel
                            binding.textViewCarDescription.text = car.description

                            car.imageUrl.let { imageUrl ->
                                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                storageReference.downloadUrl.addOnSuccessListener { uri ->
                                    Glide.with(this@CarProfileActivity)
                                        .load(uri)
                                        .into(binding.imgCarProfile)
                                }.addOnFailureListener { exception ->
                                    Log.e(ContentValues.TAG, "Error al descargar la imagen", exception)
                                }
                            }
                        } else {
                            Log.d(ContentValues.TAG, "El coche es nulo o los datos no coinciden con la estructura esperada")
                        }
                    } else {
                        Log.d(ContentValues.TAG, "El coche no existe en la base de datos")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
                }
            })
        } else {
            Log.e(ContentValues.TAG, "El ID del coche es nulo")
        }

        binding.imageBtnGoBack.setOnClickListener {
            finish()
        }

        binding.btnEditProfile.setOnClickListener {
            // Lógica para editar el perfil del coche
        }

        binding.btnAdd.setOnClickListener {
            // Lógica para añadir algo relacionado con el coche
        }
    }
}
