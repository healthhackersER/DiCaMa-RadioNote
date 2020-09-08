package com.example.radioapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_keyword_dialogue.*

/**
 * Keyword_dialogue creates an Activity which shows the additional patient data passed in intent
 * message
 *
 * @constructor Create empty Keyword_dialogue
 */
class keyword_dialogue : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyword_dialogue)
        val message= intent.getStringExtra("message")
        kwd_text.text=message
        kwd_ok_button.setOnClickListener{
            val intent= Intent(this, ExaminationEditingActivity::class.java)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}