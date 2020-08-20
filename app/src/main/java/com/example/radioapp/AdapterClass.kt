package com.example.radioapp

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class AdapterClass (context: Context, private val layoutResource: Int,
                                        private val dataSource: MutableList<Any>) : ArrayAdapter<Any>(context,layoutResource,dataSource) {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //1

    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
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
        val object_item = getItem(position) as ObjectClass

        titleTextView.text=object_item.examination
        detailTextView.text= object_item.date
        var dropdownStringArray= context.resources.getStringArray(R.array.type_array)
        subtitleTextView.text=dropdownStringArray[object_item.type!!].toString()

        return rowView
        }


    }
