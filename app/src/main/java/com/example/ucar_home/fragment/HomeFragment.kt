package com.example.ucar_home

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postsList = mutableListOf<Pair<PostObject, User>>()

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

    private fun onPostItemClicked(post: PostObject) {
        // Aquí puedes definir lo que sucede cuando se hace clic en un elemento de la lista
        Log.d(ContentValues.TAG, "Post clicked: ${post.idPost}")
    }

    // Method to update the list of posts
    fun updatePosts(newPostsList: MutableList<Pair<PostObject, User>>) {
        postsList.clear() // Clear the existing list
        postsList.addAll(newPostsList) // Add new posts
        postAdapter.notifyDataSetChanged() // Notify adapter of data change
    }
}
