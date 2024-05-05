package com.example.ucar_home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ucar_home.R
import io.grpc.Context


class HomeActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), android.content.Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()


}}