package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ucar_home.databinding.FragmentSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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
    private var chatsList: List<User> = listOf()

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

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener { task ->
                if (task.isSuccessful && auth.uid != null) {
                    loadPosts()
                    setupSearchListener()
                } else {
                    Log.e(ContentValues.TAG, "El UID de auth es nulo o la autenticación falló")
                }
            }
        }

        return binding.root
    }

    private fun loadPosts() {
        val postsReference = postReference.child(auth.uid!!)
        postsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear() // Limpiar la lista antes de agregar nuevos elementos
                dataSnapshot.children.forEach {
                    val car = it.getValue(PostObject::class.java)
                    car?.let {
                        postList.add(it)
                        Log.d(ContentValues.TAG, "Publicación añadida: ${car}")
                    }
                }
                if (postList.isNotEmpty()) {
                    Log.d(ContentValues.TAG, "Número de publicaciones en la lista: ${postList.size}")
                    binding.publicaciones.layoutManager = GridLayoutManager(context, 3)
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
    }

    private fun setupSearchListener() {
        val usersReference = FirebaseDatabase.getInstance().getReference("users")
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()

                if (query.isNotEmpty()) {
                    searchUsers(query, usersReference)
                } else {
                    loadPosts() // Volver a cargar las publicaciones si el campo de búsqueda está vacío
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchUsers(query: String, usersReference: DatabaseReference) {
        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatsList = mutableListOf()
                dataSnapshot.children.forEach {
                    val chat = it.getValue(User::class.java)
                    chat?.let {
                        if (chat.username.contains(query, true)) {
                            (chatsList as MutableList).add(it)
                        }
                    }
                }
                if (chatsList.isNotEmpty()) {
                    binding.publicaciones.layoutManager = LinearLayoutManager(context)
                    val adapter = UserProfileAdapter(chatsList) { user ->
                        val intent = Intent(context, OtherProfileActivity::class.java).apply {
                            putExtra("idUser", user.idUser)
                        }
                        startActivity(intent)
                    }
                    binding.publicaciones.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error en la configuración del adapter", databaseError.toException())
            }
        })
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
