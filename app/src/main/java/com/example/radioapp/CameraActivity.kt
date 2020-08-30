package com.example.radioapp

import android.app.Activity
import android.content.ClipDescription
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_new_list_item.*
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.*
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity() {
    private var currentImagePath:String=""
    private lateinit var adapter: RecyclerAdapter
    private lateinit var currentPhotoImages: MutableList<String>
    private lateinit var currentPhotoDescription: MutableList<String>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val intent = getIntent()
        currentPhotoImages=intent.getJsonExtra("dataDesciption", MutableList::class.java) as MutableList<String>
        currentPhotoDescription=intent.getJsonExtra("dataImage", MutableList::class.java) as MutableList<String>
        val photoButton = findViewById<ImageButton>(R.id.camera_Button)


        //setting layout manager to horizontal
        val recyclerView = findViewById<RecyclerView>(R.id.rv_recyclerview)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        adapter = RecyclerAdapter(
            currentPhotoImages,
            currentPhotoDescription
        )
        recyclerView.adapter=adapter



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






    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NewListItem.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //val takenImage = data?.extras?.get("data") as Bitmap
            val takenImage = BitmapFactory.decodeFile(currentImagePath)
            big_imageView.setImage(ImageSource.bitmap(takenImage))
            currentPhotoImages.add(currentImagePath)
            currentPhotoDescription.add("TESTSagdsagadjdfjflsfjdsfjfdsjfdsjlfkljdsjfdgfdjgfdjgifdgjidfgjgdfjgfghjdlgjfdjgfgjcvgjvdljkfdlkjhdjkhldjgjfjgdjflkhhjdflffhdhdfjldj")
            adapter.notifyDataSetChanged()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        onRestart()
    }
}