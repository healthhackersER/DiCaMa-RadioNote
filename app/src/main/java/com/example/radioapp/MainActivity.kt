package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.View
import android.widget.AbsListView.CHOICE_MODE_SINGLE
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    //List and adapter which hold the Data
    var listItems = mutableListOf<Any>()

    //lateinit var adapter: ArrayAdapter<Any>
    lateinit var adapter: AdapterClass


    //Creating the Main ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val shareButton = findViewById(R.id.share_Button) as ImageButton
        val searchButton = findViewById(R.id.search_Button) as ImageButton
        val configButton = findViewById(R.id.config_Button) as ImageButton
        val deleteButton = findViewById(R.id.delete_Button) as ImageButton
////


        //adapter = ArrayAdapter(this, R.layout.listview_item, listItems)
        adapter = AdapterClass(this, R.layout.listview_item, listItems)


        // attach the array adapter with list view
        val listView: android.widget.ListView = findViewById(R.id.listview_1)
        listView.adapter = adapter
        listView.setChoiceMode(CHOICE_MODE_SINGLE)


        // opening editing class
        listView.setOnItemClickListener { parent, view, position, id ->

            Toast.makeText(this, "Clicked item : $position", Toast.LENGTH_SHORT).show()
            val element = parent.getItemAtPosition(position)
            val intent = Intent(this, NewListItem::class.java)
            intent.putExtra("purpose", "editing")
            //intent.putExtra("position", position)
            startActivityForResult(intent, 2)

        }
        // TODO: Delete Element From list
        listView.setOnItemLongClickListener { parent, view, position, id ->
            Toast.makeText(this, "Position Clicked:" + " " + position, Toast.LENGTH_SHORT).show()
            listView.setItemChecked(position, true)

            return@setOnItemLongClickListener (true)
        }
    }

    //Opening editing Activity for new ExaminationObject to listView
    fun onNewItemButton(view: View) {
        Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, NewListItem::class.java)
        intent.putExtra("purpose", "new")
        startActivityForResult(intent, 1)
    }

    //adding the new Item to the listItems and adapter
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 1) {

                val dataObject = data?.getJsonExtra("data", ObjectClass::class.java)
                adapter.add(dataObject)
                //restating the main activity
                onRestart()

            }
        }
    }


    // "Go to Second Activity" button click

//    val list = mutableListOf<ObjectClass?>(ObjectClass("CT","04.02.99"))
//    val currentPath = System.getProperty("user.dir")
//
//
//    //when second activity is finished
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode== Activity.RESULT_OK) {
//
//            if (requestCode == 1){
//
//                val dataObject=data?.getJsonExtra("data",ObjectClass::class.java)
//                list.add(dataObject)
//
//                print("message received")
//
//            }
//        }
//    }
//
//    fun list_to_arrayList(list: MutableList<ObjectClass?>): List<String?> {
//        var array= mutableListOf<String?>()
//        for (i in list.indices){
//            array[i]=list[i]?.examination
//        }
//        return array
//
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
////      var array_list=list_to_arrayList(list)
//
//        var array_test =mutableListOf("CT","MRT")
//        // initialize an array adapter
//        val adapter:ArrayAdapter<String> = ArrayAdapter(
//            this,
//            android.R.layout.simple_dropdown_item_1line,array_test
//        )
//
//        // attach the array adapter with list view
//        val listView: ListView = findViewById(R.id.listview_1)
//        listView.adapter = adapter
//
//
//
//
//
//
//
//
//
//        //Starting second activity on button click
//        val newButton = findViewById(R.id.button) as Button
//        newButton.setOnClickListener {
//            Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
//            val i = Intent(this, NewListItem::class.java)
//            startActivityForResult(i,1)
//            adapter.notifyDataSetChanged()
//        }
//
//
//
//
//
//
//
//
//
//
//
//
//    }

}