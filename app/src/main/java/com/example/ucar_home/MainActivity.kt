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
import com.google.firebase.auth.FirebaseAuth
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
    val postsReference = FirebaseDatabase.getInstance().getReference("posts")
    var postsList = mutableMapOf<CarObject, User>()

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
        setupToolbar()

        // Cargar el fragmento de inicio
        val selectedFragmentClassName = intent.getStringExtra("selected_fragment")
        val selectedFragment = when (selectedFragmentClassName) {
            HomeFragment::class.java.name -> homeFragment
            SearchFragment::class.java.name -> searchFragment
            MapsFragment::class.java.name -> mapsFragment
            ChatFragment::class.java.name -> chatFragment
            else -> homeFragment // Fragmento por defecto
        }
        loadFragment(selectedFragment)

        // Establecer el listener para la selección de elementos del menú
        binding.bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Autenticar y cargar datos si hay credenciales
        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "Autenticación exitosa, uid: ${auth.uid}")
                    auth.uid?.let { uid ->
                        loadUserPosts(uid)
                    } ?: Log.e(ContentValues.TAG, "El UID de auth es nulo")
                } else {
                    Log.d(ContentValues.TAG, "Error al autenticar: ${task.exception?.message ?: "Error desconocido"}")
                }
            }
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homeFragment -> {
                loadFragment(homeFragment)
                item.setIcon(R.drawable.icon_home_bold)
                restoreOriginalIcon(item)
                true
            }
            R.id.searchFragment -> {
                loadFragment(searchFragment)
                item.setIcon(R.drawable.icon_search_bold)
                restoreOriginalIcon(item)
                true
            }
            R.id.mapsFragment -> {
                loadFragment(mapsFragment)
                item.setIcon(R.drawable.icon_maps_bold)
                restoreOriginalIcon(item)
                true
            }
            R.id.chatFragment -> {
                loadFragment(chatFragment)
                item.setIcon(R.drawable.icon_chat_bold)
                restoreOriginalIcon(item)
                true
            }
            else -> false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    private fun restoreOriginalIcon(selectedItem: MenuItem) {
        val itemId = selectedItem.itemId
        originalIconsMap.keys.forEach { id ->
            if (id != itemId) {
                binding.bottomNavigation.menu.findItem(id)?.setIcon(originalIconsMap[id]!!)
            }
        }
    }

    private fun setupToolbar() {
        Log.d(ContentValues.TAG, "empiezo funcion toolbar")
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val imageButton: ImageButton = findViewById(R.id.btnHome)
        imageButton.setOnClickListener {
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
        }

        val imageUser: ImageButton = findViewById(R.id.btnProfile)
        val userReference = FirebaseDatabase.getInstance().getReference("users")

        try {
            if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        userReference.orderByChild("email").equalTo(variables.Email).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val userSnapshot = dataSnapshot.children.firstOrNull()
                                userSnapshot?.getValue(com.example.ucar_home.User::class.java)?.imageUrl?.let { imageUrl ->
                                    FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).downloadUrl.addOnSuccessListener { uri ->
                                        Glide.with(this@MainActivity)
                                            .load(uri)
                                            .into(binding.toolbar.btnProfile)
                                    }.addOnFailureListener { exception ->
                                        Log.e(ContentValues.TAG, "Error al descargar la imagen: ${exception.message}")
                                    }
                                } ?: Log.d("TAG", "No se encontraron resultados para el correo electrónico proporcionado")
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(ContentValues.TAG, "Error en la consulta del usuario: ${databaseError.toException()}")
                            }
                        })
                    } else {
                        Log.d(ContentValues.TAG, "Error al autenticar: ${task.exception?.message ?: "Error desconocido"}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error fatal en toolbar", e)
        }

        imageUser.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserPosts(uid: String) {
        val userCarsReference = postsReference.child(uid)
        userCarsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val postsList = mutableMapOf<PostObject, User>()
                dataSnapshot.children.forEach { snapshot ->
                    val post = snapshot.getValue(PostObject::class.java)
                    val user = snapshot.getValue(User::class.java)
                    if (post != null && user != null) {
                        postsList[post] = user
                    }
                }

                if (postsList.isNotEmpty()) {
                    // Asumiendo que el fragmento actualmente visible es HomeFragment
                    (supportFragmentManager.findFragmentById(R.id.frame_container) as? HomeFragment)?.updatePosts(postsList)
                } else {
                    Log.d(ContentValues.TAG, "La lista de publicaciones está vacía")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error en la configuración del adapter", databaseError.toException())
            }
        })
    }
}