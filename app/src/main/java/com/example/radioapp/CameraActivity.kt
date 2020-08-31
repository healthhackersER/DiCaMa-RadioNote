package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_camera.*
import kotlin.properties.Delegates

/**
 * Camera Activity to open up a camera Dialogue to make multiple editable cameras
 */
class CameraActivity : AppCompatActivity(), RecyclerAdapter.OnItemClickListener {

    //currentImagePath is a variable to store temporary filepath
    private var currentImagePath:String=""
    private lateinit var adapter: RecyclerAdapter
    private lateinit var currentPhotoImages: MutableList<String>
    private lateinit var currentPhotoDescription: MutableList<String>
    private var currentPosition = -1


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val intent = getIntent()
        currentPhotoImages=intent.getJsonExtra("dataImages", MutableList::class.java) as MutableList<String>
        currentPhotoDescription=intent.getJsonExtra("dataDescription", MutableList::class.java) as MutableList<String>

        //different Buttons
        val photoButton = findViewById<ImageButton>(R.id.camera_Button)
        val editCameraButton = findViewById<ImageButton>(R.id.edit_camera_button)
        val deleteCameraButton = findViewById<ImageButton>(R.id.delete_camera_button)
        val okCameraButton = findViewById<Button>(R.id.ok_camera_button)

        val editBildbeschreibung = findViewById<TextView>(R.id.edit_text_Bildbeschreibung)



        //setting layout manager to horizontal
        val recyclerView = findViewById<RecyclerView>(R.id.rv_recyclerview)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        adapter = RecyclerAdapter(
            currentPhotoImages,
            currentPhotoDescription, this
        )
        recyclerView.adapter=adapter

        //setting up on first image display
        if (currentPhotoImages.size>=1){
            currentPosition=0
            val currentImage = BitmapFactory.decodeFile(currentPhotoImages[0])
            big_imageView.setImage(ImageSource.bitmap(currentImage))
            edit_text_Bildbeschreibung.text=currentPhotoDescription[0]
        }
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
                startActivityForResult(takePictureIntent, NewListItem.REQUEST_TAKE_PHOTO)
            } else {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
            currentImagePath=photoFile.path.toString()

        }

        okCameraButton.setOnClickListener{
            val okIntent = Intent(this, NewListItem::class.java)
            currentPhotoDescription.let { it1 -> okIntent.putExtraJson("dataDescription", it1) }
            currentPhotoImages.let { it1 -> okIntent.putExtraJson("dataImage", it1) }
            setResult(Activity.RESULT_OK, okIntent)
            finish()
        }

        //setting the entered text in an extra window
        editBildbeschreibung.setOnClickListener{
            editCameraDialog(editBildbeschreibung, NewListItem.EDIT_TEXT)
        }





    }

    override fun onItemClick(position: Int) {

        val currentImage = BitmapFactory.decodeFile(currentPhotoImages[position])
        big_imageView.setImage(ImageSource.bitmap(currentImage))
        currentPosition =position
        edit_text_Bildbeschreibung.text=currentPhotoDescription[position]




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NewListItem.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //val takenImage = data?.extras?.get("data") as Bitmap
            val takenImage = BitmapFactory.decodeFile(currentImagePath)
            big_imageView.setImage(ImageSource.bitmap(takenImage))
            currentPhotoImages.add(currentImagePath)
            currentPhotoDescription.add("")
            currentPosition=currentPhotoDescription.size-1
            adapter.notifyDataSetChanged()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        onRestart()
    }



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
                    if (currentPosition != -1){
                        currentPhotoDescription[currentPosition]= text!!.text.toString()
                    }
                    bottomSheetDialog.dismiss()
                }
            }


        }


}

