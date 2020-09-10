package com.example.radioapp

import android.graphics.BitmapFactory
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_camera.*

/**
 * Recylcer adapter class implements an adapter for a recyclerView displayed
 * in the [CameraEditingActivity]
 *
 * @property images the pathnames of the images in a list
 * @property text the description of the images in text
 * @property selection if the item is selected
 * @property listener if the item is clicked on
 * @constructor Create empty Recycler adapter
 */
class RecyclerAdapter(
    var images: MutableList<String>,
    private var text: MutableList<String>,
    private val selection: MutableList<Boolean>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    /**
     * get the Item at position
     * @param position
     * @return the Image as String (the path of the file)
     */
    override fun getItemId(position: Int): Long = position.toLong()

    /**
     * method to create a viewHolder
     * @return viewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.camera_listview_item, parent, false)

        return ViewHolder(v)

    }

    /**
     * gets the size of the Recyclerview
     * @return size
     */
    override fun getItemCount(): Int {
        return images.size
    }

    /**
     * method to bind the viewHolder to the view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val currentImage = BitmapFactory.decodeFile(images[position])
        holder.itemImage.setImageBitmap(currentImage)
        holder.itemView.isActivated = selection[position] == true

    }

    /**
     * On item click listener create an interface for the item click listener
     *
     * @constructor Create empty On item click listener
     */
    interface OnItemClickListener {
        /**
         * On item click behaviour defined in [CameraEditingActivity]
         *
         * @param position
         */
        fun onItemClick(position: Int)
        fun onButtonClick( position:Int)
    }

    /**
     * View Holder class for the RecyclerViewer on [CameraEditingActivity]
     *
     * @constructor
     *
     * @param itemView
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val itemImage: ImageView = itemView.findViewById(R.id.camera_list_image)
        private val deleteButton: ImageButton= itemView.findViewById(R.id.delete_list_image_button)




        init {
            itemView.setOnClickListener(this)
            deleteButton.setOnClickListener {
                val position= adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onButtonClick(position)
                }
            }

        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }

        }


    }
}

