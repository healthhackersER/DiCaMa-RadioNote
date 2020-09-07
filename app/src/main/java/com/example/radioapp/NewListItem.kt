package com.example.radioapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_new_list_item.*
import java.io.File
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Help function to parse a string array
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

/**
 * Activity Class to edit the different list view items
 */
class NewListItem : AppCompatActivity() {
    //variable for the pathname of the photo taken by the camera activity, needs to be declared here in order for all class function to be able to access it
    var currentPhotoPath: MutableList<String> = mutableListOf<String>()
    var currentPhotoDescription: MutableList<String> = mutableListOf()
    var currentMarker: MutableList<FloatArray> = mutableListOf()
    private lateinit var myStringMap: MutableMap<String,String>

    var clickList = mutableListOf<Any>()

    @RequiresApi(Build.VERSION_CODES.O)
    var formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd/MM/yyyy")


    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        const val EDIT_DATE = 3
        const val EDIT_TEXT = 4
        const val REQUEST_IMAGE_EDITOR = 5
        const val REQUEST_KEYWORD_DIALOGUE = 6

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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)

        myStringMap = parseStringArray(R.array.key_string_array, this)

        //variables and values for different buttons, textfields ect.
        val okButton = findViewById<Button>(R.id.edit_ok_Button)
        val cancelButton = findViewById<Button>(R.id.edit_cancel_Button)
        val deleteButton = findViewById<ImageButton>(R.id.edit_delete_Button)
        val photoButton = findViewById<ImageButton>(R.id.edit_photo_Button)

        val purpose = intent.getStringExtra("purpose")
        val editAblageort = findViewById<TextView>(R.id.edit_Ablageort)
        val editBeurteilung = findViewById<TextView>(R.id.edit_Beurteilung)
        val editNotiz = findViewById<TextView>(R.id.edit_Notiz)
//        val editBildbeschreibung = findViewById<TextView>(R.id.edit_Bildbeschreibung)
        val editDate = findViewById<TextView>(R.id.edit_Date)
        val editBeschreibung = findViewById<TextView>(R.id.edit_Beschreibung)
        var spinner_selection: Int? = null
        var favorites = false
        val intent = getIntent()

        //random listView position initialization to make it none null
        var position: Int = 505

        editBeschreibung.setOnClickListener {
            editDialog(editBeschreibung, EDIT_TEXT)
        }
        editDate.setOnClickListener {
            editDialog(editDate, EDIT_DATE)
        }
        editAblageort.setOnClickListener {
            editDialog(editAblageort, EDIT_TEXT)
        }
        editBeurteilung.setOnClickListener {
            editDialog(editBeurteilung, EDIT_TEXT)

        }
        editNotiz.setOnClickListener {
            editDialog(editNotiz, EDIT_TEXT)
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
            val objectClass = intent.getJsonExtra("data", RadFileDataClass::class.java)
            position = intent.getIntExtra("position", 0)

            //loading the data from the data object
            edit_Beschreibung.setText(objectClass!!.examination)

            val dateFormatted = objectClass!!.date?.format(formatter)
            edit_Date.setText(dateFormatted.toString())
            favorites = objectClass.favorites
            edit_Ablageort.setText(objectClass!!.storage)
            edit_Beurteilung.setText(objectClass!!.evaluation)
            edit_Notiz.setText(objectClass!!.note)
            currentPhotoPath = objectClass.image.imageFiles!!
            currentPhotoDescription = objectClass.image.imageDescription!!
            currentMarker=objectClass.image.marker!!



            if (objectClass.image.imageFiles.size >= 1) {
                val currentImage = BitmapFactory.decodeFile(objectClass.image.imageFiles[0])
                imageView.setImageBitmap(currentImage)
            }else if(objectClass.image.imageFiles.size ==0 ){
                val defaultImage = BitmapFactory.decodeResource(this.resources, R.drawable.xray_flower)
                imageView.setImageBitmap(defaultImage)
            }


            val currentPosition = objectClass.type!!
            spinner.setSelection(currentPosition)

            checkKeyWords(editBeurteilung, myStringMap, clickList)
            checkKeyWords(editNotiz,myStringMap,clickList)
        }


        //if the activity was started by the new exmamination button
        if (purpose == "new") {
            val defaultImage = BitmapFactory.decodeResource(this.resources, R.drawable.xray_flower)
            imageView.setImageBitmap(defaultImage)
        }


        /**
         * if the user is finnished and clicks on the ok button the edited/entered values get returned to the main activity
         */
        okButton.setOnClickListener {
            val nameExamination = edit_Beschreibung.text.toString()
            val dateExamination = edit_Date.text.toString()
            val storageData = edit_Ablageort.text.toString()
            val evaluationData = edit_Beurteilung.text.toString()
            val noteData = edit_Notiz.text.toString()
            val favoritesOut = favorites

            //check description
            if (nameExamination != "") {
                //do nothing and continue
            } else {
                Toast.makeText(
                    this@NewListItem,
                    "Bitte geben sie eine Beschreibung ein",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener

            }
            //check if datefield is entered correctly
            if (dateExamination.matches("^\\d{2}[/]\\d{2}[/]\\d{4}$".toRegex())) {
                //do nothing and continue
            } else {
                Toast.makeText(
                    this@NewListItem,
                    "Bitte geben sie ein valides Datum im Format dd/mm/yyyy ein",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            //check if description is entered

            //formating date to Date object
            var date = LocalDate.parse(dateExamination, formatter)

            //the values of the different input fields of the editing Activation gets returned to the MainActivity attached to the intent
            val testValue: RadFileDataClass = RadFileDataClass(
                nameExamination,
                spinner_selection,
                date,
                storageData,
                evaluationData,
                noteData,
                ImageDataClass(currentPhotoPath,currentPhotoDescription,currentMarker),
                favoritesOut,
                highlight = false
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
                //TODO: delete the image file
//                val path = Paths.get(currentPhotoPath)
//                if (path.delete()) {
//                    println("Deleted ${path.fileName}")
//                } else {
//                    println("Could not delete ${path.fileName}")
//                }

                setResult(Activity.RESULT_FIRST_USER, deleteIntent)
                finish()
            }
        }

        imageView.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            val imageData= ImageDataClass(currentPhotoPath,currentPhotoDescription,currentMarker)
            intent.putExtraJson("imageData", imageData)

            startActivityForResult(intent, REQUEST_IMAGE_EDITOR)
        }

        photoButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            val imageData= ImageDataClass(currentPhotoPath,currentPhotoDescription,currentMarker)
            intent.putExtraJson("imageData", imageData)

//            currentPhotoDescription.let { it1 -> intent.putExtraJson("dataDescription", it1) }
//            currentPhotoPath.let { it1 -> intent.putExtraJson("dataImages", it1) }
            startActivityForResult(intent, REQUEST_IMAGE_EDITOR)
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

//        val bottomSheet = findViewById<TextView>(R.id.text_image)
//        bottomSheet.setOnClickListener {
//            editDialog(bottomSheet, EDIT_TEXT)
//        }
    }

    /**
     * Method to call a sheet Dialog to enter/ edit the text of a clickable text field
     */
    private fun editDialog(target: TextView, flag: Int) {

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        val inputMethodManager: InputMethodManager = this.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager

        val text = bottomSheetDialog.findViewById<TextView>(R.id.editTextTextPersonName)
        val next = bottomSheetDialog.findViewById<ImageButton>(R.id.imageButton)
        val hint = bottomSheetDialog.findViewById<TextView>(R.id.textHint)
        //showing the hin of the TextView
        hint!!.text = target.hint

        if (text != null) {
            text.text = target.text.toString()
            if (flag == EDIT_DATE) {
                text.inputType = InputType.TYPE_CLASS_DATETIME
            }

            text.isFocusableInTouchMode = true
            text.requestFocus()
            text.requestFocusFromTouch()

            //force to show the keyboard
            inputMethodManager.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
        bottomSheetDialog.show()

        //closing the sheetDialog and setting th TextView Text to edited Text
        if (next != null) {

            next.setOnClickListener {
                if (flag == EDIT_DATE) {
                    val textToCheck = text!!.text.toString()
                    if (textToCheck.matches("^\\d{2}[/]\\d{2}[/]\\d{4}$".toRegex())) {
                        target.text = text!!.text.toString()
                        //force hide the keyboard
                        inputMethodManager.hideSoftInputFromWindow(text.windowToken, 0)
                        bottomSheetDialog.dismiss()
                    } else {
                        Toast.makeText(
                            this@NewListItem,
                            "Bitte geben sie ein valides Datum im Format dd/mm/yyyy ein",
                            Toast.LENGTH_LONG
                        ).show()
                        bottomSheetDialog.show()
                    }

                } else if (flag == EDIT_TEXT) {
                    target.text = text!!.text.toString()

                    //force hide the keyboard
                    inputMethodManager.hideSoftInputFromWindow(text.windowToken, 0)
                    checkKeyWords(target, myStringMap, clickList)
                    bottomSheetDialog.dismiss()
                }
            }


        }


    }


    //method to check the TextView Items for key words.
    fun checkKeyWords(
        target: TextView,
        thisStringMap: MutableMap<String, String>,
        array: MutableList<Any>
    ) {
        val sentenceString = target.text.toString()
        val spanString = SpannableString(sentenceString)
        var sentenceWords = sentenceString.replace('\n', ' ').split(" ").toMutableList()
        for (i in sentenceWords.indices){
            sentenceWords[i]=sentenceWords[i].toLowerCase()
        }

        for (word in sentenceWords) {
            if (thisStringMap.containsKey(word)) {
                val startIndex = sentenceString.indexOf(word, 0, true)
                val stopIndex = startIndex + word.length

                val clickableSpan = object : ClickableSpan() {
                    @Override
                    override fun onClick(p0: View) {
                        //Do nothing
                        val message = thisStringMap.getValue(word)
                        val intent = Intent(this@NewListItem, keyword_dialogue::class.java)
                        intent.putExtra("message",message)
                        startActivityForResult(intent,REQUEST_KEYWORD_DIALOGUE)


                        //Toast.makeText(this@NewListItem, message, Toast.LENGTH_SHORT).show()
                    }
                }

                spanString.setSpan(
                    clickableSpan,
                    startIndex,
                    stopIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )


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
        if (requestCode == REQUEST_IMAGE_EDITOR && resultCode == Activity.RESULT_OK) {
            val currentData=  data!!.getJsonExtra("imageData", ImageDataClass::class.java)
            currentPhotoPath = currentData!!.imageFiles
            currentPhotoDescription = currentData.imageDescription
            currentMarker = currentData.marker
            if (currentPhotoPath.size >= 1) {
                try{
                    val takenImage = BitmapFactory.decodeFile(currentPhotoPath[0])
                    imageView.setImageBitmap(takenImage)
                }catch (e: Exception){
                    println("Exception in onActivityResult")
                }



            }

        } else if (requestCode== REQUEST_KEYWORD_DIALOGUE){
            onResume()
        }

        else{
            super.onActivityResult(requestCode, resultCode, data)
        }
        onRestart()
    }





}

