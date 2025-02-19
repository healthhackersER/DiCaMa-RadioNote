package com.example.radioapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radioapp.MainActivity.Companion.VIEW_PDF
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.DottedLineSeparator
import kotlinx.android.synthetic.main.activity_create_p_d_f.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Activity to create a pdf from a [RadFileDataClass] object, gets started from [MainActivity],
 * loads the data which can be selected via checkboxes to include the data in the PDF to be created
 *
 *
 * @constructor Create empty Create PDF
 */
class CreatePDF : AppCompatActivity() {
    //the variable for the adapter
    private lateinit var listItems: RadFileDataClass
    private lateinit var includeImageList: BooleanArray
    private lateinit var includeMarkerList: BooleanArray
    private lateinit var includeImageDescription: BooleanArray
    private lateinit var adapter: PDFAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var createdPDF: File

    /**
     * Initialize the CreatePDF Activity
     *
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_p_d_f)
        var formatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        //getting the data from the intent
        val radData=intent.getJsonExtra("radData", RadFileDataClass::class.java)
        if (radData != null) {
            radData.date= LocalDate.parse(radData.stringDate,formatter)
        }
        listItems = radData!!
        //initializing the variables
        val len = listItems.image.imageFiles.size
        includeImageList = BooleanArray(len)
        includeMarkerList = BooleanArray(len)
        includeImageDescription = BooleanArray(len)

        //setting up the adapter for the recyclerView
        adapter=PDFAdapter(listItems, includeImageList, includeMarkerList, includeImageDescription)
        recyclerView = findViewById(R.id.pdf_recyclerViewer)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager=layoutManager
        recyclerView.adapter=adapter

        //setting the text views
        pdf_description_text.text=radData.examination
        val formattedDate= radData.date?.format(formatter)
        pdf_date_text.text=formattedDate.toString()
        val stringArray=resources.getStringArray(R.array.type_array)
        pdf_type_text.text=stringArray[radData.type!!]

        if (radData.storage==""){
            pdf_storage_text.text="keine Angabe"
                    }else{
            pdf_storage_text.text=radData.storage
        }
        if (radData.evaluation==""){
            pdf_evaluation_text.text="keine Angabe"
        }else {
            pdf_evaluation_text.text = radData.evaluation
        }
        if (radData.note==""){
            pdf_note_text.text="keine Angabe"
        }else {
            pdf_note_text.text = radData.note
        }


        //setting the click listener on the buttons
        pdf_cancel.setOnClickListener{onCancel()}

        pdf_create.setOnClickListener{ //we need to handle runtime permission for devices with marshmallow and above
            if (hasNoPermissions(this)) {
                requestPermission(this)
            }
            createdPDF=onCreatePDF()
            previewPDF(createdPDF)
        }
        //toggle all checkboxes
        var toggle=false
        pdf_check_all_button.setOnClickListener{
            if (toggle==false){
                for (i in includeImageList.indices) {
                    includeImageList[i]=true
                    includeMarkerList[i]=true
                    includeImageDescription[i]=true
                }
                adapter.notifyDataSetChanged()
                toggle=true
            }else{
                for (i in includeImageList.indices) {
                    includeImageList[i]=false
                    includeMarkerList[i]=false
                    includeImageDescription[i]=false
                }
                adapter.notifyDataSetChanged()
                toggle=false
            }
        }

        //initializing some checkboxes with true
        pdf_description_checkbox.isChecked = true
        pdf_date_checkbox.isChecked=true
        pdf_type_checkbox.isChecked=true
        pdf_evaluation_checkbox.isChecked=true
    }
    /**
     * returns to [MainActivity] and closes current [CreatePDF] instance
     *
     */
    private fun onCancel(){

        val intent = Intent(this, MainActivity::class.java)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }
    /**
     * creates a PDF from the data selected via Checkboxes
     *
     * @return the created PDF as [File]
     */

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun onCreatePDF(): File {
        val mDoc = Document()
        //pdf file name
        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())+".pdf"
        //pdf file path
        val mFileDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val mFilePath=mFileDirectory!!.resolve(mFileName)

        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))

            //open the document for writing
            mDoc.open()
            mDoc.pageSize = PageSize.A4
            //setting up Fonts and others
            val boldWhite: Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16F, Font.BOLD)
            boldWhite.color=BaseColor.WHITE
            val bold: Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16F, Font.BOLD)
            val linebreak = Chunk(DottedLineSeparator())

            //Getting Icon
            val bmp= BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round)
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val iconImage: Image = Image.getInstance(stream.toByteArray())
            iconImage.scaleAbsolute(55F, 55F)

            //Creating header table
            val table = PdfPTable(2)
            table.totalWidth = 550F
            table.isLockedWidth = true

            // first row
            val cell1 = PdfPCell(iconImage)
            cell1.fixedHeight = 60F
            cell1.border = Rectangle.NO_BORDER
            cell1.verticalAlignment = Element.ALIGN_MIDDLE
            cell1.backgroundColor= BaseColor(R.color.colorPrimaryDark)
            table.addCell(cell1)
            val cell2 = PdfPCell(Phrase("DiCaMa Files", boldWhite))
            cell2.fixedHeight = 60F
            cell2.verticalAlignment = Element.ALIGN_MIDDLE
            cell2.border = Rectangle.NO_BORDER
            cell2.backgroundColor= BaseColor(R.color.colorPrimaryDark)
            table.addCell(cell2)
            mDoc.add(table)
            mDoc.add(Chunk.NEWLINE)
            mDoc.add(Chunk.NEWLINE)

            //adding Header Info
            val table2 = PdfPTable(2)
            table2.totalWidth = 550F
            table2.isLockedWidth = true


            val cell3 = if (pdf_description_checkbox.isChecked){
                PdfPCell(Phrase(pdf_description_text.text.toString(), bold))
            }else{
                PdfPCell(Phrase("", bold))
            }
            cell3.border=Rectangle.NO_BORDER
            cell3.colspan=2
            table2.addCell(cell3)

            val cell5 = if (pdf_type_checkbox.isChecked){
                PdfPCell(Phrase(pdf_type_text.text.toString(),bold))
            }else{
                PdfPCell(Phrase("",bold))
            }
            cell5.border=Rectangle.NO_BORDER
            table2.addCell(cell5)

            val cell4 = if (pdf_date_checkbox.isChecked){
                PdfPCell(Phrase(pdf_date_text.text.toString(), bold))
            }else{
                PdfPCell(Phrase("", bold))
            }
            cell4.border=Rectangle.NO_BORDER
            table2.addCell(cell4)

            mDoc.add(table2)
            mDoc.add(linebreak)
            mDoc.add(Chunk.NEWLINE)
            mDoc.add(Chunk.NEWLINE)

            //adding Text fields
            val textField=StringBuilder()
            if(pdf_storage_checkbox.isChecked) {
                textField.appendLine("Ablageort:")
                textField.appendLine(pdf_storage_text.text)
            }
            if(pdf_evaluation_checkbox.isChecked){
                textField.appendLine("Beurteilung:")
                textField.appendLine(pdf_evaluation_text.text)
            }
            if(pdf_note_checkbox.isChecked){
                textField.appendLine("Notiz:")
                textField.appendLine(pdf_note_text.text)
            }
            if (textField.isNotEmpty()){
                mDoc.add(Paragraph(textField.toString()))
            }
            mDoc.add(linebreak)
            mDoc.add(Chunk.NEWLINE)

            //adding images
            for (i in includeImageList.indices) {
                if (includeImageList[i] && !includeMarkerList[i]) {
                    val image = Image.getInstance(listItems.image.imageFiles[i])
                    image.scaleToFit(250F,250F)
                    mDoc.add(image)
                } else if (includeImageList[i] && includeMarkerList[i]) {
                    val image = Image.getInstance(listItems.image.imageMarked[i])
                    image.scaleToFit(250F,250F)
                    mDoc.add(image)
                }
                if (includeImageDescription[i]){
                    mDoc.add(Paragraph(listItems.image.imageDescription[i]))
                }
            }

            //close document
            mDoc.close()

            //show file saved message with file name and path
//            Toast.makeText(
//                this@CreatePDF,
//                "$mFileName.pdf\nis saved to\n$mFilePath",
//                Toast.LENGTH_LONG
//            ).show()
        }
        catch (e: Exception){
            //if anything goes wrong causing exception, get and show exception message
            Toast.makeText(this@CreatePDF, e.message, Toast.LENGTH_SHORT).show()
        }
        return mFilePath
    }
    /**
     * starts the activity [PdfViewer] to preview the created pdf
     *
     * @param file the pdf file which is to be previewd
     */
    private fun previewPDF(file: File){
        val intent = Intent(this, PdfViewer::class.java)
        val path=file.absolutePath.toString()
        intent.putExtra("filePath", path)
        startActivityForResult(intent, VIEW_PDF)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //when returning from edit
        if (requestCode== VIEW_PDF && resultCode == RESULT_OK){
            onRestart()
            //when returning from cancel
        }else if (requestCode==VIEW_PDF && resultCode == RESULT_CANCELED){
            onCancel()
        }
    }

}
