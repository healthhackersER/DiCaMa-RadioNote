package com.example.radioapp

import java.time.LocalDate
import java.util.*

/**
 * Rad file data class which stores the data from each examination item from [MainActivity]
 *
 * @property examination the examination description text
 * @property type the examination type text
 * @property date the examination date tey
 * @property storage the examination stored location text
 * @property evaluation the examination evaluation text
 * @property note the examination note text
 * @property image the image data as [ImageDataClass]
 * @property favorites if the favorite checkbox is checked
 * @property highlight if the item is highlighted
 * @constructor Create empty Rad file data class
 */
data class RadFileDataClass (
    val examination: String,
    val type: Int?,
    var date: LocalDate?,
    val storage: String?,
    val evaluation: String?,
    val note: String?,
    val image: ImageDataClass,
    var favorites: Boolean,
    var highlight: Boolean,
    var stringDate: String?,
)