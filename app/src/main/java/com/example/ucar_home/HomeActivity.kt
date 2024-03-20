package com.example.ucar_login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.grpc.Context


class HomeActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), android.content.Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()


}}