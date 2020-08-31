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
 * following are help functions which are used throught the project
 */
val permissions = arrayOf(
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    android.Manifest.permission.READ_EXTERNAL_STORAGE
)
//method to check for permissions
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

//requesting the permissions
fun requestPermission(context: Context) {
    ActivityCompat.requestPermissions(context as Activity, permissions, 0)
}

//creates a unique pathname for the image files
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

//methods to safely delete a file
@RequiresApi(Build.VERSION_CODES.O)
fun Path.exists(): Boolean = Files.exists(this)

//wraps file directory
@RequiresApi(Build.VERSION_CODES.O)
fun Path.isFile(): Boolean = !Files.isDirectory(this)

//file delete function
fun Path.delete(): Boolean {
    return if (isFile() && exists()) {
        //Actual delete operation
        Files.delete(this)
        true
    } else {
        false
    }
}