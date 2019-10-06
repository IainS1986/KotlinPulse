package com.stanford.kotlinpulse.Camera

import android.media.Image

class ImageProcessor(image : Image) : Runnable
{
    val frame : Image = image

    override fun run()
    {
        // TODO Process Frame, get AVG Red Value

        val yPlane = frame.planes[0]
        val buffer = yPlane.buffer

        // Iterate through the buffer, taking average of the y plane in YUV (luminosity)
        var yTotal = 0
        while(buffer.hasRemaining())
        {
            // TODO Assuming PixelStride 1 for now and RowStride == Width
            yTotal += buffer.get()
        }

        val totalPixels : Float = buffer.position().toFloat()
        var avgY : Float = yTotal / totalPixels
        println("Average Y calculated to be ${avgY}")

        // TODO If we want to reuse the image, rewind the buffer! Currently, we throw it away so who cares
        // you must close the Image though, for reuse in the ImageReader
        frame?.close()
    }
}