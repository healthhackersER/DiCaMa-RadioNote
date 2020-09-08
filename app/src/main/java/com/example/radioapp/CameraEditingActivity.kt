package com.example.radioapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_camera.*
import java.nio.file.Paths

/**
 * Camera editing activity shows a list of taken images to an examination Item in a horizontal view
 * and the selected image in an zoomable, panable big view. Markers can be set on the image and
 * description to each image can be entered
 *
 * @constructor Create empty Camera editing activity
 */
class CameraEditingActivity : AppCompatActivity(), RecyclerAdapter.OnItemClickListener {

    //currentImagePath is a variable to store temporary filepath
    private var currentImagePath: String = ""
    private lateinit var adapter: RecyclerAdapter
    private lateinit var currentPhotoImages: MutableList<String>
    private lateinit var currentPhotoDescription: MutableList<String>
    private lateinit var currentMarker: MutableList<FloatArray>
    private lateinit var currentSelection: MutableList<Boolean>

    //holds the position for the selection
    private var currentPosition = -1

    // class member variable to save the X,Y coordinates of the marker
    private var lastTouchDownXY: FloatArray? = FloatArray(2)

    //Creates an OnTouchListener which saves the X,Y values of the touch to in lastTouchDownXY
    @SuppressLint("ClickableViewAccessibility")
    var touchListener = OnTouchListener { v, event ->
        if (currentPhotoImages.size >= 1) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                lastTouchDownXY!![0] = event.x
                lastTouchDownXY!![1] = event.y
            }
        }

        return@OnTouchListener false
    }

    //draws an marker at the position saved in lastTouchDownXY
    var clickListener: View.OnClickListener =
        View.OnClickListener { // retrieve the stored coordinates
            if (currentPhotoImages.size >= 1) {
                val x = lastTouchDownXY!![0]
                val y = lastTouchDownXY!![1]

                // use the coordinates to draw a pin
                currentMarker[currentPosition].set(0, x)
                currentMarker[currentPosition].set(1, y)

                big_imageView.setPin(PointF(x, y))
            }


        }

    /**
     * Initialize the Camera Editing activity
     *
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        //get the intent from the caller activity
        val intent = getIntent()

        //extract the data from the intent
        val imageData = intent.getJsonExtra("imageData", ImageDataClass::class.java)
        currentPhotoImages = imageData!!.imageFiles
        currentPhotoDescription = imageData.imageDescription
        currentMarker = imageData.marker
        currentSelection = MutableList(currentPhotoImages.size) { false }

        //setting up values for the different buttons
        val photoButton = findViewById<ImageButton>(R.id.camera_Button)
        val deleteCameraButton = findViewById<ImageButton>(R.id.delete_camera_button)
        val okCameraButton = findViewById<Button>(R.id.ok_camera_button)
        val editBildbeschreibung = findViewById<TextView>(R.id.edit_text_Bildbeschreibung)


        //setting layout manager to horizontal
        val recyclerView = findViewById<RecyclerView>(R.id.rv_recyclerview)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        adapter = RecyclerAdapter(
            currentPhotoImages,
            currentPhotoDescription, currentSelection, this
        )
        recyclerView.adapter = adapter

        //setting up the initial image in the big image viewer
        if (currentPhotoImages.size >= 1) {
            simulateClick(0)
        } else if (currentPhotoImages.size == 0) {
            val defaultImage = BitmapFactory.decodeResource(this.resources, R.drawable.xray_flower)
            big_imageView.setImage(ImageSource.bitmap(defaultImage))
        }

        //setting the marker functions to the big image viewer
        big_imageView.setOnTouchListener(touchListener);
        big_imageView.setOnClickListener(clickListener);

        //taking a new image
        photoButton.setOnClickListener {

            //check permissions
            if (hasNoPermissions(this)) {
                requestPermission(this)
            }

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile(this)

            //creating the Uri necessary for SDK 29 up
            val fileProvider =
                FileProvider.getUriForFile(this, "com.example.radioapp.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            //starting the camera activity with check if it is possible
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, ExaminationEditingActivity.REQUEST_TAKE_PHOTO)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
            currentImagePath = photoFile.path.toString()

        }

        //delete the currently selected image item
        deleteCameraButton.setOnClickListener {
            onDeleteImage()
        }

        //finishing the Camera editing activity
        okCameraButton.setOnClickListener {
            onOkButton()
        }

        //setting the entered text in an extra window
        editBildbeschreibung.setOnClickListener {
            editCameraDialog(editBildbeschreibung, ExaminationEditingActivity.EDIT_TEXT)
        }


    }

    /**
     * Returns the results as an [ImageDataClass] to the [ExaminationEditingActivity] and
     * closes the current instance of the [CameraEditingActivity]
     *
     */
     private fun onOkButton(){
        val okIntent = Intent(this, ExaminationEditingActivity::class.java)
        val imageData =
            ImageDataClass(currentPhotoImages, currentPhotoDescription, currentMarker)
        okIntent.putExtraJson("imageData", imageData)
        setResult(Activity.RESULT_OK, okIntent)
        finish()
    }

    /**
     * deletes the image files and list Item Object that is currently selected and moves the
     * selection to previous item in the list if possible else to the next or to -1
     *
     */
     private fun onDeleteImage(){
        if (currentPosition != -1) {
            val path = Paths.get(currentPhotoImages[currentPosition])
            if (path.delete()) {
                println("Deleted ${path.fileName}")
            } else {
                println("Could not delete ${path.fileName}")
            }
            currentPhotoImages.removeAt(currentPosition)
            currentPhotoDescription.removeAt(currentPosition)
            currentMarker.removeAt(currentPosition)
            currentSelection.removeAt(currentPosition)
            if (currentPosition - 1 != -1 && currentPosition != 0) {
                simulateClick(currentPosition - 1)
            } else if (currentPosition == 0 && currentPhotoImages.size >= 1) {
                simulateClick(0)
            } else if (currentPosition == 0 && currentPhotoImages.size == 0) {
                currentPosition = -1
            }
            adapter.notifyDataSetChanged()
        }
    }


    /**
     * selects the Item at the clicked position on the view
     *
     * @param position as Int
     */
    override fun onItemClick(position: Int) {

        val currentImage = BitmapFactory.decodeFile(currentPhotoImages[position])
        big_imageView.setImage(ImageSource.bitmap(currentImage))
        currentSelection[currentPosition] = false
        currentSelection[position] = true
        currentPosition = position
        adapter.notifyDataSetChanged()
        edit_text_Bildbeschreibung.text = currentPhotoDescription[position]
        val coordinates = currentMarker[position]
        big_imageView.setPin(PointF(coordinates[0], coordinates[1]))

    }
    /**
     * get the images from the Camera App
     *
     * @param requestCode
     * @param resultCode
     * @param data the image file is attached to the Intent
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ExaminationEditingActivity.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            val takenImage = BitmapFactory.decodeFile(currentImagePath)
            //draws the pin outside of the big image viewer to reset it
            big_imageView.setImage(ImageSource.bitmap(takenImage))
            big_imageView.setPin(PointF(-10000f, -10000f))
            currentPhotoImages.add(currentImagePath)
            currentPhotoDescription.add("")
            currentMarker.add(floatArrayOf(-10000f, -10000f))
            if (currentPosition != -1) {
                currentSelection[currentPosition] = false
            }
            currentSelection.add(true)
            edit_text_Bildbeschreibung.text = ""
            currentPosition = currentPhotoDescription.size - 1
            adapter.notifyDataSetChanged()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        onRestart()
    }

    /**
     * Help function to simulate a user Input at the defined position on the List
     *
     * @param position the position which the click is simulated on
     * @exception Exception with println of location
     */
     private fun simulateClick(position: Int) {
        //check if nothing is selected before deselecting old item
        if (currentPosition != -1) {
            if (currentSelection.size >= currentPosition + 1) {
                currentSelection[currentPosition] = false
            }
        }
        //selecting new item
        try {
            currentPosition = position
            val currentImage = BitmapFactory.decodeFile(currentPhotoImages[position])
            big_imageView.setImage(ImageSource.bitmap(currentImage))
            currentSelection[currentPosition] = true
            val coordinates = currentMarker[position]
            big_imageView.setPin(PointF(coordinates[0], coordinates[1]))
            edit_text_Bildbeschreibung.text = currentPhotoDescription[position]
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            println("Exception loading on Create CameraActivity")
        }
    }
    /**
     * opens up a bottom sheet dialogue in order to enter Text
     *
     * @param target the TextView in which the Text Input is saved in
     * @param flag if other data type then multiline Text is supposed to be entered
     *
     */
     private fun editCameraDialog(target: TextView, flag: Int) {

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        val inputMethodManager: InputMethodManager = this.getSystemService(
            AppCompatActivity.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        val text = bottomSheetDialog.findViewById<TextView>(R.id.editTextTextPersonName)
        val next = bottomSheetDialog.findViewById<ImageButton>(R.id.imageButton)
        val hint = bottomSheetDialog.findViewById<TextView>(R.id.textHint)
        //showing the hin of the TextView
        hint!!.text = target.hint

        if (text != null) {
            text.text = target.text.toString()
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


                target.text = text!!.text.toString()
                //force hide the keyboard
                inputMethodManager.hideSoftInputFromWindow(text.windowToken, 0)
                if (currentPosition != -1) {
                    currentPhotoDescription[currentPosition] = text!!.text.toString()
                }
                bottomSheetDialog.dismiss()
            }
        }


    }


}

