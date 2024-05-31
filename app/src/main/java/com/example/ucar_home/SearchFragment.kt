package com.example.ucar_home

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ucar_home.databinding.FragmentSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentSearchBinding
    private lateinit var auth: FirebaseAuth
    private val postReference = FirebaseDatabase.getInstance().getReference("posts")
    private var postList: MutableList<PostObject> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        Log.d(ContentValues.TAG, "Aqui estoy")

        // Configurar el RecyclerView con un GridLayoutManager de 3 columnas
        binding.publicaciones.layoutManager = GridLayoutManager(context, 3)

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener { task ->
                if (auth.uid != null) {
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

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
