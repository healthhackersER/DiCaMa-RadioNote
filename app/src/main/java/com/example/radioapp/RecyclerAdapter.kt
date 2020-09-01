package com.example.radioapp

import android.graphics.BitmapFactory
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(var images: MutableList<String>, private var text: MutableList<String>, private val listener: OnItemClickListener):
RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }
    override fun getItemId(position: Int): Long = position.toLong()





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item,parent,false)
        return ViewHolder(v)

    }



    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val currentImage = BitmapFactory.decodeFile(images[position])
        holder.itemImage.setImageBitmap(currentImage)

        tracker?.let {
            holder.bind(position, it.isSelected(position.toLong()))
        }

    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val itemImage: ImageView = itemView.findViewById(R.id.iv_image)


        init {
            itemView.setOnClickListener(this)
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
                override fun inSelectionHotspot(e: MotionEvent): Boolean { return true }
            }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }



        }

        fun bind(value: Int, isActivated: Boolean = false) {
            itemView.isActivated = isActivated

        }
    }
}

