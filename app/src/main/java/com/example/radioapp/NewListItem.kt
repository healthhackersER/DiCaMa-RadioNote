package com.example.radioapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.SparseArray
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.set
import androidx.core.text.toSpannable
import kotlinx.android.synthetic.main.activity_new_list_item.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


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

/**
 * Help function to parse a string array
 */
fun parseStringArray(stringArrayResourceId: Int, context: Context): MutableMap <String,String>{
    val stringArray: Array<String> = context.getResources().getStringArray(stringArrayResourceId)
    val outputArray = mutableMapOf<String,String>()
    for (entry in stringArray) {
        val splitResult = entry.split("\\|".toRegex(), 2).toTypedArray()
        outputArray.put(splitResult[0], splitResult[1])
    }
    return outputArray
}

/**
 * Activity Class to edit the different list view items
 */
class NewListItem : AppCompatActivity() {
    //variable for the pathname of the photo taken by the camera activity, needs to be declared here in order for all class function to be able to access it
    var currentPhotoPath: String? = null


    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_IMAGE_CAPTURE = 2
    }

    /**
     * following code implements a variable and method to check for the required permissions on runtime
     */
    val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    //method to check for permissions
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

    //requesting the permissions
    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    /**
     * creating the Activity to edit the listView item
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)
        val myStringMap = parseStringArray(R.array.key_string_array, this)
        var clickList = mutableListOf<Any>()


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

        //setting up the spinner as a dropdown menu, items of the dropdown menu defined n values dropdown
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


        /**
         * if an existing item was clicked
         */
        if (purpose == "editing") {
            val objectClass = intent.getJsonExtra("data", ObjectClass::class.java)
            position = intent.getIntExtra("position", 0)

            //loading the data from the data object
            edit_Beschreibung.setText(objectClass!!.examination)
            edit_Date.setText(objectClass!!.date)
            edit_Ablageort.setText(objectClass!!.storage)
            edit_Beurteilung.setText(objectClass!!.evaluation)
            edit_Notiz.setText(objectClass!!.note)

            //loading the image from file
            if (objectClass.image != null) {
                currentPhotoPath = objectClass.image
                val currentImage = BitmapFactory.decodeFile(objectClass.image)
                imageView.setImageBitmap(currentImage)
            }

            val current_position = objectClass.type!!
            spinner.setSelection(current_position)

            checkKeyWords(edit_Beurteilung, myStringMap, clickList)
        }


        //if the activity was started by the new exmamination button
        if (purpose == "new") {
            //do nothing
        }

        //to deselect all items at start


        /**
         * if the user is finnished and clicks on the ok button the edited/entered values get returned to the main activity
         */
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
                //telling the main activity to delete the listView item
                deleteIntent.putExtra("position", position)
                //delete the image file
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

            //check permissions
            if (hasNoPermissions()) {
                requestPermission()
            }

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            //creating the Uri necessary for SDK 29 up
            val fileProvider =
                FileProvider.getUriForFile(this, "com.example.radioapp.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            //starting the camera activity with check if it is possible
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }


        }



        //method to check if done button is clicked in TextView
        // use: myEditText.onClickKeyboardDoneButton{myFunctionToExecuteWhenUserClickDone()}
        fun TextView.onClickKeyboardDoneButton(funExecute: () -> Unit) {
            this.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        funExecute.invoke()
                        true
                    }
                    else -> false
                }
            }
        }
    }

//    //TODO: function to check a TextViews for keys in mykeymap, and then setting them to clickable
    //method to check the TextView Items for key words.
    fun checkKeyWords(target:TextView, thisStringMap: MutableMap<String,String>,array: MutableList<Any>){
        val sentenceString=target.text.toString()
        val spanString = SpannableString(sentenceString)
        val sentenceWords= sentenceString.replace('\n', ' ').split(" ")


        for (word in sentenceWords){
            if (thisStringMap.containsKey(word)){
                val startIndex=sentenceString.indexOf(word,0)
                val stopIndex=startIndex+word.length

                val clickableSpan= object : ClickableSpan() {
                    @Override
                    override fun onClick(p0: View) {
                        //Do nothing
                        val message=thisStringMap.getValue(word)
                        Toast.makeText(this@NewListItem, message, Toast.LENGTH_SHORT).show()
                    }
                }

                spanString.setSpan(clickableSpan,startIndex,stopIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


            }

        }
        target.text = spanString
        target.movementMethod = LinkMovementMethod.getInstance()

    }

    /**
     * unused function to create jpg file from filename
     */
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    /**
     * method to display and set up tp the pathname for the image taken by the camera
     */

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


    /**
     * creating a unique path name for the photo app and save it in currentPhotoPath
     */

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

