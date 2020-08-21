package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_list_item.*
import android.widget.*

//Class for the editing Activity of the listView Item Objects
class NewListItem : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)

        //variables and values for different buttons, textfields ect.
        val okButton = findViewById<Button>(R.id.edit_ok_Button)
        val cancelButton = findViewById<Button>(R.id.edit_cancel_Button)
        val deleteButton = findViewById<ImageButton>(R.id.edit_delete_Button)
        val intent = getIntent()
        val purpose = intent.getStringExtra("purpose")
        val editAblageort = findViewById(R.id.edit_Ablageort) as TextView
        val editBeurteilung = findViewById(R.id.edit_Beurteilung) as TextView
        val editNotiz = findViewById(R.id.edit_Notiz) as TextView
        var spinner_selection: Int? = null

        //random listView position initialization to make it none null
        var position: Int = 505

        //reformating the different text input fields to multiline but still enabeling the done button on touch keyboard
        if (editAblageort != null) {
            editAblageort.setHorizontallyScrolling(false)
            editAblageort.setMaxLines(20)
        }
        if (editBeurteilung != null) {
            editBeurteilung.setHorizontallyScrolling(false)
            editBeurteilung.setMaxLines(20)
        }
        if (editNotiz != null) {
            editNotiz.setHorizontallyScrolling(false)
            editNotiz.setMaxLines(20)
        }

        if (edit_Beschreibung != null) {
            edit_Beschreibung.setHorizontallyScrolling(false)
        }

        //setting up the spinner as a dropdown menue, items of the dropdown menue defined n values dropdown
        val spinner = findViewById<Spinner>(R.id.spinner)
        val spinner_adapter = ArrayAdapter.createFromResource(
            this,
            R.array.type_array,
            android.R.layout.simple_spinner_item
        )
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(spinner_adapter)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinner_selection = null
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinner_selection = position
            }
        }


        //if an existing item was clicked
        if (purpose == "editing") {
            val objectClass = intent.getJsonExtra("data", ObjectClass::class.java)
            position = intent.getIntExtra("position", 0)

            //loading the data from the data object
            edit_Beschreibung.setText(objectClass!!.examination)
            edit_Date.setText(objectClass!!.date)
            edit_Ablageort.setText(objectClass!!.storage)
            edit_Beurteilung.setText(objectClass!!.evaluation)
            edit_Notiz.setText(objectClass!!.note)
            val current_position = objectClass.type!!
            spinner.setSelection(current_position)
        }

        //TODO: load the image

        //if the activity was started by the new exmamination button
        if (purpose == "new") {
            //do nothing
        }

        //if the editing is finnished and the user clicks on the ok button
        okButton.setOnClickListener {
            val nameExamination = edit_Beschreibung.text.toString()
            val dateExamination = edit_Date.text.toString()
            val storageData = edit_Ablageort.text.toString()
            val evaluationData = edit_Beurteilung.text.toString()
            val noteData = edit_Notiz.text.toString()

            //the values of the different input fields of the editing Activation gets returned to the MainActivity attached to the intent
            val testValue: ObjectClass = ObjectClass(
                nameExamination,
                spinner_selection,
                dateExamination,
                storageData,
                evaluationData,
                noteData,
                null
            )
            val okIntent = Intent(this, MainActivity::class.java)
            okIntent.putExtraJson("data", testValue)
            okIntent.putExtra("position", position)
            setResult(Activity.RESULT_OK, okIntent)
            finish()
        }

        //when the cancel button was clicked
        cancelButton.setOnClickListener {
            val cancelIntent = Intent(this, MainActivity::class.java)
            setResult(Activity.RESULT_CANCELED, cancelIntent)
            finish()
        }

        //when the delete button was clicked the current item gets deleted
        deleteButton.setOnClickListener {
            val deleteIntent = Intent(this, MainActivity::class.java)
            //when the delete button was clicked when a new item was created do the same as cancel
            if (position == 505) {
                val cancelIntent = Intent(this, MainActivity::class.java)
                setResult(Activity.RESULT_CANCELED, cancelIntent)
                finish()
            }
            //else delete the listView Item
            else {
                deleteIntent.putExtra("position", position)
                setResult(Activity.RESULT_FIRST_USER, deleteIntent)
                finish()
            }
        }
    }
}

