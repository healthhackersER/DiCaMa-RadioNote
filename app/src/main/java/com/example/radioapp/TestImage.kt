package com.example.radioapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test_image.*


class TestImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_image)
        val intent=intent
        val file=intent.getData()
        val currentImage = MediaStore.Images.Media.getBitmap(this.contentResolver, file)


        test_imageView.setImageBitmap(currentImage)

        //setImage(ImageSource.bitmap(currentImage))
    }
}