package com.example.ucar_home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ucar_home.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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

        if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
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


                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al autenticar"
                    Log.d(ContentValues.TAG, "Error al autenticar: $errorMessage")
                }
            }
        }

        setupChat()
    }

    private fun setupChat() {
        val idUser1 = auth.currentUser?.uid ?: ""
        val idUser2 = param1 ?: ""
        chatId = generateChatId(idUser1, idUser2)
        messagesRef = FirebaseDatabase.getInstance().getReference("chats/$chatId/messages")
        loadMessages()
    }

    private fun loadMessages() {
        messagesRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messagesList.add(message)
                    }
                }
                // Aquí deberías notificar al adaptador de mensajes que los datos han cambiado
                // messagesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Error loading messages: ${error.message}")
            }
        })
    }

    private fun generateChatId(idUser1: String, idUser2: String): String {
        return if (idUser1 < idUser2) {
            "$idUser1-$idUser2"
        } else {
            "$idUser2-$idUser1"
        }
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
