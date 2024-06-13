package com.example.ucar_home.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ucar_home.CarObject
import com.example.ucar_home.databinding.ActivityCarProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class CarProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarProfileBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        const val TAG = "CarProfileActivity"
        const val CAR_ID_KEY = "carId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val carId = intent.getStringExtra(CAR_ID_KEY)
        Log.d(TAG, "El ID del coche es: $carId")

        if (carId != null) {
            val carsReference = FirebaseDatabase.getInstance().getReference("cars")
            carsReference.child(carId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val car = dataSnapshot.getValue(CarObject::class.java)
                        if (car != null) {
                            Log.d(TAG, "Coche obtenido: $car")
                            binding.apply {
                                textViewName.text = car.title ?: "N/A"
                                textViewBrand.text = car.brand ?: "N/A"
                                textViewModel.text = car.model ?: "N/A"
                                textViewHp.text = car.cv?.toString() ?: "N/A"
                                textViewCc.text = car.cc?.toString() ?: "N/A"
                                textViewYear.text = car.year?.toString() ?: "N/A"
                                textViewFuel.text = car.fuel ?: "N/A"
                                textViewCarDescription.text = car.description ?: "N/A"
                            }

                            car.imageUrl?.let { imageUrl ->
                                if (imageUrl.isNotEmpty()) {
                                    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                                        Glide.with(this@CarProfileActivity)
                                            .load(uri)
                                            .into(binding.imgCarProfile)
                                    }.addOnFailureListener { exception ->
                                        Log.e(TAG, "Error al descargar la imagen", exception)
                                    }
                                } else {
                                    Log.e(TAG, "La URL de la imagen está vacía")
                                }
                            } ?: run {
                                Log.e(TAG, "La URL de la imagen es nula")
                            }
                        } else {
                            Log.d(TAG, "El coche es nulo o los datos no coinciden con la estructura esperada")
                        }
                    } else {
                        Log.d(TAG, "El coche no existe en la base de datos")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error al consultar la base de datos", databaseError.toException())
                }
            })
        } else {
            Log.e(TAG, "El ID del coche es nulo")
        }

        binding.imageBtnGoBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnEditProfile.setOnClickListener {
            // Lógica para editar el perfil del coche
        }

        binding.btnAdd.setOnClickListener {
            // Lógica para añadir algo relacionado con el coche
        }
    }
}
