package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_list_item.*
import android.widget.*
import java.io.Serializable


class NewListItem : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)
        val newButton = findViewById(R.id.button2) as Button
        
        newButton.setOnClickListener{
            var nameExamination = editTextName.text.toString()
            var dateExamination = editTextDate.text.toString()

            Toast.makeText(this,nameExamination, Toast.LENGTH_SHORT).show()
            Toast.makeText(this,dateExamination, Toast.LENGTH_SHORT).show()
            var testValue: ObjectClass = ObjectClass(nameExamination,dateExamination)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtraJson("data",testValue)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }
}

class EditListItem: AppCompatActivity(){

}