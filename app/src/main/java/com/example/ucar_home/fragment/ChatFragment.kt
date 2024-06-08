package com.example.ucar_home.fragment

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ucar_home.Chat
import com.example.ucar_home.ChatAdapter
import com.example.ucar_home.ChatProfileAdapter
import com.example.ucar_home.Message
import com.example.ucar_home.databinding.FragmentChatBinding
import com.example.ucar_home.variables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private val messagesList = mutableListOf<Message>()
    private lateinit var chatId: String
    private lateinit var messagesRef: DatabaseReference
    private var _binding: FragmentChatBinding? = null
    private var chatsList: List<Chat> = listOf()

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView for chats
        recyclerView = binding.recyclerViewChats
        recyclerView.layoutManager = LinearLayoutManager(context)

        val userId = auth.currentUser?.uid
        val idUser = activity?.intent?.getStringExtra("idUser")

        if (userId != null && idUser != null) {
            setupChat(userId, idUser)
            binding.recyclerViewChat.visibility = View.VISIBLE
            binding.messageInputContainer.visibility = View.VISIBLE
            binding.recyclerViewChats.visibility = View.GONE
            binding.tabs.visibility = View.GONE
        } else {
            binding.recyclerViewChat.visibility = View.GONE
            binding.messageInputContainer.visibility = View.GONE
            binding.recyclerViewChats.visibility = View.VISIBLE
            binding.tabs.visibility = View.VISIBLE
            loadChats()
        }

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(
                variables.Email,
                variables.Password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userReference = FirebaseDatabase.getInstance().getReference("chats")
                    userReference.orderByChild("idUser1").equalTo(auth.uid).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            chatsList = mutableListOf()
                            dataSnapshot.children.forEach {
                                val chat = it.getValue(Chat::class.java)
                                chat?.let {
                                    (chatsList as MutableList).add(it)
                                }
                            }
                            if (chatsList.isNotEmpty()) {
                                val adapter = ChatProfileAdapter(chatsList)
                                binding.recyclerViewChats.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(
                                ContentValues.TAG,
                                "Error al consultar la base de datos",
                                databaseError.toException()
                            )
                        }
                    })
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al autenticar"
                    Log.d(ContentValues.TAG, "Error al autenticar: $errorMessage")
                }
            }
        }
    }

    private fun setupChat(currentUserId: String, targetUserId: String) {
        chatId = if (currentUserId < targetUserId) {
            "$currentUserId-$targetUserId"
        } else {
            "$targetUserId-$currentUserId"
        }

        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)

        recyclerView = binding.recyclerViewChat
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ChatAdapter(messagesList.sortedByDescending { it.timestamp })

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messagesList.clear()
                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(Message::class.java)
                    message?.let { messagesList.add(it) }
                }
                messagesList.sortByDescending { it.timestamp }
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to read messages", databaseError.toException())
            }
        })
    }

    private fun loadChats() {
        val userReference = FirebaseDatabase.getInstance().getReference("chats")
        userReference.orderByChild("idUser1").equalTo(auth.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatsList = mutableListOf()
                dataSnapshot.children.forEach {
                    val chat = it.getValue(Chat::class.java)
                    chat?.let {
                        (chatsList as MutableList).add(it)
                    }
                }
                if (chatsList.isNotEmpty()) {
                    val adapter = ChatProfileAdapter(chatsList)
                    binding.recyclerViewChats.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
