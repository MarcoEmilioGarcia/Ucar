package com.example.ucar_home

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val dataLists = DataProvider.dataLists
        Log.d(ContentValues.TAG, "Crea una instancia de tu adaptador y configúralo en el RecyclerView principal")
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CustomAdapter(this, dataLists)
        recyclerView.setAdapter(adapter)





    }
}