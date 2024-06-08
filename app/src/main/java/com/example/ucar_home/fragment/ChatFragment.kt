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

            // Clear any previous adapter to avoid issues
            recyclerView.adapter = null

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

            // Configura el LinearLayoutManager para que apile desde el final
            binding.recyclerViewChat.layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        } ?: Log.e(ContentValues.TAG, "Binding es nulo en onViewCreated")
    }



    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (TextUtils.isEmpty(messageText)) {
            // Si el mensaje está vacío, no hacer nada
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val message = Message(content = messageText)
            Log.d(ContentValues.TAG, "Enviando mensaje: $message")
            messagesRef.push().setValue(message).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Limpiar el campo de entrada de texto después de enviar el mensaje
                    binding.messageInput.text.clear()
                    Log.d(ContentValues.TAG, "Mensaje enviado con éxito")

                    // Actualizar el último mensaje y la marca de tiempo en la referencia del chat
                    val chatUpdates = hashMapOf<String, Any>(
                        "lastMessage" to messageText,
                        "timestamp" to message.timestamp
                    )
                    messagesRef.parent?.updateChildren(chatUpdates)

                    // Crear un nuevo chat si no existe
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
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Crear nuevo chat
                    val chat = Chat(
                        idUser1 = userId,
                        idUser2 = chatId.replace("$userId-", "").replace("-$userId", ""),
                        username = "", // Añadir nombre de usuario del otro usuario si está disponible
                        imageUrl = "", // Añadir URL de la imagen del otro usuario si está disponible
                        lastMessage = chatUpdates["lastMessage"] as String,
                        unreadMessages = "0", // Inicialmente no hay mensajes no leídos
                        timestamp = chatUpdates["timestamp"] as Long,
                        messages = emptyMap() // Inicialmente no hay mensajes en el mapa
                    )
                    chatRef.setValue(chat)
                } else {
                    // Actualizar chat existente
                    chatRef.updateChildren(chatUpdates)
                }
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
        val chatAdapter = ChatAdapter(messagesList)
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

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(targetUserId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                user?.let {
                    val chatUpdates = hashMapOf<String, Any>(
                        "lastMessage" to "", // Inicialmente vacío
                        "timestamp" to System.currentTimeMillis()
                    )
                    createOrUpdateChat(currentUserId, it.username, it.imageUrl, chatUpdates)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al obtener detalles del usuario", databaseError.toException())
            }
        })
    }




    private fun createOrUpdateChat(userId: String, username: String, imageUrl: String, chatUpdates: HashMap<String, Any>) {
        val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Crear nuevo chat
                    val chat = Chat(
                        idUser1 = userId,
                        idUser2 = chatId.replace("$userId-", "").replace("-$userId", ""),
                        username = username, // Nombre de usuario del otro usuario
                        imageUrl = imageUrl, // URL de la imagen del otro usuario
                        lastMessage = chatUpdates["lastMessage"] as String,
                        unreadMessages = "0", // Inicialmente no hay mensajes no leídos
                        timestamp = chatUpdates["timestamp"] as Long,
                        messages = emptyMap() // Inicialmente no hay mensajes en el mapa
                    )
                    chatRef.setValue(chat)
                } else {
                    // Actualizar chat existente
                    chatRef.updateChildren(chatUpdates)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "Error al consultar la base de datos", databaseError.toException())
            }
        })
    }





    private fun loadChats() {
        val userReference = FirebaseDatabase.getInstance().getReference("chats")
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(ContentValues.TAG, "DataSnapshot: ${dataSnapshot.value}")
                val chatList = mutableListOf<Chat>()
                dataSnapshot.children.forEach {
                    val chat = it.getValue(Chat::class.java)
                    chat?.let {
                        if (it.idUser1 == auth.uid || it.idUser2 == auth.uid) {
                            chatList.add(it)
                            Log.d(ContentValues.TAG, "Chat añadido: $chat")
                        }
                    }
                }
                chatsList = chatList
                if (chatsList.isNotEmpty()) {
                    val adapter = ChatProfileAdapter(chatsList) { chat ->
                        onChatClicked(chat)
                    }
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
