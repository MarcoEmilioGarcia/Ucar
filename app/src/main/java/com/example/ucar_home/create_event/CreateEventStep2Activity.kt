package com.example.ucar_home.create_event

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ucar_home.Event
import com.example.ucar_home.profile.ProfileActivity
import com.example.ucar_home.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class CreateEventStep2Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editTextDescription: EditText
    private lateinit var btnNext: Button
    private lateinit var imageUser: ImageView
    private lateinit var btnCamera: ImageButton
    private lateinit var btnGallery: ImageButton

    private var imageUri: Uri? = null

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_PICK_IMAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event_step2)

        auth = FirebaseAuth.getInstance()

        // Binding the views
        btnNext = findViewById(R.id.btnNext)
        editTextDescription = findViewById(R.id.editTextBibliography)
        imageUser = findViewById(R.id.imageUser)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)

        // Get data from the first step
        val title = intent.getStringExtra("Title") ?: ""
        val dateText = intent.getStringExtra("DateText") ?: ""
        val address = intent.getStringExtra("Address") ?: ""
        val time = intent.getStringExtra("Time") ?: ""

        btnCamera.setOnClickListener {
            openCamera()
        }

        btnGallery.setOnClickListener {
            openGallery()
        }

        btnNext.setOnClickListener {
            val description = editTextDescription.text.toString()
            val imageUrl = imageUri.toString()

            if (description.isNotEmpty()) {
                if (title.isNotEmpty() && dateText.isNotEmpty() && address.isNotEmpty() && time.isNotEmpty()) {
                    val user = auth.currentUser
                    val idUser = user?.uid

                    val eventDate = "$dateText $time"
                    val evento = Event(title, imageUrl, eventDate, address, description, idUser!!)

                    // Guardar el evento en Firebase Database
                    saveEvent(evento)

                    Toast.makeText(this, "Event created: $evento", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in the description", Toast.LENGTH_SHORT).show()
            }


        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imageUri = getImageUriFromBitmap(imageBitmap)
                    imageUser.setImageBitmap(imageBitmap)
                }
                REQUEST_PICK_IMAGE -> {
                    imageUri = data?.data
                    imageUser.setImageURI(imageUri)
                }
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
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

        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}

