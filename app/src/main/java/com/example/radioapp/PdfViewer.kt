package com.example.radioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_pdf_viewer.*
import java.io.File
import java.nio.file.Paths


/**
 * Activity to preview an PDF file before sharing it
 *
 * @constructor Create empty PDF Viewer
 */
class PdfViewer : AppCompatActivity() {
    private lateinit var file:File
    /**
     * Initialize a PdfViewer
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        //extracting the filePath
        val filePath = intent.getStringExtra("filePath")
        file = File(filePath)
        //setting the pdf to the pdf viewer
        try{
            pv_pdfView.fromFile(file).load()
        }catch (e: Exception) {
            //if anything goes wrong causing exception, get and show exception message
            Toast.makeText(this@PdfViewer, e.message, Toast.LENGTH_LONG).show()
        }
        pv_edit_button.setOnClickListener{
            onEdit()
        }
        pv_finished_button.setOnClickListener{
            onDelete()
        }
        pv_share_button.setOnClickListener {
            shareFile(file)
        }
    }
    /**
     * method to delete the current pdf and return to the [CreatePDF] instance
     */
    private fun onEdit(){
        val intent = Intent(this, CreatePDF::class.java)
        val path = Paths.get(file.absolutePath)
        if (path.delete()) {
            println("Deleted ${path.fileName}")
        } else {
            println("Could not delete ${path.fileName}")
        }
        setResult(Activity.RESULT_OK, intent)
        finish()

            }

    /**
     * method to delete the current pdf and return to the [MainActivity]
     */
    private fun onDelete(){
        val intent = Intent(this, CreatePDF::class.java)
        val path = Paths.get(file.absolutePath)
        if (path.delete()) {
            println("Deleted ${path.fileName}")
        } else {
            println("Could not delete ${path.fileName}")
        }
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    /**
     * method to share the pdf with other applications
     */
    private fun shareFile(file: File) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        //create Uri with FileProvider required above API 24+
        val pdfUri = FileProvider.getUriForFile(this, "com.example.radioapp.fileprovider", file)
        sharingIntent.type = "application/pdf"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, pdfUri)
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooserIntent = Intent.createChooser(sharingIntent, "Share PDF using")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(chooserIntent)
        } catch (e: Exception) {
            //if anything goes wrong causing exception, get and show exception message
            Toast.makeText(this@PdfViewer, e.message, Toast.LENGTH_LONG).show()
        }
    }
}

