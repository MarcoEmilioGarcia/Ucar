package com.example.ucar_home

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.ucar_home.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private lateinit var geocoder: Geocoder
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val DEFAULT_LOCATION = LatLng(40.416775, -3.70379) // Madrid, España
    private val DEFAULT_ZOOM = 11f
    private val markers = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geocoder = Geocoder(requireContext())

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.addMarkerButton.setOnClickListener {
            showInputDialog()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMarkers(newText)
                return true
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableMyLocation()

        if (!mMap.isMyLocationEnabled) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 4f))
        }

        mMap.setOnMarkerClickListener { marker ->
            showDeleteConfirmationDialog(marker)
            true
        }
    }

    private fun showDeleteConfirmationDialog(marker: Marker) {
        AlertDialog.Builder(requireContext())
            .setTitle(marker.title)
            .setMessage("¿Estás seguro de que quieres eliminar esta etiqueta?")
            .setPositiveButton("Eliminar") { _, _ ->
                marker.remove()
                markers.remove(marker)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
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

    private fun showInputDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ingrese la información")

        val titleInput = EditText(requireContext())
        titleInput.hint = "Título de la etiqueta"
        val addressInput = EditText(requireContext())
        addressInput.hint = "Dirección"

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(titleInput)
        layout.addView(addressInput)

        builder.setView(layout)

        builder.setPositiveButton("OK") { dialog, _ ->
            val title = titleInput.text.toString()
            val address = addressInput.text.toString()
            if (title.isNotEmpty() && address.isNotEmpty()) {
                addMarker(title, address)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun addMarker(title: String, address: String) {
        try {
            val location = geocoder.getFromLocationName(address, 1)
            if (location!!.isNotEmpty()) {
                val latitude = location[0].latitude
                val longitude = location[0].longitude
                val position = LatLng(latitude, longitude)
                val marker = mMap.addMarker(MarkerOptions().position(position).title(title))
                markers.add(marker!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
            } else {
                Toast.makeText(requireContext(), "Dirección no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al obtener la dirección", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
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
}
