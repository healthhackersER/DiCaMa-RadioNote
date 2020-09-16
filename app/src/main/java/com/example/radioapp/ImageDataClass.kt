package com.example.radioapp

/**
 * Image data class holds the image data passed from [CameraEditingActivity] to
 * [ExaminationEditingActivity]
 *
 * @property imageFiles a list of filepath as String
 * @property imageDescription a list of Strings
 * @property marker X,Y coordinates as float
 * @property imageMarked files of marked images
 * @constructor Create empty Image data class
 */
data class ImageDataClass(
    val imageFiles: MutableList<String>,
    val imageDescription: MutableList<String>, val marker: MutableList<MutableList<FloatArray>>,
    val imageMarked: MutableList<String>
)