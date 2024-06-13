package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ucar_home.add_car.AddCarActivity
import com.example.ucar_home.create_event.CreateEventStep1Activity
import com.example.ucar_home.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity(), CarAdapter.OnItemClickListener {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.publicaciones.layoutManager = LinearLayoutManager(this)

        auth = FirebaseAuth.getInstance()
        val userReference = FirebaseDatabase.getInstance().getReference("users")
        val carsReference = FirebaseDatabase.getInstance().getReference("cars")
        var carList: MutableList<CarObject> = mutableListOf()

        // Ocultar el LinearLayout con ID linnear
        binding.linnear.visibility = View.GONE

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email.toString(), variables.Password.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    userReference.orderByChild("email").equalTo(variables.Email).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val userSnapshot = dataSnapshot.children.firstOrNull()
                            if (userSnapshot != null) {
                                val user = userSnapshot.getValue(User::class.java)
                                if (user != null) {
                                    binding.textViewName.text = user.name
                                    binding.textViewUsername.text = user.username
                                    binding.textViewBibliography.text = user.bibliography
                                    binding.textView7.text = user.followers.toString()
                                    binding.textView9.text = user.following.toString()

                                    user.imageUrl?.let { imageUrl ->
                                        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                                            Glide.with(this@ProfileActivity)
                                                .load(uri)
                                                .into(binding.imageView2)
                                        }.addOnFailureListener { exception ->
                                            Log.e(ContentValues.TAG, "Error al descargar la imagen", exception)
                                        }
                                    }
                                } else {
                                    Log.d(ContentValues.TAG, "El usuario es nulo")
                                }
                            } else {
                                Log.d(ContentValues.TAG, "No se encontraron resultados para el correo electrónico proporcionado")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
                        }
                    })

                    if (auth.uid != null) {
                        val userCarsReference = carsReference.child(auth.uid!!)
                        userCarsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                dataSnapshot.children.forEach {
                                    val car = it.getValue(CarObject::class.java)
                                    car?.let {
                                        carList.add(it)
                                    }
                                }
                                if (carList.isNotEmpty()) {
                                    val adapter = CarAdapter(carList, this@ProfileActivity)
                                    binding.publicaciones.adapter = adapter
                                    adapter.notifyDataSetChanged()
                                } else {
                                    Log.d(ContentValues.TAG, "La lista de coches está vacía")
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(ContentValues.TAG, "Error en la configuración del adapter", databaseError.toException())
                            }
                        })
                    } else {
                        Log.e(ContentValues.TAG, "El UID de auth es nulo")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al autenticar"
                    Log.d(ContentValues.TAG, "Error al autenticar: $errorMessage")
                }
            }
        }

        binding.btnAdd.setOnClickListener {
            binding.viewOptions.visibility = View.VISIBLE
        }

        binding.imageBtnGoBack2.setOnClickListener {
            binding.viewOptions.visibility = View.GONE
        }

        binding.btnNewCar.setOnClickListener {
            val intent = Intent(this, AddCarActivity::class.java)
            startActivity(intent)
        }

        binding.btnCreateEvent.setOnClickListener {
            val intent = Intent(this, CreateEventStep1Activity::class.java)
            startActivity(intent)
        }

        binding.imageBtnSignOut.setOnClickListener {
            binding.viewLogOut.visibility = View.VISIBLE
        }

        binding.imageBtnGoBack3.setOnClickListener {
            binding.viewLogOut.visibility = View.GONE
        }

        binding.btnLogOut.setOnClickListener {
            signOut()
        }
    }

    override fun onItemClick(car: CarObject) {
        val intent = Intent(this, CarProfileActivity::class.java)
        intent.putExtra("car_title", car.title)
        intent.putExtra("car_image_url", car.imageUrl)
        startActivity(intent)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
