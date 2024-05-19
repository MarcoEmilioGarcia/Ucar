package com.example.ucar_home

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ucar_home.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.time.LocalDate
import java.util.Calendar

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private lateinit var geocoder: Geocoder
    private lateinit var auth: FirebaseAuth
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val DEFAULT_LOCATION = LatLng(40.416775, -3.70379) // Madrid, España
    private val DEFAULT_ZOOM = 11f
    private val markers = mutableListOf<Marker>()

    @RequiresApi(Build.VERSION_CODES.O)
    private val eventsList = mutableListOf<Event>( )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(ContentValues.TAG, "traza 1")

        geocoder = Geocoder(requireContext())
        auth = FirebaseAuth.getInstance()
        val eventsReference = FirebaseDatabase.getInstance().getReference("events")
        auth.signInWithEmailAndPassword(variables.Email.toString(), variables.Password.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(ContentValues.TAG, "traza 2")
                val user = auth.currentUser
                val idUser = user?.uid

                eventsReference.orderByChild("idUser").equalTo(idUser).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d(ContentValues.TAG, "traza 3")
                        val userSnapshot = dataSnapshot.children.forEach {

                            Log.d(ContentValues.TAG, "traza 4")
                            val event = it.getValue(Event::class.java)


                            if (event != null && idUser != null) {
                                Log.d(ContentValues.TAG, "traza 5")
                                /*
                                val dateString = it.child("date").getValue(String::class.java)
                                if (dateString != null) {
                                    val date = LocalDate.parse(dateString)
                                   */

                                eventsList.add( Event(event.title, event.imageUrl, event.date, event.address, event.description, idUser))
/*
                                } else {
                                    // Manejar el caso donde la fecha es nula
                                }*/
                            }

                        }


                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar errores de cancelación
                        Log.e(ContentValues.TAG, "Error en la consulta de eventos: ${databaseError.message}")
                    }
                })
            } else {
                val errorMessage = task.exception?.message ?: "Error desconocido al autenticar"
                Log.d(ContentValues.TAG, "Error al autenticar: $errorMessage")
                // Aquí podrías mostrar un mensaje de error al usuario, dependiendo del tipo de error
            }
        }


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMarkers(newText)
                return true
            }
        })

        val buttonDatePicker: ImageButton = binding.buttonDatePicker
        buttonDatePicker.setOnClickListener {
            showStartDatePickerDialog()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))

        // Ocultar etiquetas de puntos de interés
        mMap.isIndoorEnabled = false

        // Primero habilita la ubicación actual
        enableMyLocation()

        // Luego aplica el estilo del mapa
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))

        if (!mMap.isMyLocationEnabled) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 4f))
        }

        eventsList.forEach { event ->
            addEventMarker(event)
        }

        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }
    }


    private fun filterMarkers(query: String?) {
        markers.forEach { marker ->
            val title = marker.title
            if (title!!.contains(query ?: "", ignoreCase = true)) {
                marker.isVisible = true
            } else {
                marker.isVisible = false
            }
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    private fun addEventMarker(event: Event) {
        try {
            val location = geocoder.getFromLocationName(event.address, 1)
            if (location!!.isNotEmpty()) {
                val latitude = location[0].latitude
                val longitude = location[0].longitude
                val position = LatLng(latitude, longitude)

                val bitmap = resizeBitmap(R.drawable.alfiler__1_, 100, 110)

                val marker = mMap.addMarker(MarkerOptions().position(position).title(event.title)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
                marker!!.tag = event
                markers.add(marker!!)
            } else {
                Toast.makeText(requireContext(), "Dirección no encontrada para ${event.title}", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al obtener la dirección para ${event.title}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun resizeBitmap(drawableRes: Int, width: Int, height: Int): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(resources, drawableRes)
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay).toString()
                filterEventsByDate(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterEventsByDate(date: String) {
        markers.forEach { it.remove() }
        markers.clear()
        eventsList.filter { it.date == date }.forEach { event ->
            addEventMarker(event)
        }
    }

    private fun showStartDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val startDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay).toString()
                showEndDatePickerDialog(startDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }


    private fun showEndDatePickerDialog(startDate: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val endDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay).toString()
                filterEventsByDateRange(startDate, endDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterEventsByDateRange(startDate: String, endDate: String) {
        markers.forEach { it.remove() }
        markers.clear()

        eventsList.filter { it.date >= startDate && it.date <= endDate }
            .forEach { event ->
                addEventMarker(event)
            }
    }
}
