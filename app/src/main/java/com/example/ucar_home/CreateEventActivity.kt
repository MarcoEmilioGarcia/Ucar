package com.example.ucar_home

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.ucar_home.databinding.ActivityCreateEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var auth: FirebaseAuth

 //  @RequiresApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val buttonCreateEvent = binding.buttonCreateEvent

        buttonCreateEvent.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val imageUrl = binding.editTextImageUrl.text.toString()
            val dateText = binding.editTextDate.text.toString()
            val address = binding.editTextAddress.text.toString()
            val description = binding.editTextDescription.text.toString()

            if (title.isNotEmpty() && dateText.isNotEmpty() && address.isNotEmpty() && description.isNotEmpty()) {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = try {
                    LocalDate.parse(dateText, dateFormatter)
                } catch (e: Exception) {
                    null
                }

                if (date != null) {
                    val evento = Event(title, imageUrl, date, address, description)

                    // Aquí puedes hacer lo que quieras con el evento, como guardarlo en una base de datos, etc.
                    saveEvent(evento)

                    Toast.makeText(this, "Event created: $evento", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }


    }
    /*

    private fun saveEventToDatabase(
        title: String?,
        imageUrl: String?,
        date: String?, // Cambiado de LocalDate a String para Firebase
        address: String?,
        description: String?
    ) {
        val database = FirebaseDatabase.getInstance().reference
        val event = EventObject(title ?: "", imageUrl ?: "", date ?: "", address ?: "", description ?: "")

        database.child("events").push().setValue(event)
            .addOnSuccessListener {
                Log.d("TAG", "Evento guardado exitosamente.")
                // Continuar con la siguiente actividad o cualquier otra acción necesaria
            }
            .addOnFailureListener { e ->
                Log.d("TAG", "Error al guardar el evento: ", e)
            }
    }*/
    fun saveEvent(event: Event) {
        // Obtener la instancia de Firebase Database
        val database = FirebaseDatabase.getInstance()

        // Obtener una referencia a la ubicación donde se guardarán los eventos
        val eventsRef = database.getReference("events")

        // Generar una nueva clave única para el evento
        val eventKey = eventsRef.push().key ?: ""

        // Asignar el evento a esa clave
        eventsRef.child(eventKey).setValue(event)
            .addOnSuccessListener {
                // Éxito al guardar el evento
                println("Evento guardado exitosamente en Firebase!")
            }
            .addOnFailureListener { e ->
                // Error al guardar el evento
                println("Error al guardar el evento en Firebase: $e")
            }
    }



}
