package com.example.radioapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
//import com.davemorrissey.labs.subscaleview.test.R.drawable


/**
 * Pin view extends the big image viewer in [CameraEditingActivity] to draw pin markers
 *
 * @constructor
 *
 * @param context
 * @param attr
 */
class PinView @JvmOverloads constructor(context: Context?, attr: AttributeSet? = null) :
    SubsamplingScaleImageView(context, attr) {
    private val paint = Paint()
    private val vPin = PointF()
    private var sPin: PointF? = null
    private var sPinArray: Array<PointF>? = null
    private var pin: Bitmap? = null

    /**
     * method to set the pin
     *
     * @param sPin
     */
    fun setPin(sPin: PointF?) {
        this.sPin = sPin
        initialise()
        invalidate()
    }
    fun setPins(sPins : Array<PointF>?){
        this.sPinArray = sPins
        initialise()
        invalidate()
    }

    /**
     * initialize the [PinView] class
     */
    private fun initialise() {
        val density = resources.displayMetrics.densityDpi.toFloat()
        pin = BitmapFactory.decodeResource(this.resources, R.drawable.pushpin_blue)
        var localPin=pin!!
        val w = (density/420f * localPin.width)/4
        val h = (density/420f * localPin.height)/4
        pin = Bitmap.createScaledBitmap(localPin, w.toInt(), h.toInt(), true)
    }

    /**
     * draw the pin on the canvas
     *
     * @param canvas the image view which is supposed to be drawn on
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady) {
            return
        }
        paint.isAntiAlias = true
//        if (sPin != null && pin != null) {
//            sourceToViewCoord(sPin, vPin)
//            val vX = vPin.x - pin!!.width / 2
//            val vY = vPin.y + pin!!.height
//            canvas.drawBitmap(pin!!, vX, vY, paint)
//        }
        if (sPinArray != null && pin != null) {
            for (i in sPinArray!!.indices){
                sourceToViewCoord(sPinArray!![i], vPin)
                val vX = vPin.x - pin!!.width / 2
                val vY = vPin.y - pin!!.height
                canvas.drawBitmap(pin!!, vX, vY, paint)
            }
        }
    }

    init {
        initialise()
    }
}