package com.example.radioapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_new_list_item.*


/**
 * this adapter class disyplays the list items in the listView with thumbnail image, description, date, and type
 *
 */
class AdapterClass (context: Context, private val layoutResource: Int,
                                        private val dataSource: MutableList<ObjectClass>) : ArrayAdapter<ObjectClass>(context,layoutResource,dataSource) {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }


    override fun getItem(position: Int): ObjectClass {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Get view for row item
        val rowView = inflater.inflate(R.layout.listview_item, parent, false)

        val titleTextView = rowView.findViewById(R.id.object_list_title) as TextView

// Get subtitle element
        val subtitleTextView = rowView.findViewById(R.id.object_list_subtitle) as TextView

// Get detail element
        val detailTextView = rowView.findViewById(R.id.object_list_date) as TextView

// Get thumbnail element
        val thumbnailImageView = rowView.findViewById(R.id.object_list_thumbnail) as ImageView

// getting the data from the different listView items and setting them to the view
        val object_item = getItem(position) as ObjectClass
        titleTextView.text=object_item.examination
        detailTextView.text= object_item.date
        var dropdownStringArray= context.resources.getStringArray(R.array.type_array)
        subtitleTextView.text=dropdownStringArray[object_item.type!!].toString()
        if (object_item.image!=null){
            val currentImage = BitmapFactory.decodeFile(object_item.image)
            thumbnailImageView.setImageBitmap(currentImage)
        }


        return rowView
        }


    }
