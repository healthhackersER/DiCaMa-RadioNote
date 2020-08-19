package com.example.radioapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class EditListItem : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)
        print("Hallo")
    }
}