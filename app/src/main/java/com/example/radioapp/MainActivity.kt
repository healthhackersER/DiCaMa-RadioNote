package com.example.radioapp

import android.Manifest
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
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.text.method.TextKeyListener.clear
import android.widget.AbsListView.CHOICE_MODE_MULTIPLE
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

//help function

inline fun <reified ObjectClass> genericType() =
    object : TypeToken<com.example.radioapp.ObjectClass>() {}.type

/**
 * This Class displays a listView with ObjectClass Items. The Adapter is defined in the AdapterClass.
 */

class MainActivity : AppCompatActivity() {
    //different constants that are used over the project
    companion object {
        const val NEW_ITEM = 1
        const val EDIT_ITEM = 2
        const val SHARED_PREFERANCES = "shared_preferences"
        const val KEY_PATH = "radioApp"
        const val REQUEST_IMAGE_CAPTURE = 3
    }

    //Checking for the permissions at runtime methods
    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    //the variable for the adapter
    private lateinit var adapter: AdapterClass

    //onCreate function of the Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //values of different Buttons
        val shareButton = findViewById<ImageButton>(R.id.share_Button)
        val searchButton = findViewById<ImageButton>(R.id.search_Button)
        val configButton = findViewById<ImageButton>(R.id.config_Button)
        val deleteButton = findViewById<ImageButton>(R.id.delete_Button)
        val saveButton = findViewById<Button>(R.id.save_Button)
        val favoriteButton = findViewById<ImageButton>(R.id.favorite_Button)
        val sortButton = findViewById<ImageButton>(R.id.sort_Button)

        //toggle value for the favorite Button to toggle bettwen date and favorite
        var toggle = false

        //requesting the permission at runtime
        if (hasNoPermissions()) {
            requestPermission()
        }

        //loading the data from File
        var listItems = loadFromFile()

        //initializing the listView adapter
        adapter = AdapterClass(this, R.layout.listview_item, listItems)

        //attach the array adapter with list view
        val listView: android.widget.ListView = findViewById(R.id.listview_1)
        listView.itemsCanFocus = true
        listView.adapter = adapter

        // opening the editing Activity when a click is performed on an existing listView Item
        listView.setOnItemClickListener { parent, view, position, id ->

            val element: ObjectClass = parent.getItemAtPosition(position) as ObjectClass
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

        //saving the listitem object to sharedPreferences on button click
        saveButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences(SHARED_PREFERANCES, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(listItems)
            editor.putString(KEY_PATH, json)
            editor.apply()
        }

        //sorting after favorites
        favoriteButton.setOnClickListener {
            if (toggle == false) {
                adapter.sort(compareByDescending({ it.favorites }))
                toggle = true
            } else {
                adapter.sort(compareByDescending({ it.date }))
            }
        }

        //sortting after date
        sortButton.setOnClickListener {
            adapter.sort(compareByDescending({ it.date }))
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
    @RequiresApi(Build.VERSION_CODES.O)
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
            for (item in currentItem.image!!) {
                val path = Paths.get(item)
                if (path.delete()) {
                    println("Deleted ${path.fileName}")
                } else {
                    println("Could not delete ${path.fileName}")
                }
            }
            adapter.remove(currentItem)
            onRestart()
        }


    }

    //method load the list items from sharedPreferences
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



