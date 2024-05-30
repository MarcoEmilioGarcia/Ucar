package com.example.ucar_home

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ucar_home.databinding.ActivitySearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var auth: FirebaseAuth
    val postReference = FirebaseDatabase.getInstance().getReference("posts")
    var postList: MutableList<PostObject> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        Log.d(ContentValues.TAG, "Aqui estoy")
        // Configurar el RecyclerView con un GridLayoutManager de 3 columnas
        binding.publicaciones.layoutManager = GridLayoutManager(this, 3)

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener(this) { task ->
                if (auth.uid != null) {
                    // Referencia directa a la ruta del usuario específico
                    val postsReference = postReference.child(auth.uid!!)

                    postsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.d(ContentValues.TAG, "Consulta de publicaciones exitosa, número de resultados: ${dataSnapshot.childrenCount}")
                            dataSnapshot.children.forEach {
                                val car = it.getValue(PostObject::class.java)
                                car?.let {
                                    postList.add(it)
                                    Log.d(ContentValues.TAG, "Publicación añadida: ${car}")
                                }
                            }
                            if (postList.isNotEmpty()) {
                                Log.d(ContentValues.TAG, "Número de publicaciones en la lista: ${postList.size}")
                                val adapter = SearchAdapter(postList)
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
                } else {
                    Log.e(ContentValues.TAG, "El UID de auth es nulo")
                }
            }
        }
    }
}
