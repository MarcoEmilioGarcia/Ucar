package com.example.ucar_home.profile

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.ucar_home.CarObject
import com.example.ucar_home.HomeFragment
import com.example.ucar_home.PostAdapter
import com.example.ucar_home.PostObject
import com.example.ucar_home.R
import com.example.ucar_home.SearchAdapter
import com.example.ucar_home.User
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
    private val userReference = FirebaseDatabase.getInstance().getReference("users")
    private val carsReference = FirebaseDatabase.getInstance().getReference("cars")
    private val postsReference = FirebaseDatabase.getInstance().getReference("posts")
    private var postList: MutableList<Pair<PostObject, User>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val carId = intent.getStringExtra("carId")
        Log.d(TAG, "El ID del coche es: $carId")

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email.toString(), variables.Password.toString()).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (auth.uid != null) {
                        val userCarsReference = carsReference.child(auth.uid!!)
                        userCarsReference.child(carId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val car = dataSnapshot.getValue(CarObject::class.java)
                                car?.let {
                                    binding.textViewName.text = car.title ?: "N/A"
                                    binding.textViewBrand.text = car.brand ?: "N/A"
                                    binding.textViewModel.text = car.model ?: "N/A"
                                    binding.textViewHp.text = car.cv?.toString() ?: "N/A"
                                    binding.textViewCc.text = car.cc?.toString() ?: "N/A"
                                    binding.textViewYear.text = car.year?.toString() ?: "N/A"
                                    binding.textViewFuel.text = car.fuel ?: "N/A"
                                    binding.textViewCarDescription.text = car.description ?: "N/A"

                                    car.imageUrl?.let { imageUrl ->
                                        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                                            Glide.with(this@CarProfileActivity)
                                                .load(uri)
                                                .into(binding.imgCarProfile)
                                        }.addOnFailureListener { exception ->
                                            Log.e(ContentValues.TAG, "Error al descargar la imagen", exception)
                                        }
                                    }

                                    loadCarPosts(carId)
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

    private fun loadCarPosts(carId: String) {
        Log.d(ContentValues.TAG, "Iniciando loadCarPosts con carId: $carId")

        postsReference.orderByChild("idCar").equalTo(carId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(ContentValues.TAG, "Recibidos ${dataSnapshot.childrenCount} publicaciones para carId: $carId")

                val postsList = mutableListOf<Pair<PostObject, User>>()
                val countDownLatch = CountDownLatch(dataSnapshot.childrenCount.toInt())

                dataSnapshot.children.forEach { snapshot ->
                    val post = snapshot.getValue(PostObject::class.java)
                    if (post != null) {
                        Log.d(ContentValues.TAG, "Procesando post con id: ${post.idPost}")

                        userReference.child(post.idUser).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(User::class.java)
                                if (user != null) {
                                    Log.d(ContentValues.TAG, "Usuario encontrado para post id: ${post.idPost}")
                                    postsList.add(Pair(post, user))
                                } else {
                                    Log.e(ContentValues.TAG, "Usuario no encontrado para post id: ${post.idPost}")
                                }
                                countDownLatch.countDown()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(ContentValues.TAG, "Error al obtener el usuario para post id: ${post.idPost}", databaseError.toException())
                                countDownLatch.countDown()
                            }
                        })
                    } else {
                        Log.e(ContentValues.TAG, "Post es nulo para snapshot key: ${snapshot.key}")
                        countDownLatch.countDown()
                    }
                }

                countDownLatch.await()
                if (postsList.isNotEmpty()) {
                    Log.d(ContentValues.TAG, "Actualizando postAdapter con ${postsList.size} publicaciones")
                    postAdapter.updatePosts(postsList)
                } else {
                    Log.d(ContentValues.TAG, "La lista de publicaciones está vacía")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al obtener las publicaciones", databaseError.toException())
            }
        })
    }





}
