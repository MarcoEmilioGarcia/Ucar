package com.example.ucar_home

import java.net.URL
import java.time.LocalDate


object variables {
    var Email: String = ""
    var Password: String = ""
}

data class User(
    val username: String = "",
    val email: String = "", // Added email field
    val phoneNumber: String ="",
    val name: String = "",
    val imageUrl: String ="",
    val bibliography: String =""
)

data class Event(
     var title: String,
     var imageUrl: String,
     var date: LocalDate,
     var address: String,
     var description: String
)

data class CarObject(
    var title: String="",
    var brand: String="",
    var model: String="",
    var cv: Int,
    var cc: Int,
    var year: Int,
    val fuel: String ="",
    val imageUrl: String ="",
    var description: String=""
)


object DataProvider {
    val publicaciones: List<Any> = listOf(
        1, listOf(URL("R.drawable.porche"), URL("R.drawable.porche2")), "Nuevo Coche", 24, listOf("Esta muy chulo", "Dabuti Colega"),
        2, listOf(URL("R.drawable.bmw"), URL("R.drawable.bmw2")), "Nuevo Coche", 23, listOf("Esta muy chulo", "Dabuti Colega")
    )
}

