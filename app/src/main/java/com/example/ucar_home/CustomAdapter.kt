package com.example.ucar_home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

/*
class CustomAdapter(private val context: Context, private val images: List<Int>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        //INFLAMOS LO QUE SERA EL ELEMENTO DEL RECYCLEVIEW
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, viewGroup, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //ASIGNACION DE LOS RECURSOS DE LA LISTA
        val currentImage = images[position]
        holder.imageView1.setImageResource(currentImage)
        holder.imageView2.setImageResource(currentImage)
        holder.imageView3.setImageResource(currentImage)
        val slideInBottom = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom)
        val slideOutTop = AnimationUtils.loadAnimation(context, R.anim.slide_out_top)
        val animationDuration = 2000L // Duración de la animación en milisegundos
        // Función para ejecutar las animaciones
        fun runAnimation() {
            for (i in 1..5) {
                holder.imageView1.startAnimation(slideInBottom)
                holder.imageView2.startAnimation(slideInBottom)
                holder.imageView3.startAnimation(slideInBottom)
                slideInBottom.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        holder.imageView1.startAnimation(slideOutTop)
                        holder.imageView2.startAnimation(slideOutTop)
                        holder.imageView3.startAnimation(slideOutTop)
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }
        // Ejecutar la animación inicial y luego repetirla continuamente
        runAnimation()
        Log.d("TAG", "Error?") }
    override fun getItemCount(): Int {
        return images.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var linearLayout: LinearLayout = itemView.findViewById(R.id.Linnear01)
        var imageView1: ImageView = itemView.findViewById(R.id.imageView)
        var imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        var imageView3: ImageView = itemView.findViewById(R.id.imageView3) }


}
*/