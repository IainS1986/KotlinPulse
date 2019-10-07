package com.stanford.kotlinpulse.Camera

import android.graphics.ImageFormat
import android.media.Image
import com.jjoe64.graphview.series.DataPoint
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.cvtColor
import java.nio.ByteBuffer
import java.util.*

class ImageProcessor(private val image : Image, private val time: Long, private val mainHandler: MainHandler) : Runnable
{
    override fun run()
    {
//        simpleRun()
        opencvRun()

        // you must close the Image though, for reuse in the ImageReader
        image?.close()
    }

    private fun simpleRun()
    {
        val yPlane = image.planes[0]
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

        // PPG is inverted due to the method
        val value = 255L - avgY

        // Marshal back to UI Thread
        mainHandler.SendDataPointToGraph(DataPoint(Date(time), value.toDouble()))
    }

    private fun opencvRun()
    {
        val rgbMat = yuvToRGBMat(image)
        val mean = MatOfDouble()
        val std = MatOfDouble()
        Core.meanStdDev(rgbMat, mean, std)

        // TODO Are we in RGB or BGR format???
        val r = mean.get(0, 0)[0]
        val g = mean.get(1, 0)[0]
        val b = mean.get(2 ,0)[0]
        println("Average RGB calculated to (${r}, ${g}, ${b})")

        // PPG is inverted due to the method
        val value = 255L - r

        // Marshal back to UI Thread
        mainHandler.SendDataPointToGraph(DataPoint(Date(time), value))
    }

    //https://stackoverflow.com/a/35221548/9829321
    private fun imageToMat(image : Image) : Mat
    {
        var buffer : ByteBuffer
        var rowStride : Int
        var pixelStride : Int
        val width : Int = image.width
        val height : Int = image.height
        var offset : Int = 0

        val planes = image.planes
        val data = ByteArray(image.width * image.height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8)
        val rowData = ByteArray(planes[0].rowStride)

        for (i in planes.indices)
        {
            buffer = planes[i].buffer
            rowStride = planes[i].rowStride
            pixelStride = planes[i].pixelStride

            val w = if (i == 0) width else width / 2
            val h = if (i == 0) height else height / 2

            for(r in 0 until h)
            {
                val bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
                if (pixelStride == bytesPerPixel)
                {
                    val length = w * bytesPerPixel
                    buffer.get(data, offset, length)

                    if (h - r != 1)
                    {
                        buffer.position(buffer.position() + rowStride - length)
                    }

                    offset += length
                }
                else
                {
                    if (h - r == 1)
                    {
                        buffer.get(rowData, 0, width - pixelStride + 1)
                    }
                    else
                    {
                        buffer.get(rowData, 0, rowStride)
                    }

                    for (c in 0 until w)
                    {
                        data[offset++] = rowData[c * pixelStride]
                    }
                }
            }
        }

        val mat = Mat(height + height / 2, width, CvType.CV_8UC1)
        mat.put(0,0,data)
        return mat
    }

    //https://stackoverflow.com/a/52476977/9829321
    private fun yuvToRGBMat(image : Image) : Mat
    {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val mYuv = Mat(image.height + image.height / 2, image.width, CV_8UC1)
        mYuv.put(0,0, nv21)
        val mRGB = Mat()
        cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_NV21, 3)
        return mRGB
    }
}