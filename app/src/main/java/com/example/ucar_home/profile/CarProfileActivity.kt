package com.example.ucar_home.profile

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.ucar_home.*
import com.example.ucar_home.add_car.AddCarActivity
import com.example.ucar_home.add_post.AddPostActivity
import com.example.ucar_home.databinding.ActivityCarProfileBinding
import com.example.ucar_home.variables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.CountDownLatch

class CarProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var postAdapter: PostAdapter
    private val carsReference = FirebaseDatabase.getInstance().getReference("cars")
    private val postsReference = FirebaseDatabase.getInstance().getReference("posts")
    private val userReference = FirebaseDatabase.getInstance().getReference("users")

    private var postList: MutableList<Pair<PostObject, User>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val carId = intent.getStringExtra("carId")
        Log.d(TAG, "El ID del coche es: $carId")

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.uid?.let { uid ->
                        val userCarsReference = carsReference.child(uid)
                        carId?.let { id ->
                            userCarsReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val car = dataSnapshot.getValue(CarObject::class.java)
                                    car?.let {
                                        updateUIWithCarDetails(it)
                                       // loadCarPosts(id)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(TAG, "Error en la configuración del adapter", databaseError.toException())
                                }
                            })
                        } ?: run {
                            Log.e(TAG, "El ID del coche es nulo")
                        }
                    } ?: run {
                        Log.e(TAG, "El UID de auth es nulo")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al autenticar"
                    Log.d(TAG, "Error al autenticar: $errorMessage")
                }
            }
        }

        binding.imageBtnGoBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnEditProfile.setOnClickListener {
            // Lógica para editar el perfil del coche
        }

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            intent.putExtra("carId", carId)
            startActivity(intent)
        }
    }

    private fun updateUIWithCarDetails(car: CarObject) {
        binding.apply {
            textViewName.text = car.title ?: "N/A"
            textViewBrand.text = car.brand ?: "N/A"
            textViewModel.text = car.model ?: "N/A"
            textViewHp.text = car.cv?.toString() ?: "N/A"
            textViewCc.text = car.cc?.toString() ?: "N/A"
            textViewYear.text = car.year?.toString() ?: "N/A"
            textViewFuel.text = car.fuel ?: "N/A"
            textViewCarDescription.text = car.description ?: "N/A"

            car.imageUrl?.let { imageUrl ->
                val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this@CarProfileActivity)
                        .load(uri)
                        .into(imgCarProfile)
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Error al descargar la imagen", exception)
                }
            }
        }
    }

    private fun loadCarPosts(carId: String?) {
        Log.d(TAG, "Iniciando loadCarPosts con carId: $carId")

        val postsList = mutableListOf<Pair<PostObject, User>>()
        postsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "Número de posts obtenidos: ${dataSnapshot.childrenCount}")
                postsList.clear()
                val countDownLatch = CountDownLatch(dataSnapshot.childrenCount.toInt())
                dataSnapshot.children.forEach { snapshot ->
                    val post = snapshot.getValue(PostObject::class.java)
                    if (post != null) {
                        Log.d(TAG, "Post obtenido: $post")
                        if (carId == null || post.idCar == carId) {
                            Log.d(TAG, "Post coincide con el carId: $carId")
                            userReference.child(post.idUser ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val user = userSnapshot.getValue(User::class.java)
                                    if (user != null) {
                                        Log.d(TAG, "Usuario obtenido: $user")
                                        postsList.add(Pair(post, user))
                                    } else {
                                        Log.e(TAG, "Usuario inválido para el post: ${snapshot.value}")
                                    }
                                    countDownLatch.countDown()
                                }

                                override fun onCancelled(userDatabaseError: DatabaseError) {
                                    Log.e(TAG, "Error al obtener el usuario", userDatabaseError.toException())
                                    countDownLatch.countDown()
                                }
                            })
                        } else {
                            Log.d(TAG, "Post no coincide con el carId: $carId")
                            countDownLatch.countDown()
                        }
                    } else {
                        Log.e(TAG, "Datos inválidos en el post: ${snapshot.value}")
                        countDownLatch.countDown()
                    }
                }

                try {
                    countDownLatch.await() // Esperar a que todos los usuarios sean cargados antes de continuar
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Error al esperar que todos los usuarios sean cargados", e)
                }

                if (postsList.isNotEmpty()) {
                    Log.d(TAG, "Número de posts en la lista: ${postsList.size}")
                    binding.publicaciones.layoutManager = GridLayoutManager(this@CarProfileActivity, 3)
                    val adapter = PostAdapter(postsList)
                    binding.publicaciones.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "La lista de posts está vacía")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error al obtener los posts", databaseError.toException())
            }
        })
    }

}
