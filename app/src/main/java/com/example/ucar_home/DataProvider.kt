package com.example.ucar_home

import java.net.URL
import java.time.LocalDate


object variables {
    var Email: String = ""
    var Password: String = ""
}

data class User(
    var idUser: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val bibliography: String = "",
    var followers: Int = 0,
    var following: Int = 0,
    val followersList: MutableList<String> = mutableListOf(), // List of user IDs following this user
    val followingList: MutableList<String> = mutableListOf() // List of user IDs this user is following
)



data class Chat(
    var idUser1: String = "",
    var idUser2: String = "",
    var username: String = "",
    var imageUrl: String = "",
    var lastMessage: String = "",
    var unreadMessages: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var messages: Map<String, Message> = emptyMap()
)


data class Message(
    var timestamp: Long = System.currentTimeMillis(), // Unix timestamp
    var content: String = "",
    var userId: String =""
)




data class Event(
    var idUser: String = "",
    var title: String = "",
    var imageUrl: String = "",
    var date: String = "",
    var address: String = "",
    var description: String = ""

)
data class CarObject(
    var idUser: String = "",
    var title: String = "",
    var brand: String = "",
    var model: String = "",
    var cv: Int = 0,
    var cc: Int = 0,
    var year: Int = 0,
    val fuel: String = "",
    val imageUrl: String = "",
    var description: String = "",
)

data class PostObject(
    var idPost: String ="",
    var idUser: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var likes: Int = 0
)


