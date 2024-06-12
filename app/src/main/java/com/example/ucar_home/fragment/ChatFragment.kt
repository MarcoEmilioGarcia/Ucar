package com.example.ucar_home.fragment

import android.content.ContentValues
import android.os.Bundle
import android.text.TextUtils
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
import com.example.ucar_home.User
import com.example.ucar_home.databinding.FragmentChatBinding
import com.example.ucar_home.variables
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {
    // Variables iniciales y FirebaseAuth
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
        _binding?.let {
            Log.d(ContentValues.TAG, "Traza 1")
            recyclerView = it.recyclerViewChats
            recyclerView.layoutManager = LinearLayoutManager(context)

            val idUser = arguments?.getString("idUser")

            if (variables.Email.isNotEmpty() && variables.Password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(variables.Email, variables.Password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            if (idUser != null) {
                                setupChat(userId, idUser)
                                binding.recyclerViewChat.visibility = View.VISIBLE
                                binding.messageInputContainer.visibility = View.VISIBLE
                                binding.recyclerViewChats.visibility = View.GONE
                                binding.tabs.visibility = View.GONE
                            } else {
                                Log.e(ContentValues.TAG, "idUser es nulo")
                                binding.recyclerViewChat.visibility = View.GONE
                                binding.messageInputContainer.visibility = View.GONE
                                binding.recyclerViewChats.visibility = View.VISIBLE
                                binding.tabs.visibility = View.VISIBLE
                                loadChats()
                            }
                        } else {
                            Log.e(ContentValues.TAG, "userId es nulo después de la autenticación")
                        }
                    } else {
                        Log.e(ContentValues.TAG, "Error en la autenticación: ${task.exception?.message}")
                    }
                }
            } else {
                Log.e(ContentValues.TAG, "Email o Password están vacíos")
            }

            binding.sendButton.setOnClickListener {
                sendMessage()
            }

            binding.recyclerViewChat.layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        } ?: Log.e(ContentValues.TAG, "Binding es nulo en onViewCreated")
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (TextUtils.isEmpty(messageText)) {
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val message = Message(content = messageText, userId = userId)

            messagesRef.push().setValue(message).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.messageInput.text.clear()

                    val chatUpdates = hashMapOf<String, Any>(
                        "lastMessage" to messageText,
                        "timestamp" to System.currentTimeMillis()
                    )
                    messagesRef.parent?.updateChildren(chatUpdates)

                    // Crear o actualizar el chat
                    createOrUpdateChat(userId, chatUpdates)
                } else {
                    Log.e(ContentValues.TAG, "Error al enviar el mensaje: ${task.exception?.message}")
                }
            }
        } else {
            Log.e(ContentValues.TAG, "userId es nulo en sendMessage")
        }
    }

    private fun createOrUpdateChat(userId: String, chatUpdates: HashMap<String, Any>) {
        val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        val targetUserId = chatId.replace("$userId-", "").replace("-$userId", "")

        val currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val targetUserRef = FirebaseDatabase.getInstance().getReference("users").child(targetUserId)

        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(currentUserDataSnapshot: DataSnapshot) {
                val currentUser = currentUserDataSnapshot.getValue(User::class.java)
                targetUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(targetUserDataSnapshot: DataSnapshot) {
                        val targetUser = targetUserDataSnapshot.getValue(User::class.java)
                        if (currentUser != null && targetUser != null) {
                            chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(chatSnapshot: DataSnapshot) {
                                    if (!chatSnapshot.exists()) {
                                        val chat = Chat(
                                            idUser1 = userId,
                                            idUser2 = targetUser.idUser,
                                            usernameUser1 = currentUser.username,
                                            usernameUser2 = targetUser.username,
                                            imageUrlUser1 = currentUser.imageUrl,
                                            imageUrlUser2 = targetUser.imageUrl,
                                            lastMessage = chatUpdates["lastMessage"] as String,
                                            unreadMessages = "0",
                                            timestamp = chatUpdates["timestamp"] as Long,
                                            messages = mapOf()
                                        )
                                        chatRef.setValue(chat).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.e(ContentValues.TAG, "Chat creado")
                                            } else {
                                                Log.e(ContentValues.TAG, "Error al crear el chat: ${task.exception?.message}")
                                            }
                                        }
                                    } else {
                                        val chatUpdatesFull = chatUpdates.toMutableMap()
                                        chatUpdatesFull["idUser1"] = userId
                                        chatUpdatesFull["idUser2"] = targetUser.idUser
                                        chatUpdatesFull["usernameUser1"] = currentUser.username
                                        chatUpdatesFull["usernameUser2"] = targetUser.username
                                        chatUpdatesFull["imageUrlUser1"] = currentUser.imageUrl
                                        chatUpdatesFull["imageUrlUser2"] = targetUser.imageUrl
                                        chatUpdatesFull["unreadMessages"] = "0"

                                        chatRef.updateChildren(chatUpdatesFull).addOnCompleteListener { task ->
                                            if (!task.isSuccessful) {
                                                Log.e(ContentValues.TAG, "Error al actualizar el chat: ${task.exception?.message}")
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
                                }
                            })
                        } else {
                            Log.e(ContentValues.TAG, "El usuario actual o el usuario objetivo es nulo")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
            }
        })
    }

    private fun setupChat(currentUserId: String, targetUserId: String) {
        chatId = if (currentUserId < targetUserId) {
            "$currentUserId-$targetUserId"
        } else {
            "$targetUserId-$currentUserId"
        }

        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages")

        recyclerView = binding.recyclerViewChat
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        val chatAdapter = ChatAdapter(messagesList, currentUserId)
        recyclerView.adapter = chatAdapter

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messagesList.clear()
                for (snapshot in dataSnapshot.children) {
                    val message = snapshot.getValue(Message::class.java)
                    message?.let {
                        Log.d(ContentValues.TAG, "Mensaje recibido: $it")
                        messagesList.add(it)
                    }
                }
                messagesList.sortBy { it.timestamp }
                chatAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to read messages", databaseError.toException())
            }
        })
    }

    private fun loadChats() {
        val userReference = FirebaseDatabase.getInstance().getReference("chats")
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(ContentValues.TAG, "DataSnapshot: ${dataSnapshot.value}")
                var chatList = mutableListOf<Chat>()
                if (auth.uid == null) {
                    Log.e(ContentValues.TAG, "El UID del usuario actual es nulo")
                    return
                }
                dataSnapshot.children.forEach { snapshot ->
                    val chat = snapshot.getValue(Chat::class.java)
                    chat?.let {
                        chatList.add(it)
                        Log.d(ContentValues.TAG, "Chat añadido: $it")
                    } ?: Log.e(ContentValues.TAG, "El chat es nulo para el snapshot: ${snapshot.key}")
                }
                chatsList = chatList
                if (chatsList.isNotEmpty()) {
                    val adapter = ChatProfileAdapter(chatsList, auth.uid!!, ::onChatClicked)
                    binding.recyclerViewChats.adapter = adapter
                    adapter.notifyDataSetChanged()
                    Log.d(ContentValues.TAG, "Chats list updated: ${chatsList.size} chats loaded")
                } else {
                    Log.d(ContentValues.TAG, "La lista de chats está vacía")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
            }
        })
    }



    private fun onChatClicked(chat: Chat) {
        val currentUserId = auth.currentUser?.uid ?: return
        val targetUserId = if (chat.idUser1 == currentUserId) chat.idUser2 else chat.idUser1
        setupChat(currentUserId, targetUserId)
        binding.recyclerViewChat.visibility = View.VISIBLE
        binding.messageInputContainer.visibility = View.VISIBLE
        binding.recyclerViewChats.visibility = View.GONE
        binding.tabs.visibility = View.GONE
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
