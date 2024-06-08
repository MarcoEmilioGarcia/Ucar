package com.example.ucar_home

import java.net.URL
import java.time.LocalDate


object variables {
    var Email: String = ""
    var Password: String = ""
}

data class User(
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val bibliography: String = "",
    var followers: Int = 0,
    var following: Int = 0,
    val followersList: List<String> = emptyList(), // List of user IDs following this user
    val followingList: List<String> = emptyList() // List of user IDs this user is following
)

data class Chat(
    var idUser1: String = "",
    var idUser2: String = "",
    var username: String = "",
    val imageUrl: String = "",
    val lastMessage: String = "",
    val unreadMessages: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    val messages: Map<String, Message> = emptyMap()
)


data class Message(
    var senderId: String = "",
    var timestamp: Long = System.currentTimeMillis(), // Unix timestamp
    var content: String = ""
)




data class Event(
    var title: String = "",
    var imageUrl: String = "",
    var date: String = "",
    var address: String = "",
    var description: String = "",
    var idUser: String = ""
)
data class CarObject(
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
    var description: String = "",
    var imageUrl: String = "",
    var likes: Int = 0,
)


