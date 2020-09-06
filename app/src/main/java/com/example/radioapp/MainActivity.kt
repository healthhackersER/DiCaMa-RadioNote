package com.example.radioapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Paths

//help function

inline fun <reified ObjectClass> genericType() =
    object : TypeToken<com.example.radioapp.RadFileDataClass>() {}.type

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

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    //the variable for the adapter
    private lateinit var adapter: MainListAdapterClass
    private lateinit var listItems: MutableList<RadFileDataClass>
    private lateinit var highlightList:MutableList<Int>


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menue,menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings_action){
            Toast.makeText(this, "item Add Clicked", Toast.LENGTH_SHORT).show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    //onCreate function of the Activity
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //values of different Buttons
        val shareButton = findViewById<ImageButton>(R.id.share_Button)
        val searchButton = findViewById<ImageButton>(R.id.search_Button)

        val deleteButton = findViewById<ImageButton>(R.id.delete_Button)
        val saveButton = findViewById<Button>(R.id.save_Button)
        val favoriteButton = findViewById<ImageButton>(R.id.favorite_Button)
        val sortButton = findViewById<ImageButton>(R.id.sort_Button)

        //toggle value for the favorite Button to toggle bettwen date and favorite
        var toggle = false
        shareButton.visibility = View.INVISIBLE
        deleteButton.visibility = View.INVISIBLE

        //requesting the permission at runtime
        if (hasNoPermissions()) {
            requestPermission()
        }

        //loading the data from File
        listItems = loadFromFile()
        var temp = IntArray(listItems.size) { _ -> -1 }
        highlightList = temp.toMutableList()

        //initializing the listView adapter
        adapter = MainListAdapterClass(this, R.layout.listview_item, listItems, highlightList)

        //attach the array adapter with list view
        val listView: android.widget.ListView = findViewById(R.id.listview_1)
        listView.itemsCanFocus = true
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        // opening the editing Activity when a click is performed on an existing listView Item
        listView.setOnItemClickListener { parent, view, position, id ->

            val element: RadFileDataClass = parent.getItemAtPosition(position) as RadFileDataClass
            val intent = Intent(this, NewListItem::class.java)


            intent.putExtra("purpose", "editing")
            intent.putExtra("position", position)
            intent.putExtraJson("data", element)

            startActivityForResult(intent, EDIT_ITEM)

        }


        listView.setOnItemLongClickListener { parent, view, position, id ->
            Toast.makeText(this, "Position Clicked:" + " " + position, Toast.LENGTH_SHORT).show()
            if (listView.isItemChecked(position)) {
                listView.setItemChecked(position, false)
                shareButton.visibility = View.INVISIBLE
                deleteButton.visibility = View.INVISIBLE
            } else {
                listView.setItemChecked(position, true)
                shareButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
            }

            return@setOnItemLongClickListener (true)
        }

        //TODO fill with life
        deleteButton.setOnClickListener {
            val itemId = listView.checkedItemPosition
            println("Test")
        }
        //saving the listitem object to sharedPreferences on button click
        saveButton.setOnClickListener {
            saveToFile()
        }


        //sorting after favorites
        favoriteButton.setOnClickListener {
            if (toggle == false) {
                adapter.sort(compareByDescending({ it.favorites }))
                toggle = true
            } else {
                adapter.sort(compareByDescending({ it.date }))
                toggle = false
            }
        }

        //sortting after date
        var toggle_sort=false
        sortButton.setOnClickListener {
            //array with the position of the most recent type Item in the listView. The Index of the array
            //marks the position of the Type defined by the @values keyStringMap
            if(toggle_sort==false) {
                val recent = searchMostRecent()
                var recentList = mutableListOf<Int>()
                for (i in recent.indices) {
                    if (recent[i] != -1) {
                        recentList.add(recent[i]!!)
                    }
                }

                for (i in recentList.indices) {
                    highlightList[recentList[i]] = 1
                }
                adapter.notifyDataSetChanged()

                toggle_sort=true
            }else if (toggle_sort==true){
                temp= IntArray(listItems.size) { _ -> -1 }
                highlightList = temp.toMutableList()

                toggle_sort=false
                adapter.notifyDataSetChanged()
            }

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
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            //when the request was from the new examination button
            if (requestCode == NEW_ITEM) {

                val dataObject = data?.getJsonExtra("data", RadFileDataClass::class.java)
                adapter.add(dataObject)
                highlightList.add(-1)
                //restating the main activity
                adapter.sort(compareByDescending({ it.date }))
                saveToFile()
                onRestart()

            }
            //when the request was from a click on a listView item
            if (requestCode == EDIT_ITEM) {
                val currentPosition = data?.getIntExtra("position", 0)
                val dataObject = data?.getJsonExtra("data", RadFileDataClass::class.java)
                val currentItem = adapter.getItem(currentPosition!!)
                adapter.remove(currentItem)
                adapter.insert(dataObject, currentPosition!!)
                //restating the main activity
                adapter.sort(compareByDescending({ it.date }))
                saveToFile()
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
            for (item in currentItem.image.imageFiles!!) {
                val path = Paths.get(item)
                if (path.delete()) {
                    println("Deleted ${path.fileName}")
                } else {
                    println("Could not delete ${path.fileName}")
                }
            }
            adapter.remove(currentItem)
            highlightList.remove(highlightList.size-1)
            saveToFile()
            onRestart()
        }


    }
    private fun saveToFile(){
        val sharedPreferences = getSharedPreferences(SHARED_PREFERANCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(listItems)
        editor.putString(KEY_PATH, json)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun searchMostRecent(): Array<Int?> {
        val res: Resources = resources
        val dropdownArray=res.getStringArray(R.array.type_array)
        var currentRecentItems= arrayOfNulls<Int>(dropdownArray.size)
        currentRecentItems.fill(-1,0,currentRecentItems.size)
        for (i in listItems.indices){
            val currentData= listItems[i].type
            if (currentRecentItems[currentData!!]==-1){
                currentRecentItems[currentData!!]=i
            }else if(currentRecentItems[i]!=-1){
                if( listItems[currentRecentItems[currentData!!]!!].date?.compareTo(listItems[i].date)!! < 0){
                    currentRecentItems[currentData!!]=i
                }


            }

        }
        return currentRecentItems
    }

    //method load the list items from sharedPreferences
    private fun loadFromFile(): MutableList<RadFileDataClass> {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERANCES, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_PATH, null)


        val objectType = object : TypeToken<MutableList<RadFileDataClass>>() {}.type

        var listItems = gson.fromJson<MutableList<RadFileDataClass>>(json, objectType)

        if (listItems == null) {
            listItems = mutableListOf<RadFileDataClass>()
        }
        return listItems
    }

}



