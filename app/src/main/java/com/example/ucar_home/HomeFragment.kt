package com.example.ucar_home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postsList = mutableMapOf<PostObject, User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.publicaciones)
        postAdapter = PostAdapter(postsList)
        recyclerView.adapter = postAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    fun updatePosts(newPostsList: MutableMap<PostObject, User>) {
        postsList = newPostsList
        postAdapter.updatePosts(postsList)
    }
}
