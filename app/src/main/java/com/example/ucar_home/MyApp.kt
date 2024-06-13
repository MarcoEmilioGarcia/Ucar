package com.example.ucar_home

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configurar Firestore para habilitar la persistencia
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestore.firestoreSettings = settings

        Log.d("com.example.ucar_home.MyApp", "Firestore settings applied: ${firestore.firestoreSettings.isPersistenceEnabled}")
    }
}
