package com.example.radioapp

import android.graphics.BitmapFactory
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.FileDescriptor

/**
 * Recycler adapter class implements an adapter for a recyclerView displayed
 * in the [CreatePDF] activity
 * @property radData the data of the examination which is selected
 * @property includeImageList list of Booleans which image items are checked
 * @propert includeMarkerList list of Booleans which marker items are checked
 * @constructor Create empty Recycler adapter
 */

class PDFAdapter (
    private val radData: RadFileDataClass,
    private var includeImageList: BooleanArray,
    private var includeMarkerList: BooleanArray,
    private var includeImageDescriptionList: BooleanArray

) :
    RecyclerView.Adapter<PDFAdapter.ViewHolder>() {

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
            LayoutInflater.from(parent.context).inflate(R.layout.pdf_recycler_view_item, parent, false)

        return ViewHolder(v)

    }

    /**
     * gets the size of the Recyclerview
     * @return size
     */
    override fun getItemCount(): Int {
        return radData.image.imageFiles.size
    }

    /**
     * method to bind the viewHolder to the view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val currentImagePath=radData.image.imageFiles[position]
        val currentImage = BitmapFactory.decodeFile(currentImagePath)
        holder.itemImage.setImageBitmap(currentImage)
        holder.imageText.text = radData.image.imageDescription[position]
        holder.checkImage.isChecked = includeImageList[position] != false
        holder.checkMarker.isChecked = includeMarkerList[position] != false
        holder.checkDescription.isChecked=includeImageDescriptionList[position] != false

    }

    /**
     * View Holder class for the RecyclerViewer on [CreatePDF]
     * sets checkbox values to the bool array
     * @constructor
     *
     * @param itemView
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
       {
           val itemImage: ImageView = itemView.findViewById(R.id.pdf_recycler_imageView)
           val imageText:TextView=itemView.findViewById(R.id.pdf_recycler_text)
           var checkImage: CheckBox = itemView.findViewById(R.id.pdf_recycler_checkImage)
           var checkMarker: CheckBox = itemView.findViewById(R.id.pdf_recycler_checkMarker)
           var checkDescription: CheckBox =
               itemView.findViewById(R.id.pdf_recycler_checkImageDescription)


           init {
               checkImage.setOnClickListener {
                   if (!includeImageList[adapterPosition]) {
                       checkImage.isChecked = true
                       includeImageList[adapterPosition] = true
                   } else {
                       checkImage.isChecked = false
                       includeImageList[adapterPosition] = false
                   }
               }

               checkMarker.setOnClickListener {
                   if (!includeMarkerList[adapterPosition]) {
                       checkMarker.isChecked = true
                       includeMarkerList[adapterPosition] = true
                   } else {
                       checkMarker.isChecked = false
                       includeMarkerList[adapterPosition] = false
                   }
               }
               checkDescription.setOnClickListener {
                   if (!includeImageDescriptionList[adapterPosition]) {
                       checkDescription.isChecked = true
                       includeImageDescriptionList[adapterPosition] = true
                   } else {
                       checkDescription.isChecked = false
                       includeImageDescriptionList[adapterPosition] = false
                   }
               }

           }


       }
}


