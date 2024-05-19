package com.example.ucar_home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    @SuppressLint("MissingInflatedId")
    override fun getInfoContents(marker: Marker): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.info_window_layout, null)

        marker?.let {
            val event = it.tag as? Event
            event?.let { event ->
                view.findViewById<TextView>(R.id.titleTextView).text = event.title
                view.findViewById<TextView>(R.id.dateTextView).text = "Date: ${event.date}"
                view.findViewById<TextView>(R.id.addressTextView).text = "Address: ${event.address}"
                view.findViewById<TextView>(R.id.descriptionTextView).text = "Description: ${event.description}"
            }
        }
        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}