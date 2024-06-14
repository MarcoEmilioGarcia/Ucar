package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ucar_home.databinding.ActivityMainBinding
import com.example.ucar_home.fragment.ChatFragment
import com.example.ucar_home.fragment.MapsFragment
import com.example.ucar_home.fragment.SearchFragment

import com.example.ucar_home.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    val usersReference = FirebaseDatabase.getInstance().getReference("users")
    var postsList = mutableListOf<Pair<PostObject, User>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            // Redirigir a la pantalla de inicio de sesión si no hay usuario autenticado
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Inicializar los íconos originales
        originalIconsMap[R.id.homeFragment] = R.drawable.icon_home
        originalIconsMap[R.id.searchFragment] = R.drawable.icon_search
        originalIconsMap[R.id.mapsFragment] = R.drawable.icon_maps
        originalIconsMap[R.id.chatFragment] = R.drawable.icon_chat

        // Funcionalidad del toolbar
        setupToolbar()

        // Obtener el fragmento seleccionado y el idUser
        val selectedFragmentClassName = intent.getStringExtra("selected_fragment")
        val selectedFragment = when (selectedFragmentClassName) {
            ChatFragment::class.java.name -> {
                val idUser = intent.getStringExtra("idUser")
                val chatFragment = ChatFragment.newInstance("", "")
                val args = Bundle()
                args.putString("idUser", idUser)
                chatFragment.arguments = args
                chatFragment
            }
            HomeFragment::class.java.name -> homeFragment
            SearchFragment::class.java.name -> searchFragment
            MapsFragment::class.java.name -> mapsFragment
            else -> homeFragment // Fragmento por defecto
        }
        loadFragment(selectedFragment)

        // Establecer el listener para la selección de elementos del menú
        binding.bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Cargar datos iniciales
        loadData()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cada vez que la actividad se reanuda
        loadData()
    }

    private fun loadData() {
        // Autenticar y cargar datos si hay credenciales
        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.uid?.let { uid ->
                        loadFollowingUserPosts(uid)
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
                restoreOriginalIcons(item)
                true
            }
            R.id.searchFragment -> {
                loadFragment(searchFragment)
                item.setIcon(R.drawable.icon_search_bold)
                restoreOriginalIcons(item)
                true
            }
            R.id.mapsFragment -> {
                loadFragment(mapsFragment)
                item.setIcon(R.drawable.icon_maps_bold)
                restoreOriginalIcons(item)
                true
            }
            R.id.chatFragment -> {
                loadFragment(chatFragment)
                item.setIcon(R.drawable.icon_chat_bold)
                restoreOriginalIcons(item)
                true
            }
            else -> false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    private fun restoreOriginalIcons(selectedItem: MenuItem) {
        val itemId = selectedItem.itemId
        originalIconsMap.keys.forEach { id ->
            if (id != itemId) {
                binding.bottomNavigation.menu.findItem(id)?.setIcon(originalIconsMap[id]!!)
            }
        }
    }

    private fun setupToolbar() {
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

    private fun loadFollowingUserPosts(uid: String) {
        Log.d(ContentValues.TAG, "Empiezo a cargar las publicaciones de los usuarios seguidos")
        usersReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)

                if (user != null) {
                    val followingList = user.followingList
                    Log.d(ContentValues.TAG, "Usuario sigue a: ${followingList.size} usuarios")
                    if (followingList.isNotEmpty()) {
                        val postsList = mutableListOf<Pair<PostObject, User>>()
                        followingList.forEach { userId ->
                            Log.d(ContentValues.TAG, "Cargando publicaciones del usuario: $userId")
                            postsReference.orderByChild("idUser").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    Log.d(ContentValues.TAG, "Publicaciones del usuario: $userId cargadas")

                                    dataSnapshot.children.forEach { snapshot ->
                                        val post = snapshot.getValue(PostObject::class.java)
                                        if (post != null) {
                                            Log.d(ContentValues.TAG, "Publicación encontrada: ${post.idPost}")
                                            usersReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                                    val user = userSnapshot.getValue(User::class.java)
                                                    if (user != null) {
                                                        postsList.add(Pair(post, user))
                                                        Log.d(ContentValues.TAG, "Post añadido a la lista: ${post.idPost}")
                                                    } else {
                                                        Log.d(ContentValues.TAG, "Usuario no encontrado para la publicación: ${post.idPost}")
                                                    }
                                                    // Mover la actualización de la UI aquí
                                                    if (postsList.isNotEmpty()) {
                                                        Log.d(ContentValues.TAG, "Posts list size: ${postsList.size}")
                                                        val homeFragment = supportFragmentManager.findFragmentById(R.id.frame_container) as? HomeFragment
                                                        homeFragment?.updatePosts(postsList)
                                                        Log.d(ContentValues.TAG, "Posts actualizados en HomeFragment")
                                                    } else {
                                                        Log.d(ContentValues.TAG, "La lista de publicaciones está vacía")
                                                    }
                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {
                                                    Log.e(ContentValues.TAG, "Error al obtener el usuario", databaseError.toException())
                                                }
                                            })
                                        } else {
                                            Log.d(ContentValues.TAG, "Publicación nula para el usuario: $userId")
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(ContentValues.TAG, "Error al obtener las publicaciones", databaseError.toException())
                                }
                            })
                        }
                    } else {
                        Log.d(ContentValues.TAG, "El usuario no sigue a nadie")
                    }
                } else {
                    Log.e(ContentValues.TAG, "Error al obtener el usuario")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error en la configuración del adapter", databaseError.toException())
            }
        })
    }
}
