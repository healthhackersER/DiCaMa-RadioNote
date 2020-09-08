package com.example.radioapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.*
import java.nio.file.Files
import java.nio.file.Path

/**
 * permission array
 */
val permissions = arrayOf(
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    android.Manifest.permission.READ_EXTERNAL_STORAGE
)

/**
 * Has no permissions method to check if the permissions are not granted
 *
 * @param context
 * @return
 */
fun hasNoPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) != PackageManager.PERMISSION_GRANTED
}

/**
 * Request permission method
 *
 * @param context
 */
fun requestPermission(context: Context) {
    ActivityCompat.requestPermissions(context as Activity, permissions, 0)
}

/**
 * Creates a image file with a unique Filename
 *
 * @param context
 * @return File the file with the unique path
 *///creates a unique pathname for the image files
@RequiresApi(Build.VERSION_CODES.N)
@Throws(IOException::class)
fun createImageFile(context:Context): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */)
}

/**
 * Exists method to check if the path exists
 *
 * @return
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Path.exists(): Boolean = Files.exists(this)

/**
 * Method to check if the the Path is a directory
 *
 * @return
 *///wraps file directory
@RequiresApi(Build.VERSION_CODES.O)
fun Path.isFile(): Boolean = !Files.isDirectory(this)

/**
 * Method to safely delete a Path
 *
 * @return true in success
 */
fun Path.delete(): Boolean {
    return if (isFile() && exists()) {
        //Actual delete operation
        Files.delete(this)
        true
    } else {
        false
    }
}

/**
 * Method to parse a string array
 *
 * @param stringArrayResourceId
 * @param context
 * @return returns a String map with keyword and associate string
 */
fun parseStringArray(stringArrayResourceId: Int, context: Context): MutableMap<String, String> {
    val stringArray: Array<String> = context.getResources().getStringArray(stringArrayResourceId)
    val outputArray = mutableMapOf<String, String>()
    for (entry in stringArray) {
        val splitResult = entry.split("\\|".toRegex(), 2).toTypedArray()
        outputArray.put(splitResult[0], splitResult[1])
    }
    return outputArray
}