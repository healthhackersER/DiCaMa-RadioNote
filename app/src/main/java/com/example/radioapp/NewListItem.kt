package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_list_item.*
import android.widget.*


class NewListItem : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)

        //variables and values for different buttons, textfields usw.
        val newButton = findViewById(R.id.edit_ok_Button) as Button
        val intent = getIntent()
        var purpose = intent.getStringExtra("purpose")
        val editAblageort = findViewById(R.id.edit_Ablageort) as TextView
        val editBeurteilung = findViewById(R.id.edit_Beurteilung) as TextView
        val editNotiz = findViewById(R.id.edit_Notiz) as TextView

        //reformating the different text input fields
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
        if ( edit_Beschreibung !=null){
            edit_Beschreibung.setHorizontallyScrolling(false)
        }

        if (purpose == "edit") {

        }

        if (purpose == "new") {

        }


        newButton.setOnClickListener {
            var nameExamination = edit_Beschreibung.text.toString()
            var dateExamination = edit_Date.text.toString()

            Toast.makeText(this, nameExamination, Toast.LENGTH_SHORT).show()
            Toast.makeText(this, dateExamination, Toast.LENGTH_SHORT).show()
            var testValue: ObjectClass = ObjectClass(nameExamination,null,dateExamination, null,null,null,null)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtraJson("data", testValue)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }
}

