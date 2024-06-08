package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ucar_home.databinding.ActivityProfileBinding
import com.example.ucar_home.fragment.ChatFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class OtherProfileActivity : AppCompatActivity() {
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
        val idUser = intent.getStringExtra("idUser")
        val carList: MutableList<CarObject> = mutableListOf()

        // Ocultar los botones de añadir y editar
        binding.btnAdd.visibility = View.GONE
        binding.btnAdd2.visibility = View.GONE
        binding.btnEditProfile.visibility = View.GONE

        if (idUser != null) {
            loadUserProfile(idUser, userReference, carsReference, carList)
        } else {
            Log.d(ContentValues.TAG, "idUser no proporcionado en el Intent")
        }

        setupFollowButton(userReference, carsReference, idUser, carList)
        setupRefreshButton(idUser)
    }

    private fun loadUserProfile(idUser: String, userReference: DatabaseReference, carsReference: DatabaseReference, carList: MutableList<CarObject>) {
        userReference.child(idUser).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    bindUserData(user)
                    loadUserCars(idUser, carsReference, carList)
                } else {
                    Log.d(ContentValues.TAG, "El usuario es nulo")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
            }
        })
    }

    private fun bindUserData(user: User) {
        binding.textViewName.text = user.name
        binding.textViewUsername.text = user.username
        binding.textViewBibliography.text = user.bibliography
        binding.textView7.text = user.followers.toString()
        binding.textView9.text = user.following.toString()

        user.imageUrl?.let { imageUrl ->
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@OtherProfileActivity)
                    .load(uri)
                    .into(binding.imageView2)
            }.addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Error al descargar la imagen", exception)
            }
        }
    }

    private fun loadUserCars(idUser: String, carsReference: DatabaseReference, carList: MutableList<CarObject>) {
        carsReference.child(idUser).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                carList.clear()
                dataSnapshot.children.forEach {
                    val car = it.getValue(CarObject::class.java)
                    car?.let { carList.add(it) }
                }
                if (carList.isNotEmpty()) {
                    val adapter = CarAdapter(carList)
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
    }

    private fun setupFollowButton(userReference: DatabaseReference, carsReference: DatabaseReference, idUser: String?, carList: MutableList<CarObject>) {
        binding.button3.setOnClickListener {
            if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        updateFollowStatus(userReference, idUser)
                        loadAuthUserCars(carsReference, carList)
                    } else {
                        Log.e(ContentValues.TAG, "Error en la autenticación")
                    }
                }
            }
        }
    }

    private fun updateFollowStatus(userReference: DatabaseReference, idUser: String?) {
        userReference.orderByChild("email").equalTo(variables.Email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userSnapshot = dataSnapshot.children.firstOrNull()
                if (userSnapshot != null) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let {
                        if (idUser != null) {
                            if (user.followingList.contains(idUser)) {
                                user.following -= 1
                                user.followingList.remove(idUser)
                                binding.button3.text = "Follow"
                            } else {
                                user.following += 1
                                user.followingList.add(idUser)
                                binding.button3.text = "Unfollow"
                            }

                            userSnapshot.ref.setValue(user)
                                .addOnSuccessListener { Log.d(ContentValues.TAG, "Usuario actualizado exitosamente") }
                                .addOnFailureListener { exception -> Log.e(ContentValues.TAG, "Error al actualizar el usuario", exception) }
                        }
                    }
                } else {
                    Log.d(ContentValues.TAG, "No se encontraron resultados para el correo electrónico proporcionado")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
            }
        })
    }

    private fun loadAuthUserCars(carsReference: DatabaseReference, carList: MutableList<CarObject>) {
        auth.uid?.let { uid ->
            val userCarsReference = carsReference.child(uid)
            userCarsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    carList.clear()
                    dataSnapshot.children.forEach {
                        val car = it.getValue(CarObject::class.java)
                        car?.let { carList.add(it) }
                    }
                    if (carList.isNotEmpty()) {
                        val adapter = CarAdapter(carList)
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
        } ?: Log.e(ContentValues.TAG, "El UID de auth es nulo")
    }

    private fun setupRefreshButton(idUser: String?) {
        binding.button4.setOnClickListener {
            if (idUser != null) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("idUser", idUser)
                    putExtra("selected_fragment", ChatFragment::class.java.name)
                }
                startActivity(intent)
            } else {
                Log.d(ContentValues.TAG, "idUser es nulo")
            }
        }
    }


}
