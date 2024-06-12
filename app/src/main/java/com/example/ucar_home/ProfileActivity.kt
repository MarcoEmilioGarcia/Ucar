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
import com.example.ucar_home.add_post.AddPostActivity
import com.example.ucar_home.databinding.ActivityProfileBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    // Mapa para mantener las referencias a los íconos originales
    private val originalIconsMap = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.publicaciones.layoutManager = LinearLayoutManager(this)

        auth = FirebaseAuth.getInstance()
        val userReference = FirebaseDatabase.getInstance().getReference("users")
        val carsReference = FirebaseDatabase.getInstance().getReference("cars")
        val postsReference = FirebaseDatabase.getInstance().getReference("posts")
        var carList: MutableList<CarObject> = mutableListOf()
        var postList: MutableList<Pair<PostObject, User>> = mutableListOf()

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
                                            // Mostrar mensaje de error al usuario
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
                        val userPostsReference = postsReference.child(auth.uid!!)

                        userCarsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                dataSnapshot.children.forEach {
                                    val car = it.getValue(CarObject::class.java)
                                    car?.let {
                                        carList.add(it)
                                    }
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
                        /*

                        userPostsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val postTasks = dataSnapshot.children.map { postSnapshot ->
                                    val post = postSnapshot.getValue(PostObject::class.java)
                                    val userTask = userReference.child(post?.idUser ?: "").get().continueWith { userTask ->
                                        val user = userTask.result?.getValue(User::class.java)
                                        post?.let { post to user }
                                    }
                                    userTask
                                }

                                Tasks.whenAllComplete(postTasks).addOnCompleteListener { tasks ->
                                    postTasks.forEach { task ->
                                        val postUserPair = task.result
                                        postUserPair?.let {
                                            postList.add(it as Pair<PostObject, User>)
                                        }
                                    }

                                    if (postList.isNotEmpty()) {
                                        val adapter = PostAdapter(postList)
                                        binding.publicaciones.adapter = adapter
                                        adapter.notifyDataSetChanged()
                                    } else {
                                        Log.d(ContentValues.TAG, "La lista de publicaciones está vacía")
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(ContentValues.TAG, "Error en la configuración del adapter", databaseError.toException())
                            }
                        })

                         */
                    } else {
                        Log.e(ContentValues.TAG, "El UID de auth es nulo")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al autenticar"
                    Log.d(ContentValues.TAG, "Error al autenticar: $errorMessage")
                    // Mostrar mensaje de error al usuario
                }
            }
        }

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddCarActivity::class.java)
            startActivity(intent)
        }
        binding.btnAdd2.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }

        binding.imageBtnGoBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
