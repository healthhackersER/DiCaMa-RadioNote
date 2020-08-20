package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView.CHOICE_MODE_SINGLE
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
            intent.putExtra("position", position)
            intent.putExtraJson("data", element)

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
            if (requestCode ==2) {
                val currentPosition=data?.getIntExtra("position",0)
                val dataObject = data?.getJsonExtra("data", ObjectClass::class.java)
                val currentItem=adapter.getItem(currentPosition!!)
                adapter.remove(currentItem)
                adapter.insert(dataObject,currentPosition!!)
                //restating the main activity
                onRestart()
            }
        }
        if (resultCode == Activity.RESULT_CANCELED){
            onRestart()
        }
        if (resultCode == Activity.RESULT_FIRST_USER) {
            val currentPosition=data?.getIntExtra("position",0)
            val currentItem=adapter.getItem(currentPosition!!)
            adapter.remove(currentItem)
            onRestart()
            }
    }
}



