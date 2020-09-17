package com.example.radioapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
    private lateinit var currentMarker: MutableList<MutableList<FloatArray>>
    private lateinit var currentSelection: MutableList<Boolean>
    private lateinit var currentImageMarked: MutableList<String>

    //holds the state of the marker button
    private var markerToggle = false

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
    private var clickListener: View.OnClickListener =
        View.OnClickListener { // retrieve the stored coordinates
            if (currentPhotoImages.size >= 1) {
                val x = lastTouchDownXY!![0]
                val y = lastTouchDownXY!![1]

                // use the coordinates to draw a pin
                if (markerToggle==true && currentPosition!=-1) {
                    currentMarker[currentPosition].add(floatArrayOf(-10000f, -10000f))
                    val lastIndex = currentMarker[currentPosition].lastIndex
                    currentMarker[currentPosition][lastIndex][0] = x
                    currentMarker[currentPosition][lastIndex][1] = y
                    val pointArray= listToArray(currentMarker[currentPosition])
                    big_imageView.setPins(pointArray)
                    saveMarkedImage()

                }


            }


        }
    lateinit var recyclerView: RecyclerView

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
        currentImageMarked=imageData.imageMarked
        currentSelection = MutableList(currentPhotoImages.size) { false }

        //setting up values for the different buttons

        val okCameraButton = findViewById<Button>(R.id.ok_camera_button)
        val editBildbeschreibung = findViewById<TextView>(R.id.edit_text_Bildbeschreibung)


        //setting layout manager to horizontal
        recyclerView = findViewById<RecyclerView>(R.id.rv_recyclerview)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        adapter = RecyclerAdapter(
            currentPhotoImages,
            currentPhotoDescription, currentSelection, this
        )
        recyclerView.adapter = adapter

        //setting up the initial image in the big image viewer
        val defaultImage = BitmapFactory.decodeResource(this.resources, R.drawable.camera_image)
        big_imageView.setImage(ImageSource.bitmap(defaultImage))


        //setting the marker functions to the big image viewer
        big_imageView.setOnTouchListener(touchListener);
        big_imageView.setOnClickListener(clickListener);

        //taking a new image
        ca_add_image.setOnClickListener {

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
                startActivityForResult(
                    takePictureIntent,
                    ExaminationEditingActivity.REQUEST_TAKE_PHOTO
                )
            } else {
                Toast.makeText(this, R.string.unable_to_open_camera, Toast.LENGTH_SHORT).show()
            }
            currentImagePath = photoFile.path.toString()

        }

        //finishing the Camera editing activity
        okCameraButton.setOnClickListener {
            onOkButton()
        }

        //setting the entered text in an extra window
        editBildbeschreibung.setOnClickListener {
            editCameraDialog(editBildbeschreibung, ExaminationEditingActivity.EDIT_TEXT)
        }
        ca_toggle_button.setOnCheckedChangeListener{_, isChecked ->
            markerToggle = isChecked
        }

        ca_delete_marker.setOnClickListener {
            deleteLastMarker()
        }

        window.decorView.post {
            if (currentPhotoImages.size >= 1) {
                simulateClick(0)
            }
        }
    }
    /**
     * saves the current image View with the markers to bitmap file
     *
     */
    private fun saveMarkedImage(){
        val filename= createImageFile(this)

        val stream=filename.outputStream()
        getBitmapFromView(big_imageView)?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        if (currentPosition!=-1){
            if(currentImageMarked[currentPosition].isEmpty()) {
                currentImageMarked[currentPosition] = filename.absolutePath
            }else{
                val pathMarked = Paths.get(currentImageMarked[currentPosition])
                if (pathMarked.delete()) {
                    println("Deleted ${pathMarked.fileName}")
                } else {
                    println("Could not delete ${pathMarked.fileName}")
                }
                currentImageMarked[currentPosition] = filename.absolutePath
            }
        }
    }

    /**
     * removes the last Marker of the currently selected Item
     *
     */
    private fun deleteLastMarker(){
        if(currentPosition!=-1){
            if(currentMarker[currentPosition].size>=1){
                currentMarker[currentPosition].removeLast()
                val pointArray=listToArray(currentMarker[currentPosition])
                big_imageView.setPins(pointArray)
                saveMarkedImage()
            }

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
            ImageDataClass(currentPhotoImages, currentPhotoDescription, currentMarker, currentImageMarked)
        okIntent.putExtraJson("imageData", imageData)
        setResult(Activity.RESULT_OK, okIntent)
        finish()
    }

    /**
     * selects the Item at the clicked position on the view
     *
     * @param position as Int
     */
    override fun onItemClick(position: Int) {

        val currentImage = BitmapFactory.decodeFile(currentPhotoImages[position])
        //big_imageView.setImage(ImageSource.bitmap(currentImage))
        val scaledImage= createScaledBitmap(currentImage,big_imageView.width,big_imageView.height,false)
        big_imageView.setImage(ImageSource.bitmap(scaledImage))
        if (currentPosition!=-1){
            currentSelection[currentPosition] = false
        }
        currentSelection[position] = true
        currentPosition = position
        adapter.notifyDataSetChanged()
        edit_text_Bildbeschreibung.text = currentPhotoDescription[position]
        val pointArray= listToArray(currentMarker[position])
        big_imageView.setPins(pointArray)
        saveMarkedImage()

    }

    /**
     * deletes the RecyclerView Item at position
     *
     * @param position as Int
     */
    override fun onButtonClick(position: Int) {
        if (position==currentPosition) {
            if (currentPosition != -1) {
                val path = Paths.get(currentPhotoImages[currentPosition])
                val pathMarked = Paths.get(currentImageMarked[currentPosition])
                if (path.delete() && pathMarked.delete()) {
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
        }else{
            val path = Paths.get(currentPhotoImages[position])
            val pathMarked = Paths.get(currentImageMarked[currentPosition])
            if (path.delete() && pathMarked.delete()) {
                println("Deleted ${path.fileName}")
            } else {
                println("Could not delete ${path.fileName}")
            }
            currentPhotoImages.removeAt(position)
            currentPhotoDescription.removeAt(position)
            currentMarker.removeAt(position)
            currentSelection.removeAt(position)
            adapter.notifyDataSetChanged()
        }
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
            //big_imageView.setImage(ImageSource.bitmap(takenImage))
            val scaledImage=createScaledBitmap(takenImage,big_imageView.width,big_imageView.height,false)
            big_imageView.setImage(ImageSource.bitmap(scaledImage))
            val tempArray=arrayOf(PointF(-10000f, -10000f))
            big_imageView.setPins(tempArray)
            currentPhotoImages.add(currentImagePath)
            currentPhotoDescription.add("")
            currentMarker.add(mutableListOf<FloatArray>())
            if (currentPosition != -1) {
                currentSelection[currentPosition] = false
            }
            currentSelection.add(true)
            edit_text_Bildbeschreibung.text = ""
            currentPosition = currentPhotoDescription.size - 1
            currentImageMarked.add("")
            saveMarkedImage()
            adapter.notifyDataSetChanged()
            //scrolling to position
            recyclerView.scrollToPosition(currentPosition)


        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        onResume()

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
            //big_imageView.setImage(ImageSource.bitmap(currentImage))
            val scaledImage=createScaledBitmap(currentImage,big_imageView.width,big_imageView.height,false)
            big_imageView.setImage(ImageSource.bitmap(scaledImage))
            currentSelection[currentPosition] = true
            val pointArray=listToArray(currentMarker[position])
            big_imageView.setPins(pointArray)
            edit_text_Bildbeschreibung.text = currentPhotoDescription[position]
            adapter.notifyDataSetChanged()
            saveMarkedImage()
        } catch (e: Exception) {
            //if anything goes wrong causing exception, get and show exception message
            Toast.makeText(this@CameraEditingActivity, e.message, Toast.LENGTH_LONG).show()
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

        val text = bottomSheetDialog.findViewById<TextView>(R.id.bs_edit_editText)
        val next = bottomSheetDialog.findViewById<ImageButton>(R.id.bs_done_button)
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
    /**
     * method to get a bitmap from the view
     *
     * @param view from which to get the bitmap
     * @return the bitmap from the view
     *
     */
    open fun getBitmapFromView(view: View): Bitmap? {
        var bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


}

