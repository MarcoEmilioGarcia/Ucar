package com.example.ucar_home

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ucar_home.databinding.ActivityCreateEventBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    val event = EventObject(title, imageUrl, date, address, description)
                    // Aquí puedes hacer lo que quieras con el evento, como guardarlo en una base de datos, etc.
                    Toast.makeText(this, "Event created: $event", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
