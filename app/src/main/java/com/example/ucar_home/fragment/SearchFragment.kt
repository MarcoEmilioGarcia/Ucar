package com.example.ucar_home.fragment

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ucar_home.profile.OtherProfileActivity
import com.example.ucar_home.PostObject
import com.example.ucar_home.SearchAdapter
import com.example.ucar_home.User
import com.example.ucar_home.UserProfileAdapter
import com.example.ucar_home.PostAdapter
import com.example.ucar_home.databinding.FragmentSearchBinding
import com.example.ucar_home.variables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

const val ARG_PARAM1 = "param1"
const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentSearchBinding
    private lateinit var auth: FirebaseAuth
    private val postReference = FirebaseDatabase.getInstance().getReference("posts")
    private var postList: MutableList<PostObject> = mutableListOf()
    private var chatsList: List<User> = listOf()
    private lateinit var postAdapter: PostAdapter
    private val usersReference = FirebaseDatabase.getInstance().getReference("users")

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

        postAdapter = PostAdapter(mutableListOf())

        binding.publicaciones2.layoutManager = LinearLayoutManager(context)

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(
                variables.Email,
                variables.Password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful && auth.uid != null) {
                    loadPosts()
                    setupSearchListener()
                } else {
                    Log.e(ContentValues.TAG, "El UID de auth es nulo o la autenticación falló")
                }
            }
        }

        binding.publicaciones2.adapter = postAdapter

        return binding.root
    }

    private fun loadPosts() {
        val postsReference = FirebaseDatabase.getInstance().getReference("posts")
        postsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                dataSnapshot.children.forEach { postSnapshot ->
                    val post = postSnapshot.getValue(PostObject::class.java)
                    if (post != null) {
                        postList.add(post)
                    } else {
                        Log.e(ContentValues.TAG, "Datos inválidos en el post: ${postSnapshot.value}")
                    }
                }
                if (postList.isNotEmpty()) {
                    postList = postList.shuffled().toMutableList()
                    binding.publicaciones.layoutManager = GridLayoutManager(context, 3)
                    val adapter = SearchAdapter(postList) { post -> onPostItemClicked(post) }
                    binding.publicaciones.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d(ContentValues.TAG, "La lista de coches está vacía")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    ContentValues.TAG,
                    "Error en la configuración del adapter",
                    databaseError.toException()
                )
            }
        })
    }


    private fun onPostItemClicked(post: PostObject) {
        usersReference.child(post.idUser).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    binding.publicaciones.visibility = View.GONE
                    binding.publicaciones2.visibility = View.VISIBLE

                    val userPostsList = mutableListOf<Pair<PostObject, User>>()
                    userPostsList.add(Pair(post, user))

                    val randomPosts = postList.filter { it != post }.shuffled().take(5)
                    val randomUsersMap = mutableMapOf<PostObject, User>()

                    for (randomPost in randomPosts) {
                        usersReference.child(randomPost.idUser).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(randomPostSnapshot: DataSnapshot) {
                                val randomUser = randomPostSnapshot.getValue(User::class.java)
                                if (randomUser != null) {
                                    randomUsersMap[randomPost] = randomUser
                                    userPostsList.add(Pair(randomPost, randomUser))

                                    if (userPostsList.size == randomPosts.size + 1) {
                                        postAdapter.updatePosts(userPostsList)
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.e(ContentValues.TAG, "Error al obtener el usuario", databaseError.toException())
                            }
                        })
                    }

                    if (randomPosts.isEmpty()) {
                        postAdapter.updatePosts(userPostsList)
                    }
                } else {
                    Log.e(ContentValues.TAG, "Usuario no encontrado para el post: ${post.idPost}")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al obtener el usuario", databaseError.toException())
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
                    loadPosts()
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
                Log.e(
                    ContentValues.TAG,
                    "Error en la configuración del adapter",
                    databaseError.toException()
                )
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
