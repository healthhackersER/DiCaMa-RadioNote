package com.example.radioapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_new_list_item.*
import java.io.File
import java.io.IOException
import java.util.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Help functions to properly delete a file
 */
private fun Path.exists(): Boolean = Files.exists(this)

//wraps file directory
private fun Path.isFile(): Boolean = !Files.isDirectory(this)

//file delete function
private fun Path.delete(): Boolean {
    return if (isFile() && exists()) {
        //Actual delete operation
        Files.delete(this)
        true
    } else {
        false
    }
}

//Class for the editing Activity of the listView Item Objects
class NewListItem : AppCompatActivity() {
    //creating a unique path name for the photo app and save it in currentPhotoPath
    var currentPhotoPath: String? = null

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_IMAGE_CAPTURE = 2
    }

    //runtime permission check methods
    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    //on create function of the list item class
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)

        //variables and values for different buttons, textfields ect.
        val okButton = findViewById<Button>(R.id.edit_ok_Button)
        val cancelButton = findViewById<Button>(R.id.edit_cancel_Button)
        val deleteButton = findViewById<ImageButton>(R.id.edit_delete_Button)
        val photoButton = findViewById<ImageButton>(R.id.edit_photo_Button)

        val purpose = intent.getStringExtra("purpose")
        val editAblageort = findViewById<TextView>(R.id.edit_Ablageort)
        val editBeurteilung = findViewById<TextView>(R.id.edit_Beurteilung)
        val editNotiz = findViewById<TextView>(R.id.edit_Notiz)
        val editBildbeschreibung = findViewById<TextView>(R.id.edit_Bildbeschreibung)
        var spinner_selection: Int? = null
        val intent = getIntent()

        //random listView position initialization to make it none null
        var position: Int = 505

        //reformating the different text input fields to multiline but still enabeling the done button on touch keyboard
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
        if (editBildbeschreibung != null) {
            editBildbeschreibung.setHorizontallyScrolling(false)
            editBildbeschreibung.setMaxLines(10)
        }

        if (edit_Beschreibung != null) {
            edit_Beschreibung.setHorizontallyScrolling(false)
        }

        //setting up the spinner as a dropdown menue, items of the dropdown menue defined n values dropdown
        val spinner = findViewById<Spinner>(R.id.spinner)
        val spinner_adapter = ArrayAdapter.createFromResource(
            this,
            R.array.type_array,
            android.R.layout.simple_spinner_item
        )
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(spinner_adapter)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinner_selection = null
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinner_selection = position
            }
        }


        //if an existing item was clicked
        if (purpose == "editing") {
            val objectClass = intent.getJsonExtra("data", ObjectClass::class.java)
            position = intent.getIntExtra("position", 0)

            //loading the data from the data object
            edit_Beschreibung.setText(objectClass!!.examination)
            edit_Date.setText(objectClass!!.date)
            edit_Ablageort.setText(objectClass!!.storage)
            edit_Beurteilung.setText(objectClass!!.evaluation)
            edit_Notiz.setText(objectClass!!.note)
            if (objectClass.image != null) {
                currentPhotoPath = objectClass.image
                val currentImage = BitmapFactory.decodeFile(objectClass.image)
                imageView.setImageBitmap(currentImage)
            }

            val current_position = objectClass.type!!
            spinner.setSelection(current_position)
        }

        //TODO: load the image

        //if the activity was started by the new exmamination button
        if (purpose == "new") {
            //do nothing
        }

        //if the editing is finnished and the user clicks on the ok button
        okButton.setOnClickListener {
            val nameExamination = edit_Beschreibung.text.toString()
            val dateExamination = edit_Date.text.toString()
            val storageData = edit_Ablageort.text.toString()
            val evaluationData = edit_Beurteilung.text.toString()
            val noteData = edit_Notiz.text.toString()

            //the values of the different input fields of the editing Activation gets returned to the MainActivity attached to the intent
            val testValue: ObjectClass = ObjectClass(
                nameExamination,
                spinner_selection,
                dateExamination,
                storageData,
                evaluationData,
                noteData,
                currentPhotoPath
            )
            val okIntent = Intent(this, MainActivity::class.java)
            okIntent.putExtraJson("data", testValue)
            okIntent.putExtra("position", position)
            setResult(Activity.RESULT_OK, okIntent)
            finish()
        }

        //when the cancel button was clicked
        cancelButton.setOnClickListener {
            val cancelIntent = Intent(this, MainActivity::class.java)
            setResult(Activity.RESULT_CANCELED, cancelIntent)
            finish()
        }

        //when the delete button was clicked the current item gets deleted
        deleteButton.setOnClickListener {
            val deleteIntent = Intent(this, MainActivity::class.java)
            //when the delete button was clicked when a new item was created do the same as cancel
            if (position == 505) {
                val cancelIntent = Intent(this, MainActivity::class.java)
                setResult(Activity.RESULT_CANCELED, cancelIntent)
                finish()
            }
            //else delete the listView Item
            else {
                deleteIntent.putExtra("position", position)
                val path = Paths.get(currentPhotoPath)
                if (path.delete()) {
                    println("Deketed ${path.fileName}")
                } else {
                    println("Could not delete ${path.fileName}")
                }
                setResult(Activity.RESULT_FIRST_USER, deleteIntent)
                finish()
            }
        }

        //when the photo Button is clicked
        photoButton.setOnClickListener {
            Toast.makeText(this, "Button Clicked", Toast.LENGTH_SHORT).show()
            //dispatchTakePictureIntent()
            //check permissions
            if (hasNoPermissions()) {
                requestPermission()
            }

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
            val fileProvider =
                FileProvider.getUriForFile(this, "com.example.radioapp.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }


    }


    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            //val takenImage = data?.extras?.get("data") as Bitmap
            val takenImage = BitmapFactory.decodeFile(currentPhotoPath)
            imageView.setImageBitmap(takenImage)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        onRestart()
    }


    //creating a unique path name for the photo app and save it in currentPhotoPath
    //var currentPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


}

