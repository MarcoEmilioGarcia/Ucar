package com.example.ucar_home

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
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
import java.io.IOException
import java.time.LocalDate
import java.util.Calendar

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private lateinit var geocoder: Geocoder
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val DEFAULT_LOCATION = LatLng(40.416775, -3.70379) // Madrid, España
    private val DEFAULT_ZOOM = 11f

    private val markers = mutableListOf<Marker>()

    @RequiresApi(Build.VERSION_CODES.O)
    private val eventsList = mutableListOf<Event>(
        Event("Evento 1", "url_imagen_1", LocalDate.now(), "Av de burgos 32", "Descripción 1"),
        Event("Evento 2", "url_imagen_2", LocalDate.now(), "condesa de venadito 1", "Descripción 2")
    )

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

        geocoder = Geocoder(requireContext())

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

                val bitmap = resizeBitmap(R.drawable.marcador, 100, 110)

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                filterEventsByDate(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterEventsByDate(date: LocalDate) {
        markers.forEach { it.remove() }
        markers.clear()
        eventsList.filter { it.date == date }.forEach { event ->
            addEventMarker(event)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showStartDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val startDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                showEndDatePickerDialog(startDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showEndDatePickerDialog(startDate: LocalDate) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val endDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                filterEventsByDateRange(startDate, endDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterEventsByDateRange(startDate: LocalDate, endDate: LocalDate) {
        markers.forEach { it.remove() }
        markers.clear()

        eventsList.filter { it.date.isAfter(startDate.minusDays(1)) && it.date.isBefore(endDate.plusDays(1)) }
            .forEach { event ->
                addEventMarker(event)
            }
    }
}
