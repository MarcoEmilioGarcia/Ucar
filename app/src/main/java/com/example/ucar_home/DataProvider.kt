package com.example.ucar_home



import java.net.URL


object variables {


    const val Email: String = ""
    const val Password: String = ""


}


object DataProvider {
    val publicaciones: List<Any> = listOf(
        1, listOf(URL("R.drawable.porche"), URL("R.drawable.porche2")), "Nuevo Coche", 24, listOf("Esta muy chulo", "Dabuti Colega"),
        2, listOf(URL("R.drawable.bmw"), URL("R.drawable.bmw2")), "Nuevo Coche", 23, listOf("Esta muy chulo", "Dabuti Colega")
    )
}

