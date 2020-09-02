package com.example.radioapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.time.format.DateTimeFormatter


/**
 * this adapter class disyplays the list items in the listView with thumbnail image, description, date, and type
 *
 */
class CameraListAdapterClass(
    context: Context, private val layoutResource: Int,
    private val dataSource: MutableList<String>
) : ArrayAdapter<String>(context, layoutResource, dataSource) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getCount(): Int {
        return dataSource.size
    }


    override fun getItem(position: Int): String {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("ViewHolder")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Get view for row item
        val rowView = inflater.inflate(R.layout.camera_listview_item, parent, false)


        // Get thumbnail element
        val thumbnailImageView = rowView.findViewById(R.id.camera_list_image) as ImageView

        //Get checkbox element
        val delete = rowView.findViewById(R.id.delete_list_image_button) as ImageButton


// getting the data from the different listView items and setting them to the view
        var fileName = getItem(position)
        try{
            val currentImage = BitmapFactory.decodeFile(fileName)
            thumbnailImageView.setImageBitmap(currentImage)
        }catch (e:Exception){
            println("Exception at getView in CameraListAdapterClass")
        }

        return rowView
    }


}
