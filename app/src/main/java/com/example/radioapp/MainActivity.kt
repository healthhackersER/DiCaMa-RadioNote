package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView.CHOICE_MODE_SINGLE
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.content.Context
import android.widget.Button
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileOutputStream
import java.io.ObjectOutputStream

//help function

inline fun <reified ObjectClass> genericType() = object: TypeToken<com.example.radioapp.ObjectClass>() {}.type

/* This class implements the starting view of the App with the listView  */
class MainActivity : AppCompatActivity() {
    companion object {
        const val NEW_ITEM = 1
        const val EDIT_ITEM = 2
        const val SHARED_PREFERANCES= "shared_preferences"
        const val KEY_PATH="radioApp"

    }

//    //List and adapter which hold the Data
//    var listItems = mutableListOf<Any>()
    //var listItems = loadFromFile()

    //lateinit var adapter: ArrayAdapter<Any>
    lateinit var adapter: AdapterClass


    //Creating the Main ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //TODO: implementing the aftermentioned Buttons
        val shareButton = findViewById<ImageButton>(R.id.share_Button)
        val searchButton = findViewById<ImageButton>(R.id.search_Button)
        val configButton = findViewById<ImageButton>(R.id.config_Button)
        val deleteButton = findViewById<ImageButton>(R.id.delete_Button)
        val saveButton = findViewById<Button>(R.id.save_Button)

//
//      var listItems = mutableListOf<ObjectClass>()
        var listItems = loadFromFile()

        //initializing the custom AdapterClass for the listView
        //adapter = ArrayAdapter(this, R.layout.listview_item, listItems)
        adapter = AdapterClass(this, R.layout.listview_item, listItems)


        // attach the array adapter with list view
        val listView: android.widget.ListView = findViewById(R.id.listview_1)
        listView.adapter = adapter
        listView.setChoiceMode(CHOICE_MODE_SINGLE)


        // opening the editing Activity when a click is performed on an existing listView Item
        listView.setOnItemClickListener { parent, view, position, id ->

            //Toast.makeText(this, "Clicked item : $position", Toast.LENGTH_SHORT).show()
            val element = parent.getItemAtPosition(position)
            val intent = Intent(this, NewListItem::class.java)
            intent.putExtra("purpose", "editing")
            intent.putExtra("position", position)
            intent.putExtraJson("data", element)

            startActivityForResult(intent, EDIT_ITEM)

        }

        //TODO: what happens when a long click is performed on an item (deleting, sharing)
        listView.setOnItemLongClickListener { parent, view, position, id ->
            Toast.makeText(this, "Position Clicked:" + " " + position, Toast.LENGTH_SHORT).show()
            listView.setItemChecked(position, true)

            return@setOnItemLongClickListener (true)
        }

        saveButton.setOnClickListener{
            val sharedPreferences = getSharedPreferences(SHARED_PREFERANCES, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(listItems)
            editor.putString(KEY_PATH, json)
            editor.apply()
        }


    }

    //Opening editing Activity when a click is performed on the new examination button
    fun onNewItemButton(view: View) {
        Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, NewListItem::class.java)
        intent.putExtra("purpose", "new")
        startActivityForResult(intent, NEW_ITEM)
    }

    //the results which the MainActivityClass gets returned from the different Activities
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            //when the request was from the new examination button
            if (requestCode == NEW_ITEM) {

                val dataObject = data?.getJsonExtra("data", ObjectClass::class.java)
                adapter.add(dataObject)
                //restating the main activity

                onRestart()

            }
            //when the request was from a click on a listView item
            if (requestCode == EDIT_ITEM) {
                val currentPosition = data?.getIntExtra("position", 0)
                val dataObject = data?.getJsonExtra("data", ObjectClass::class.java)
                val currentItem = adapter.getItem(currentPosition!!)
                adapter.remove(currentItem)
                adapter.insert(dataObject, currentPosition!!)
                //restating the main activity
                onRestart()
            }
        }

        //return from the cancel button of the editing Activity
        if (resultCode == Activity.RESULT_CANCELED) {
            onRestart()
        }

        //return from the delete button of the editing Activity
        if (resultCode == Activity.RESULT_FIRST_USER) {
            val currentPosition = data?.getIntExtra("position", 0)
            val currentItem = adapter.getItem(currentPosition!!)
            adapter.remove(currentItem)
            onRestart()
        }


    }

    private fun loadFromFile(): MutableList<ObjectClass> {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERANCES, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_PATH, null)


        val objectType = object : TypeToken<MutableList<ObjectClass>>() {}.type

        var listItems = gson.fromJson<MutableList<ObjectClass>>(json, objectType)

        if (listItems == null) {
            listItems = mutableListOf<ObjectClass>()
        }
        return listItems
    }

}



