package com.example.ucar_home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ucar_home.databinding.ActivityCreateEventStep1Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class CreateEventStep1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEventStep1Binding
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.editTextDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.editTextTime.setOnClickListener {
            showTimePickerDialog()
        }

        // Go Back Button
        binding.imageBtnGoBack1.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Next Button
        binding.btnNext.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val dateText = binding.editTextDate.text.toString()
            val address = binding.editTextAddress.text.toString()
            val time = binding.editTextTime.text.toString()

            if (title.isEmpty() || dateText.isEmpty() || address.isEmpty() || time.isEmpty()){
                showMessage(R.string.error_empty_fields)
            } else {
                val intent = Intent(this, CreateEventStep2Activity::class.java).apply {
                    putExtra("Title", title)
                    putExtra("DateText", dateText)
                    putExtra("Address", address)
                    putExtra("Time", time)
                }
                startActivity(intent)
            }
        }
    }

    private fun showMessage(messageResId: Int) {
        binding.textViewResult.apply {
            setTextColor(ContextCompat.getColor(context, R.color.warning))
            text = getString(messageResId)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding.editTextDate.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.editTextTime.setText(selectedTime)
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun saveEvent(event: Event) {
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
