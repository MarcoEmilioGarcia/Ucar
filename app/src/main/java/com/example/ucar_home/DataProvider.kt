package com.example.ucar_home



import java.net.URL


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


object DataProvider {
    val publicaciones: List<Any> = listOf(
        1, listOf(URL("R.drawable.porche"), URL("R.drawable.porche2")), "Nuevo Coche", 24, listOf("Esta muy chulo", "Dabuti Colega"),
        2, listOf(URL("R.drawable.bmw"), URL("R.drawable.bmw2")), "Nuevo Coche", 23, listOf("Esta muy chulo", "Dabuti Colega")
    )
}

