package com.example.ucar_home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class postSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_search)

        val postId = intent.getStringExtra("postId")
        Log.d("postSearchActivity", "Post ID recibido: $postId")
        // Usa el postId como necesites
    }
}
