package com.example.radioapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import java.time.format.DateTimeFormatter


/**
 * this adapter class disyplays the list items in the listView with thumbnail image, description, date, and type
 *
 */
class MainListAdapterClass(
    context: Context, private val layoutResource: Int,
    private val dataSource: MutableList<RadFileDataClass>
) : ArrayAdapter<RadFileDataClass>(context, layoutResource, dataSource) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    @RequiresApi(Build.VERSION_CODES.O)
    var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun getCount(): Int {
        return dataSource.size
    }


    override fun getItem(position: Int): RadFileDataClass {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("ViewHolder")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.listview_item, parent, false)

//        if(recentSource[position]==1){
//            rowView.setBackgroundResource(R.drawable.background_highlighted_item_selection)
//        }else if(recentSource[position]==-1){
//            rowView.setBackgroundResource(R.drawable.background_item_selection)
//        }

        // Get view for row item


        val titleTextView = rowView.findViewById(R.id.object_list_title) as TextView

// Get subtitle element
        val subtitleTextView = rowView.findViewById(R.id.object_list_subtitle) as TextView

// Get detail element
        val detailTextView = rowView.findViewById(R.id.object_list_date) as TextView

// Get thumbnail element
        val thumbnailImageView = rowView.findViewById(R.id.object_list_thumbnail) as ImageView

        //Get checkbox element
        val checkbox = rowView.findViewById(R.id.checkBox) as CheckBox


// getting the data from the different listView items and setting them to the view
        var object_item = getItem(position) as RadFileDataClass
        titleTextView.text = object_item.examination
        detailTextView.text = object_item.date?.format(formatter).toString()
        var dropdownStringArray = context.resources.getStringArray(R.array.type_array)
        subtitleTextView.text = dropdownStringArray[object_item.type!!].toString()

        if (object_item.image.imageFiles.size>=1) {
            val currentImage = BitmapFactory.decodeFile(object_item.image.imageFiles[0])
            thumbnailImageView.setImageBitmap(currentImage)
        }

        //setting the checkbox from saved object
        checkbox.isChecked = object_item.favorites
        checkbox.setOnClickListener {
            object_item.favorites = checkbox.isChecked
        }

        //setting the highlights from highlight
        if(object_item.highlight){
            rowView.setBackgroundResource(R.drawable.background_highlighted_item_selection)
        }

        return rowView
    }


}
