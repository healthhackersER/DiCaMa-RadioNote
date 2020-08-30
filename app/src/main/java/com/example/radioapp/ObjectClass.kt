package com.example.radioapp

import java.time.LocalDate
import java.util.*

//custom data class Object to handle the data of the Items of the ListView of the MainActivity
data class ObjectClass (
    val examination: String,
    val type: Int?,
    val date: LocalDate?,
    val storage: String?,
    val evaluation: String?,
    val note: String?,
    val image: MutableList<String>?,
    val imageDescription: MutableList<String>?,
    var favorites: Boolean
)