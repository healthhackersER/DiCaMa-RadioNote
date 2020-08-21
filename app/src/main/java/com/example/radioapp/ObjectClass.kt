package com.example.radioapp

//custom data class Object to handle the data of the Items of the ListView of the MainActivity
data class ObjectClass(
    val examination: String,
    val type: Int?,
    val date: String?,
    val storage: String?,
    val evaluation: String?,
    val note: String?,
    val image: String?
)