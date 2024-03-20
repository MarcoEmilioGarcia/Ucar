package com.example.ucar_home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.ucar_home.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val mapsFragment = MapsFragment()
    private val chatFragment = ChatFragment()

    // Mapa para mantener las referencias a los íconos originales
    private val originalIconsMap = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val imageButton: ImageButton = findViewById(R.id.btnHome)
        imageButton.setOnClickListener {
            val intent = Intent(this,HomeFragment::class.java)
            startActivity(intent)
        }
        val imageUser: ImageButton = findViewById(R.id.btnProfile)
        //imageUser.setImageDrawable(GlobalVariables.usuario!!.getImage()!!.drawable)
        imageUser.setOnClickListener {
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}