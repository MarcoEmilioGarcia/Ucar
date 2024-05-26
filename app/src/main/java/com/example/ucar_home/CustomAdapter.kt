package com.example.ucar_home


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class CarAdapter(private val carList: List<CarObject>) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carImageView: ImageView = itemView.findViewById(R.id.imageView)
        val carNameTextView: TextView = itemView.findViewById(R.id.nombreUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val currentCar = carList[position]
        holder.carNameTextView.text = currentCar.title
        Glide.with(holder.itemView.context).load(currentCar.imageUrl).into(holder.carImageView)
    }

    override fun getItemCount(): Int {
        return carList.size
    }
}

