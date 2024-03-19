package com.example.ucar_home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.ucar_home.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val homeFragment = HomeFragment()
    val searchFragment = SearchFragment()
    val mapsFragment = MapsFragment()
    val chatFragment = ChatFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //FUNCIONALIDAD TOOLBAR
        toolbar()

        //ELECCION DE FRAGMENT
        binding.bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //CARGAR FRAGMENT INICIO
        loadFragment(homeFragment)

    }


    // FUNCION PARA CARGAR LOS DISTINTOS FRAGMENTS
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homeFragment -> {
                loadFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.searchFragment -> {
                loadFragment(searchFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.mapsFragment -> {
                loadFragment(mapsFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.chatFragment -> {
                loadFragment(chatFragment)
                return@OnNavigationItemSelectedListener true
            }
            else -> return@OnNavigationItemSelectedListener false
        }
    }

    // FUNCION PARA CARGAR FRAGMENT DE INICIO
    fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.commit()
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
        /*imageUser.setImageDrawable(GlobalVariables.usuario!!.getImage()!!.drawable)
        imageUser.setOnClickListener {
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }*/
    }
}