package com.example.ucar_home

import android.content.ContentValues
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.ucar_home.databinding.ActivityMainBinding
import com.example.ucar_home.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        //val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
       // val email = prefs.getString("email", null)
        auth = FirebaseAuth.getInstance()
        val imageUser: ImageButton = findViewById(R.id.btnProfile)

        val userReference = FirebaseDatabase.getInstance().getReference("users")


        // val currentUser: FirebaseUser? = auth.currentUser //val uid: String = currentUser?.uid ?: ""
        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()){
            Log.d(ContentValues.TAG, "efectivamente")

            auth.signInWithEmailAndPassword(variables.Email.toString(), variables.Password.toString()).addOnCompleteListener(this) { task ->
                // Log.d(ContentValues.TAG, "efectivamente 2")

                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "Identificacion adecuada")

                    userReference.orderByChild("email").equalTo(variables.Email).addListenerForSingleValueEvent(object :
                        ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Obtener el primer hijo de dataSnapshot (si existe)
                            val userSnapshot = dataSnapshot.children.firstOrNull()

                            // Verificar si se encontró algún resultado
                            if (userSnapshot != null) {
                                // Obtener el usuario desde el primer hijo
                                val user = userSnapshot.getValue(User::class.java)
                                user?.imageUrl?.let { imageUrl ->

                                    // Obtener la referencia de Storage desde la URL
                                    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                    // Descargar la URL de la imagen desde Storage
                                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                                        // Cargar la imagen en el ImageButton usando Glide
                                        Log.d(ContentValues.TAG, "URI de la imagen: $uri")

                                        Glide.with(this@ProfileActivity)
                                            .load(uri)
                                            .into(binding.toolbar.btnProfile)

                                        Glide.with(this@ProfileActivity)
                                            .load(uri)
                                            .into(binding.imageView2)
                                    }.addOnFailureListener { exception ->
                                        // Manejar errores de descarga de imagen
                                    }
                                }
                            } else {
                                // Manejar el caso en el que no se encontraron resultados
                                Log.d("TAG", "No se encontraron resultados para el correo electrónico proporcionado")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejar errores de cancelación
                        }
                    })

                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al autenticar"
                    Log.d(ContentValues.TAG, "Error al autenticar: $errorMessage")
                    // Aquí podrías mostrar un mensaje de error al usuario, dependiendo del tipo de error
                }
            }
        }

/*
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    val user = dataSnapshot.getValue(SignInStep4Activity.User::class.java)
                    // Aquí puedes utilizar los datos del usuario como lo necesites
                    Log.d(TAG, "Username: ${user?.username}")
                    Log.d(TAG, "Email: ${user?.email}")
                    Log.d(TAG, "Phone Number: ${user?.phoneNumber}")
                    Log.d(TAG, "Name: ${user?.name}")
                    Log.d(TAG, "Image URL: ${user?.imageUrl}")
                    Log.d(TAG, "Bibliography: ${user?.bibliography}")
                } else {
                    Log.d(TAG, "User data does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "onCancelled: ${databaseError.message}")
            }
        })
        */

    }



}
