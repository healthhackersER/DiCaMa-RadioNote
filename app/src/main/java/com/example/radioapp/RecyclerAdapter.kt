package com.example.radioapp

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(private var images: MutableList<String>, private var text: MutableList<String>):
RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemImage: ImageView = itemView.findViewById(R.id.iv_image)
        val itemDescription: TextView = itemView.findViewById(R.id.iv_description)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return images.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemDescription.text=text[position]
        val currentImage = BitmapFactory.decodeFile(images[position])
        holder.itemImage.setImageBitmap(currentImage)
    }
}

