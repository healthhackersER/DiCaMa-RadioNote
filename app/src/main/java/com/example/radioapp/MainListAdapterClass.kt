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
 * Main list adapter class is the adapter for the listView in the [MainActivity]
 *
 * @property layoutResource
 * @property dataSource used data
 * @constructor
 *
 * @param context
 */
class MainListAdapterClass(
    context: Context, private val layoutResource: Int,
    private val dataSource: MutableList<RadFileDataClass>
) : ArrayAdapter<RadFileDataClass>(context, layoutResource, dataSource) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    @RequiresApi(Build.VERSION_CODES.O)
    var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    //CAVE: tightly coupled
    val castedContext = context as MainActivity
    /**
     * gets the size of the Recyclerview
     * @return size
     */
    override fun getCount(): Int {
        return dataSource.size
    }

    /**
     * get the Item at position
     * @param position
     * @return the Image as String (the path of the file)
     */
    override fun getItem(position: Int): RadFileDataClass {
        return dataSource[position]
    }

    /**
     * get the Item ID
     * @param position
     * @return position as long
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    /**
     * gets the View at marked position in the recyclerView
     * @param position
     * @param convertView
     * @param parent
     * @return the View of the Item
     */
    @SuppressLint("ViewHolder")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val rowView = inflater.inflate(R.layout.listview_item, parent, false)

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
        //checking if an the examination has an image and displaying it
        if (object_item.image.imageFiles.size >= 1) {
            val currentImage = BitmapFactory.decodeFile(object_item.image.imageFiles[0])
            thumbnailImageView.setImageBitmap(currentImage)
        }

        //setting the checkbox from saved object
        checkbox.isChecked = object_item.favorites
        checkbox.setOnClickListener {
            object_item.favorites = checkbox.isChecked
            castedContext.saveToFile()
        }

        //setting the highlights from highlight
        if (object_item.highlight) {
            rowView.setBackgroundResource(R.drawable.background_highlighted_item_selection)
        }

        return rowView
    }


}
