package com.example.radioapp

import android.Manifest
import android.app.Activity
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_new_list_item.*
import java.io.File
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter


/**
 * Examination editing activity displays the Activity in order to edit the Text for each examination
 * from [MainActivity]. Can call [CameraEditingActivity] for the images and returns edited
 * Items as [RadFileDataClass] back to the [MainActivity].
 *
 * @constructor Create empty Examination editing activity
 */
class ExaminationEditingActivity : AppCompatActivity() {
    //variable for the pathname of the photo taken by the camera activity, needs to be declared here in order for all class function to be able to access it
    var currentPhotoPath: MutableList<String> = mutableListOf<String>()
    var currentPhotoDescription: MutableList<String> = mutableListOf()
    var currentMarker: MutableList< MutableList<FloatArray>> = mutableListOf()
    var currentImageMarked: MutableList<String> = mutableListOf()
    private lateinit var myStringMap: MutableMap<String, String>

    var clickList = mutableListOf<Any>()

    @RequiresApi(Build.VERSION_CODES.O)
    //setting the format for the date field
    var formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    //definition of used Constant values
    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        const val EDIT_DATE = 3
        const val EDIT_TEXT = 4
        const val REQUEST_IMAGE_EDITOR = 5
        const val REQUEST_KEYWORD_DIALOGUE = 6

    }

    //checking for permissions at runtime
    private val permissions = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    /**
     * method to check if the permissions are not granted
     *
     * @return Boolean
     *
     */
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

    /**
     * Request permission method
     *
     */
    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    /**
     * creating the instance of the [ExaminationEditingActivity]
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_list_item)

        //the string map with the keywords and the additional medical information
        myStringMap = parseStringArray(R.array.key_string_array, this)

        //variables and values for different buttons, textfields ect.
        val okButton = findViewById<Button>(R.id.ee_ok_button)
        val cancelButton = findViewById<Button>(R.id.ee_cancel_button)
        val deleteButton = findViewById<ImageButton>(R.id.ee_delete_button)



        val purpose = intent.getStringExtra("purpose")
        val editAblageort = findViewById<TextView>(R.id.ee_storage_editText)
        val editBeurteilung = findViewById<TextView>(R.id.ee_evaluation_textEdit)
        val editNotiz = findViewById<TextView>(R.id.ee_note_textEdit)
        val editDate = findViewById<TextView>(R.id.edit_Date)
        val editBeschreibung = findViewById<TextView>(R.id.edit_Beschreibung)
        var spinner_selection: Int? = null
        var favorites = false
        val intent = intent

        //random listView position initialization to make it none null
        var position: Int = 505

        //connecting the different TextViews to the Editing function
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
            /**
             * function for no item selected in the spinner
             */
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinner_selection = null
            }

            /**
             * function for item selected in the spinner
             */
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinner_selection = position
            }
        }


        //if an existing item was clicked on in the MainActivity
        if (purpose == "editing") {
            val objectClass = intent.getJsonExtra("data", RadFileDataClass::class.java)
            position = intent.getIntExtra("position", 0)

            //loading the data from the data object
            edit_Beschreibung.setText(objectClass!!.examination)

            val dateFormatted = objectClass!!.date?.format(formatter)
            edit_Date.setText(dateFormatted.toString())
            favorites = objectClass.favorites
            ee_storage_editText.text = objectClass!!.storage
            ee_evaluation_textEdit.text = objectClass!!.evaluation
            ee_note_textEdit.text = objectClass!!.note
            currentPhotoPath = objectClass.image.imageFiles!!
            currentPhotoDescription = objectClass.image.imageDescription!!
            currentMarker = objectClass.image.marker!!
            currentImageMarked=objectClass.image.imageMarked!!


            //showing the image if the examination has an image
            if (objectClass.image.imageFiles.size >= 1) {
                val currentImage = BitmapFactory.decodeFile(objectClass.image.imageFiles[0])
                ee_imageView.setImageBitmap(currentImage)
                ee_image_text.text=currentPhotoDescription[0]
            } else if (objectClass.image.imageFiles.size == 0) {
                val defaultImage =
                    BitmapFactory.decodeResource(this.resources, R.drawable.camera_image)
                ee_imageView.setImageBitmap(defaultImage)
            }

            //setting up the spinner
            val currentPosition = objectClass.type!!
            spinner.setSelection(currentPosition)

            //connecting the TextViews to the keyWord function
            checkKeyWords(editBeurteilung, myStringMap, clickList)
            checkKeyWords(editNotiz, myStringMap, clickList)
        }


        //if the activity was started by the new exmamination button set up default image
        if (purpose == "new") {
            val defaultImage = BitmapFactory.decodeResource(this.resources, R.drawable.camera_image)
            ee_imageView.setImageBitmap(defaultImage)
        }


        /**
         * if the user is finnished and clicks on the ok button the edited/entered values get returned to the main activity
         */
        okButton.setOnClickListener {
            val nameExamination = edit_Beschreibung.text.toString()
            val dateExamination = edit_Date.text.toString()
            val storageData = ee_storage_editText.text.toString()
            val evaluationData = ee_evaluation_textEdit.text.toString()
            val noteData = ee_note_textEdit.text.toString()
            val favoritesOut = favorites

            //check if description field is entered
            if (nameExamination != "") {

            } else {
                Toast.makeText(
                    this@ExaminationEditingActivity,
                    "Bitte geben sie eine Beschreibung ein",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener

            }
            //check if date field is entered correctly
            if (dateExamination.matches("^\\d{2}[/]\\d{2}[/]\\d{4}$".toRegex())) {

            } else {
                Toast.makeText(
                    this@ExaminationEditingActivity,
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
                ImageDataClass(currentPhotoPath, currentPhotoDescription, currentMarker,currentImageMarked),
                favoritesOut,
                highlight = false
            )

            val okIntent = Intent(this, MainActivity::class.java)
            okIntent.putExtraJson("data", testValue)
            okIntent.putExtra("position", position)
            setResult(Activity.RESULT_OK, okIntent)
            finish()
        }

        /**
         * when the type info button is clicked
         */
        ee_info_button.setOnClickListener {
            val info=resources.getStringArray(R.array.type_info_array)
            try{
                val message = info[spinner_selection!!]
                val intent =
                    Intent(this@ExaminationEditingActivity, KeywordDialogue::class.java)
                intent.putExtra("message", message)
                startActivityForResult(intent, REQUEST_KEYWORD_DIALOGUE)
            }catch (e: Exception) {
                //if anything goes wrong causing exception, get and show exception message
                Toast.makeText(this@ExaminationEditingActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

        /**
         * when the cancel button is clicked
         */
        cancelButton.setOnClickListener {
            val cancelIntent = Intent(this, MainActivity::class.java)
            setResult(Activity.RESULT_CANCELED, cancelIntent)
            finish()
        }

        /**
         * when the delete button gets clicked the MainActivity gets notified to delete this item
         */
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
                setResult(Activity.RESULT_FIRST_USER, deleteIntent)
                finish()
            }
        }

        /**
         * when the cameraButton is clicked on the CameraEditingActivity gets started
         */
        ee_camera_button.setOnClickListener {
            val intent = Intent(this, CameraEditingActivity::class.java)
            val imageData = ImageDataClass(currentPhotoPath, currentPhotoDescription, currentMarker,currentImageMarked)
            intent.putExtraJson("imageData", imageData)

            startActivityForResult(intent, REQUEST_IMAGE_EDITOR) }

        /**
         * when the imageViewer is clicked on the CameraEditingActivity gets started
         */
        ee_imageView.setOnClickListener {
            val intent = Intent(this, CameraEditingActivity::class.java)
            val imageData = ImageDataClass(currentPhotoPath, currentPhotoDescription, currentMarker,currentImageMarked)
            intent.putExtraJson("imageData", imageData)

            startActivityForResult(intent, REQUEST_IMAGE_EDITOR)
        }


        /**
         * when the done button on the virtual Keyboard gets clicked on
         */
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

    /**
     * Method to call a sheet Dialog to enter/ edit the text of a clickable text field
     * @param target the targeted TextView
     * @param flag if a date field is supposed to be entered = EDIT_DATE else EDIT_TEXT
     */
    private fun editDialog(target: TextView, flag: Int) {

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        val inputMethodManager: InputMethodManager = this.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager

        val text = bottomSheetDialog.findViewById<TextView>(R.id.bs_edit_editText)
        val next = bottomSheetDialog.findViewById<ImageButton>(R.id.bs_done_button)
        val hint = bottomSheetDialog.findViewById<TextView>(R.id.textHint)
        //showing the hint of the TextView
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
                //checking if the date is in right format
                if (flag == EDIT_DATE) {
                    val textToCheck = text!!.text.toString()
                    if (textToCheck.matches("^\\d{2}[/]\\d{2}[/]\\d{4}$".toRegex())) {
                        target.text = text!!.text.toString()
                        //force hide the keyboard
                        inputMethodManager.hideSoftInputFromWindow(text.windowToken, 0)
                        bottomSheetDialog.dismiss()
                    } else {
                        Toast.makeText(
                            this@ExaminationEditingActivity,
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


    /**
     * Check the target view for keywords defined in thisStringMap and sets up a clickable
     * marked span the textview for the keyword. The clickable Keyword then gets connected to
     * an [KeywordDialogue] activity which displayed the text defined in thisStringMap
     *
     * @param target the targeted textView
     * @param thisStringMap the StringMap
     * @param array not used variable intended to save the clickable keywords
     */
    fun checkKeyWords(
        target: TextView,
        thisStringMap: MutableMap<String, String>,
        array: MutableList<Any>
    ) {
        val sentenceString = target.text.toString()
        val spanString = SpannableString(sentenceString)
        //getting each word from the textView
        var sentenceWords = sentenceString.replace('\n', ' ').split(" ",",",".").toMutableList()
        //converting to lower case in order to avoid case sensitivity
        for (i in sentenceWords.indices) {
            sentenceWords[i] = sentenceWords[i].toLowerCase()
        }
        //setting up the keywords with the clickable links
        for (word in sentenceWords) {
            if (thisStringMap.containsKey(word)) {
                val startIndex = sentenceString.indexOf(word, 0, true)
                val stopIndex = startIndex + word.length

                val clickableSpan = object : ClickableSpan() {
                    @Override
                    /**
                     * Method to call a  [KeywordDialogue] when the keyword gets clicked on
                     * @param p0 the clickable keyword
                     */
                    override fun onClick(p0: View) {
                        //Do nothing
                        val message = thisStringMap.getValue(word)
                        val intent =
                            Intent(this@ExaminationEditingActivity, KeywordDialogue::class.java)
                        intent.putExtra("message", message)
                        startActivityForResult(intent, REQUEST_KEYWORD_DIALOGUE)


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
     * method  to create jpg file from filename
     * @para filename
     * @return the jpg file
     */
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    /**
     * method to get the return from the [CameraEditingActivity] with the image file names,
     * the markers and the image description
     * @param requestCode
     * @param resultCode
     * @param data the data attachted to the intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_EDITOR && resultCode == Activity.RESULT_OK) {
            val currentData = data!!.getJsonExtra("imageData", ImageDataClass::class.java)
            currentPhotoPath = currentData!!.imageFiles
            currentPhotoDescription = currentData.imageDescription
            currentMarker = currentData.marker
            currentImageMarked=currentData.imageMarked
            if (currentPhotoPath.size >= 1) {
                try {
                    val takenImage = BitmapFactory.decodeFile(currentPhotoPath[0])
                    ee_imageView.setImageBitmap(takenImage)
                    ee_image_text.text=currentPhotoDescription[0]
                } catch (e: Exception) {
                    //if anything goes wrong causing exception, get and show exception message
                    Toast.makeText(this@ExaminationEditingActivity, e.message, Toast.LENGTH_LONG).show()
                }


            }
            //return from the keyword_dialogue activity
        } else if (requestCode == REQUEST_KEYWORD_DIALOGUE) {
            onResume()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        onRestart()
    }


}

