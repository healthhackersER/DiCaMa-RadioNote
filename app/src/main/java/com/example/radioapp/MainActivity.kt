package com.example.radioapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.radioapp.ExaminationEditingActivity.Companion.EDIT_TEXT
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//help function

/**
 * Help function to cast to RadFileDataClass
 *
 * @param ObjectClass
 */
inline fun <reified ObjectClass> genericType() =
    object : TypeToken<com.example.radioapp.RadFileDataClass>() {}.type


/**
 * Main activity which displays the start window with a listView of all the examination Items
 * different functions to sort, mark and share the items are implemented. If an Item on the ListView
 * gets clicked on the [ExaminationEditingActivity] gets called. The data if the ListItem Items is
 * handled as [RadFileDataClass]
 *
 * @constructor Create empty Main activity
 */

class MainActivity : AppCompatActivity() {
    //different constants that are used over the project
    companion object {
        const val NEW_ITEM = 1
        const val EDIT_ITEM = 2
        const val SHARED_PREFERANCES = "shared_preferences"
        const val KEY_PATH = "radioApp"
        const val REQUEST_IMAGE_CAPTURE = 3
        const val TEST=7
        const val CREATE_PDF =8
        const val VIEW_PDF=9
        const val STORAGE_CODE=10

    }
    private var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")


    //Checking for the permissions at runtime methods
    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    /**
     * method to check if the permissions are not granted
     *
     * @return Boolean
     *
     */
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

    /**
     * Request permission method
     *
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    //the variable for the adapter
    private lateinit var adapter: MainListAdapterClass
    private lateinit var listItems: MutableList<RadFileDataClass>
    private lateinit var foundHighlight: MutableList<Array<Array<Int>>>
    var toggleSearch= false

    /**
     * method to create the options menue in the toolbar
     * @param menue
     * @return Boolean
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menue, menu)
        return true
    }

    /**
     * method which gets called when an item on the menue gets clicked on
     * @param item
     * @return Boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.settings_action){
            Toast.makeText(this, "To be implemented in the future", Toast.LENGTH_SHORT).show()
            return true
        }
        if (id==R.id.mi_search){
            if(toggleSearch==false) {
                ma_search_linearLayout.visibility = View.VISIBLE
                toggleSearch=true
            }else if (toggleSearch==true){
                foundHighlight= MutableList(listItems.size) { arrayOf(arrayOf(-1,-1), arrayOf(-1,-1), arrayOf(-1,-1)) }
                adapter.notifyDataSetChanged()
                ma_search_text.text=""
                ma_search_linearLayout.visibility=View.GONE
                toggleSearch=false
            }

        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * creates an instance of the [MainActivity] class object
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //toggle value for the favorite Button to toggle bettwen date and favorite
        var toggle = false

        //making the share and delete button invisible by default
        share_Button.visibility = View.GONE
        delete_Button.visibility = View.GONE
        sort_Button.visibility=View.VISIBLE
        favorite_Button.visibility=View.VISIBLE
        ma_search_linearLayout.visibility=View.GONE
        //requesting the permission at runtime
        if (hasNoPermissions()) {
            requestPermission()
        }

        //loading the data from File
        try {
            listItems = loadFromFile()
        }catch (e: Exception) {
            //if anything goes wrong causing exception, get and show exception message
            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
        }
        foundHighlight = MutableList(listItems.size) { arrayOf(arrayOf(-1,-1), arrayOf(-1,-1), arrayOf(-1,-1)) }
        //initializing the listView adapter
        adapter = MainListAdapterClass(this, R.layout.listview_item, listItems)

        //attach the array adapter with list view
        val listView: android.widget.ListView = findViewById(R.id.listview_1)
        listView.itemsCanFocus = true
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        /**
         * when the floating action button is clicked [ExaminationEditingActivity] gets called on with
         * the purpose if "new"
         *
         */
        ma_add_new_button.setOnClickListener{
            onNewItemButton()
        }

        /**
         * if a listView Item gets clicked on the [ExaminationEditingActivity] gets called on with
         * the purpose if "editing"
         *
         */
        listView.setOnItemClickListener { parent, view, position, id ->
            listView.setItemChecked(position, false)
            val element: RadFileDataClass = parent.getItemAtPosition(position) as RadFileDataClass
            val intent = Intent(this, ExaminationEditingActivity::class.java)


            intent.putExtra("purpose", "editing")
            intent.putExtra("position", position)
            intent.putExtraJson("data", element)
            try {
                startActivityForResult(intent, EDIT_ITEM)
            }catch (e: Exception) {
                //if anything goes wrong causing exception, get and show exception message
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
            //undo autocheck of item
//            listView.setItemChecked(position, false)
        }

        /**
         * if a long click is performed on an item the delete and share button are visible and the
         * item gets selected
         *
         */
        listView.setOnItemLongClickListener { parent, view, position, id ->

            if (listView.isItemChecked(position)) {
                listView.setItemChecked(position, false)
                share_Button.visibility = View.GONE
                delete_Button.visibility = View.GONE
                favorite_Button.visibility=View.VISIBLE
                sort_Button.visibility=View.VISIBLE
            } else {
                listView.setItemChecked(position, true)
                share_Button.visibility = View.VISIBLE
                delete_Button.visibility = View.VISIBLE
                sort_Button.visibility=View.GONE
                favorite_Button.visibility=View.GONE
            }

            return@setOnItemLongClickListener (true)
        }

        /**
         * item gets deleted on button click
         */
        delete_Button.setOnClickListener {
            val position = listView.checkedItemPosition
            try{
            onDelete(position)}
            catch (e: Exception) {
                //if anything goes wrong causing exception, get and show exception message
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

        share_Button.setOnClickListener {
            val position=listView.checkedItemPosition
            try {
                onShareButton(position)
            }catch (e: Exception) {
                //if anything goes wrong causing exception, get and show exception message
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

        /**
         * toggle between sorting for favorites/date
         */

        favorite_Button.setOnClickListener {
            if (toggle == false) {
                adapter.sort(compareByDescending({ it.favorites }))
                toggle = true
                listView.smoothScrollToPosition(0)
            } else {
                adapter.sort(compareByDescending({ it.date }))
                toggle = false
            }
        }

        ma_search_text.setOnClickListener{
            editSearchDialog(ma_search_text, EDIT_TEXT)

        }


        ma_search_cancel_button.setOnClickListener{
            foundHighlight= MutableList(listItems.size) { arrayOf(arrayOf(-1,-1), arrayOf(-1,-1), arrayOf(-1,-1)) }
            adapter.notifyDataSetChanged()
            ma_search_text.text=""
            ma_search_linearLayout.visibility=View.GONE
            toggleSearch=false
        }


        /**
         * marks the most recent items of each examination type
         * toggle between the marked and not marked
         */
        var toggle_sort=false
        sort_Button.setOnClickListener {
            //array with the position of the most recent type Item in the listView. The Index of the array
            //marks the position of the Type defined by the @values keyStringMap
            if(toggle_sort==false) {
                //getting the positions of the items that are supposed to be marked in recentList
                val recent = searchMostRecent()
                var recentList = mutableListOf<Int>()
                for (i in recent.indices) {
                    if (recent[i] != -1) {
                        recentList.add(recent[i]!!)
                    }
                }

                //iterating through the recent list and setting them to highlighted
                for (i in recentList.indices) {
                    val currentElement=listItems.get(recentList[i])
                    currentElement.highlight=true
                    listItems[recentList[i]] = currentElement

                }
                adapter.notifyDataSetChanged()
                toggle_sort=true
                //reversing highlight
            }else if (toggle_sort==true){

                for (i in listItems.indices){
                    val currentElement=listItems.get(i)
                    currentElement.highlight=false
                    listItems[i] = currentElement
                }
                toggle_sort=false
                adapter.notifyDataSetChanged()
            }

        }






    }
    /**
     * calls the [CreatePDF] activity at the given position
     * @param position the position of the Item in the ListView
     */
    private fun onShareButton(position: Int){
        val intent = Intent(this, CreatePDF::class.java)
        val radData = adapter.getItem(position)
        intent.putExtraJson("radData", radData)

        startActivityForResult(intent, CREATE_PDF)
    }

    /**
     * deletes the Item at the given position
     * @param position the position of the Item in the ListView
     */
    private fun onDelete(position: Int){
        val currentItem = adapter.getItem(position!!)
        for (item in currentItem.image.imageFiles!!) {
            val path = Paths.get(item)
            if (path.delete()) {
                println("Deleted ${path.fileName}")
            } else {
                println("Could not delete ${path.fileName}")
            }
        }
        adapter.remove(currentItem)
        //highlightList.remove(highlightList.size-1)
        saveToFile()
        onRestart()
    }

    fun getHighlight():MutableList<Array<Array<Int>>>{
        return foundHighlight
    }

    /**
     * On new Item button click calls the [ExaminationEditingActivity] with the purpose new
     *
     * @param view the button which it is connected to
     */
    fun onNewItemButton() {
        val intent = Intent(this, ExaminationEditingActivity::class.java)
        intent.putExtra("purpose", "new")
        startActivityForResult(intent, NEW_ITEM)
    }


    /**
     * return from the [ExaminationEditingActivity] after edit was performed or canceled or deleted
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            //when the request was from the new examination button
            if (requestCode == NEW_ITEM) {

                val dataObject = data?.getJsonExtra("data", RadFileDataClass::class.java)
                if (dataObject != null) {
                    dataObject.date=LocalDate.parse(dataObject.stringDate,formatter)
                }
                adapter.add(dataObject)
                foundHighlight.add(arrayOf(arrayOf(-1,-1), arrayOf(-1,-1), arrayOf(-1,-1)))
                adapter.sort(compareByDescending({ it.date }))
                saveToFile()
                share_Button.visibility = View.GONE
                delete_Button.visibility = View.GONE
                favorite_Button.visibility=View.VISIBLE
                sort_Button.visibility=View.VISIBLE
                onRestart()

            }
            //when the request was from a click on a listView item
            if (requestCode == EDIT_ITEM) {
                val currentPosition = data?.getIntExtra("position", 0)
                val dataObject = data?.getJsonExtra("data", RadFileDataClass::class.java)
                if (dataObject != null) {
                    dataObject.date=LocalDate.parse(dataObject.stringDate,formatter)
                }
                val currentItem = adapter.getItem(currentPosition!!)
                adapter.remove(currentItem)

                adapter.insert(dataObject, currentPosition!!)
                //restating the main activity
                adapter.sort(compareByDescending({ it.date }))
                saveToFile()
                share_Button.visibility = View.GONE
                delete_Button.visibility = View.GONE
                favorite_Button.visibility=View.VISIBLE
                sort_Button.visibility=View.VISIBLE
                onRestart()
            }
        }

        //return from the cancel button of the editing Activity
        if (resultCode == Activity.RESULT_CANCELED && requestCode== EDIT_ITEM||requestCode== NEW_ITEM) {
            share_Button.visibility = View.GONE
            delete_Button.visibility = View.GONE
            favorite_Button.visibility=View.VISIBLE
            sort_Button.visibility=View.VISIBLE
            onRestart()

        }else if(resultCode == Activity.RESULT_CANCELED && requestCode==CREATE_PDF){
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
            foundHighlight.removeAt(currentPosition)

            saveToFile()
            share_Button.visibility = View.GONE
            delete_Button.visibility = View.GONE
            favorite_Button.visibility=View.VISIBLE
            sort_Button.visibility=View.VISIBLE
            onRestart()
        }


    }


    private fun onGoSearch(){
        if(ma_search_text.text==""){
            Toast.makeText(this@MainActivity, "please enter a search text", Toast.LENGTH_LONG).show()

        }else{
            foundHighlight=searchString(ma_search_text.text.toString())

        }
        adapter.notifyDataSetChanged()
    }

    /**
     * method to save the current data to shared preferences (gets called by the checkBox layout)
     */

    fun saveToFile(){
        val sharedPreferences = getSharedPreferences(SHARED_PREFERANCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        for (i in listItems.indices){
            listItems[i].stringDate= listItems[i].date?.format(formatter).toString()
        }
        val json = gson.toJson(listItems)
        editor.putString(KEY_PATH, json)
        editor.apply()
    }

    /**
     * function which searches in the listView for the most recent Items of each type
     *
     * @return an array with the length of the current listView. If the item is the most recent
     * of a type the value is 1 else -1
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun searchMostRecent(): Array<Int?> {
        val res: Resources = resources
        val dropdownArray=res.getStringArray(R.array.type_array)
        var currentRecentItems= arrayOfNulls<Int>(dropdownArray.size)
        currentRecentItems.fill(-1, 0, currentRecentItems.size)
        for (i in listItems.indices){
            val currentData= listItems[i].type
            if (currentRecentItems[currentData!!]==-1){
                currentRecentItems[currentData!!]=i
            }else if(currentRecentItems[currentData!!]!=-1){
                try {
                    if (listItems[currentRecentItems[currentData!!]!!].date?.compareTo(listItems[i].date)!! < 0) {
                        currentRecentItems[currentData!!] = i
                    }
                }
                    catch (e: Exception) {
                        //if anything goes wrong causing exception, get and show exception message
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }



            }

        }
        return currentRecentItems
    }

    /**
     * method to load the Data from file
     *
     * @return MurableList<RadFileDataClass> the examination data in a list
     *
     */
    private fun loadFromFile(): MutableList<RadFileDataClass> {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERANCES, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_PATH, null)


        val objectType = object : TypeToken<MutableList<RadFileDataClass>>() {}.type

        var listItems = gson.fromJson<MutableList<RadFileDataClass>>(json, objectType)

        if (listItems == null) {
            listItems = mutableListOf<RadFileDataClass>()
        }else{
            for (i in listItems.indices){
                listItems[i].date= LocalDate.parse(listItems[i].stringDate,formatter)
            }
        }
        return listItems
    }

    private fun searchString(word: String): MutableList<Array<Array<Int>>>{
        var dropdownStringArray = resources.getStringArray(R.array.type_array)
        //first index s description, second index is type, and third index is date
        val found = MutableList(listItems.size) { arrayOf(arrayOf(-1,-1),arrayOf(-1,-1), arrayOf(-1,-1))}

        for (i in listItems.indices) {
            val description = listItems[i].examination
            val type = listItems[i].type
            val typeString = dropdownStringArray[type!!]
            val date = listItems[i].date?.format(formatter).toString()
            found[i][0][0] = description.indexOf(word, 0, true)
            found[i][0][1] = found[i][0][0]+word.length
            found[i][1][0] = typeString.indexOf(word, 0, true)
            found[i][1][1] = found[i][1][0]+word.length
            found[i][2][0] = date.indexOf(word, 0, true)
            found[i][2][1] = found[i][2][0]+word.length


        }
        return found

    }


    private fun editSearchDialog(target: TextView, flag: Int) {

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        val inputMethodManager: InputMethodManager = this.getSystemService(
            AppCompatActivity.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        val text = bottomSheetDialog.findViewById<TextView>(R.id.bs_edit_editText)
        val next = bottomSheetDialog.findViewById<ImageButton>(R.id.bs_done_button)
        val hint = bottomSheetDialog.findViewById<TextView>(R.id.textHint)
        //showing the hin of the TextView
        hint!!.text = target.hint

        if (text != null) {
            text.text = target.text.toString()
            text.isFocusableInTouchMode = true
            text.requestFocus()
            text.requestFocusFromTouch()

            //force to show the keyboard
            inputMethodManager.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
        bottomSheetDialog.show()

        //closing the sheetDialog and setting th TextView Text to edited Text and starting Search
        if (next != null) {

            next.setOnClickListener {


                target.text = text!!.text.toString()
                onGoSearch()
                //force hide the keyboard
                inputMethodManager.hideSoftInputFromWindow(text.windowToken, 0)


                bottomSheetDialog.dismiss()
            }
        }


    }

}



