package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ucar_home.databinding.ActivityMainBinding

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val mapsFragment = MapsFragment()
    private val chatFragment = ChatFragment()
    private lateinit var auth: FirebaseAuth

    // Mapa para mantener las referencias a los íconos originales
    private val originalIconsMap = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Inicializar los íconos originales
        originalIconsMap[R.id.homeFragment] = R.drawable.icon_home
        originalIconsMap[R.id.searchFragment] = R.drawable.icon_search
        originalIconsMap[R.id.mapsFragment] = R.drawable.icon_maps
        originalIconsMap[R.id.chatFragment] = R.drawable.icon_chat

        // Funcionalidad del toolbar
        toolbar()

        // Cargar el fragmento de inicio
        loadFragment(homeFragment)

        // Establecer el listener para la selección de elementos del menú
        binding.bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homeFragment -> {
                loadFragment(homeFragment)
                item.setIcon(R.drawable.icon_home_bold)
                restoreOriginalIcon(item)
                return@OnNavigationItemSelectedListener true
            }
            R.id.searchFragment -> {
                loadFragment(searchFragment)
                item.setIcon(R.drawable.icon_search_bold)
                restoreOriginalIcon(item)
                return@OnNavigationItemSelectedListener true
            }
            R.id.mapsFragment -> {
                loadFragment(mapsFragment)
                item.setIcon(R.drawable.icon_maps_bold)
                restoreOriginalIcon(item)
                return@OnNavigationItemSelectedListener true
            }
            R.id.chatFragment -> {
                loadFragment(chatFragment)
                item.setIcon(R.drawable.icon_chat_bold)
                restoreOriginalIcon(item)
                return@OnNavigationItemSelectedListener true
            }
            else -> return@OnNavigationItemSelectedListener false
        }
    }

    // Función para cargar un fragmento en el contenedor
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    // Función para restaurar el ícono original del elemento previamente seleccionado
    private fun restoreOriginalIcon(selectedItem: MenuItem) {
        val itemId = selectedItem.itemId
        originalIconsMap.keys.forEach { id ->
            if (id != itemId) {
                binding.bottomNavigation.menu.findItem(id)?.setIcon(originalIconsMap[id]!!)
            }
        }
    }

    // FUNCION PARA LA FUNCIONALIDAD DEL TOOLBAR
    fun toolbar(){
        Log.d(ContentValues.TAG, "empiezo funcion toolbar")
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val imageButton: ImageButton = findViewById(R.id.btnHome)
        imageButton.setOnClickListener {
            val intent = Intent(this,HomeFragment::class.java)
            startActivity(intent)
        }
        Log.d(ContentValues.TAG, "traza prueba")
        val imageUser: ImageButton = findViewById(R.id.btnProfile)

        val userReference = FirebaseDatabase.getInstance().getReference("users")



        Log.d(ContentValues.TAG, "traza prueba 2")
        try {
            Log.d(ContentValues.TAG, variables.Email)
            Log.d(ContentValues.TAG, variables.Password)
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
                                Log.d(ContentValues.TAG, "Traza 3")
                                val userSnapshot = dataSnapshot.children.firstOrNull()

                                // Verificar si se encontró algún resultado
                                if (userSnapshot != null) {
                                    Log.d(ContentValues.TAG, "Traza 4")
                                    // Obtener el usuario desde el primer hijo
                                    val user = userSnapshot.getValue(User::class.java)
                                    user?.imageUrl?.let { imageUrl ->

                                        // Obtener la referencia de Storage desde la URL
                                        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

                                        // Descargar la URL de la imagen desde Storage
                                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                                            // Cargar la imagen en el ImageButton usando Glide
                                            Glide.with(this@MainActivity)
                                                .load(uri)
                                                .into(binding.toolbar.btnProfile)
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

        }catch (e: Exception){      Log.d(ContentValues.TAG, "Error fatal")}








        imageUser.setOnClickListener {
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }

    }
    data class User(
        val username: String = "",
        val email: String = "", // Added email field
        val phoneNumber: String ="",
        val name: String = "",
        val imageUrl: String ="",
        val bibliography: String =""
    )
}