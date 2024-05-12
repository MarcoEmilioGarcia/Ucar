package com.example.ucar_home

import com.example.ucar_home.databinding.FragmentMapsBinding
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsFragment : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private lateinit var geocoder: Geocoder
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val DEFAULT_LOCATION = LatLng(40.416775, -3.70379) // Madrid, España
    private val DEFAULT_ZOOM = 11f
    private val markers = mutableListOf<Marker>()



    /* Este método se llama cuando la actividad se está creando. Es donde se inicializan las
    vistas, se configuran los listeners y se obtiene la referencia al mapa utilizando el fragmento
    de soporte SupportMapFragment*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geocoder = Geocoder(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
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

    /* Este método se llama cuando el mapa está listo para ser utilizado. Aquí se configura la capa
    de ubicación en el mapa y se centra en la ubicación predeterminada (la Península Ibérica si la
    ubicación del usuario no está habilitada)*/
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Habilita la capa de ubicación en el mapa
        enableMyLocation()

        // Si no se concede permiso, centrar el mapa en la Península Ibérica
        if (!mMap.isMyLocationEnabled) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 4f))
        }

        // Configura el listener para los clics en las etiquetas
        mMap.setOnMarkerClickListener { marker ->
            showDeleteConfirmationDialog(marker)
            true // Devuelve true para indicar que el evento se ha manejado
        }
    }


    /*Muestra un cuadro de diálogo de confirmación cuando el usuario hace clic en una etiqueta. El
    cuadro de diálogo muestra el título de la etiqueta y ofrece la opción de eliminarla o cancelar
    la acción.*/
    private fun showDeleteConfirmationDialog(marker: Marker) {
        AlertDialog.Builder(this)
            .setTitle(marker.title) // Mostrar el título de la etiqueta
            .setMessage("¿Estás seguro de que quieres eliminar esta etiqueta?")
            .setPositiveButton("Eliminar") { _, _ ->
                marker.remove() // Elimina el marcador del mapa
                markers.remove(marker) // Remueve el marcador de la lista
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /*Habilita la capa de ubicación en el mapa y centra la cámara en la ubicación predeterminada si
    no se concede el permiso de ubicación*/
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
    }

    /*Muestra un cuadro de diálogo para que el usuario ingrese la información de la etiqueta,
    incluido el título y la dirección. Si el usuario proporciona ambos campos, se agrega una nueva
    etiqueta al mapa.*/
    private fun showInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ingrese la información")

        val titleInput = EditText(this)
        titleInput.hint = "Título de la etiqueta"
        val addressInput = EditText(this)
        addressInput.hint = "Dirección"

        val layout = LinearLayout(this)
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
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    /*Agrega una nueva etiqueta al mapa con el título y la dirección proporcionados por el usuario.
    La ubicación se obtiene a partir de la dirección utilizando Geocoder.*/
    private fun addMarker(title: String, address: String) {
        try {
            val location = geocoder.getFromLocationName(address, 1)
            if (location!!.isNotEmpty()) {
                val latitude = location!![0].latitude
                val longitude = location[0].longitude
                val position = LatLng(latitude, longitude)
                val marker = mMap.addMarker(MarkerOptions().position(position).title(title))
                markers.add(marker!!) // Agrega el marcador a la lista
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
            } else {
                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show()
        }
    }

    /*Se llama cuando el usuario responde a la solicitud de permiso de ubicación. Si se concede el
    permiso, se habilita la capa de ubicación en el mapa*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    /*Filtra las etiquetas en el mapa según el texto proporcionado por el usuario en el SearchView.
    Las etiquetas cuyos títulos contienen el texto de búsqueda se muestran, mientras que las demás
    se ocultan.*/
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
