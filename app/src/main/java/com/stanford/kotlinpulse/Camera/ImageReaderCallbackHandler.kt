package com.stanford.kotlinpulse.Camera

import android.media.ImageReader
import android.os.Handler

class ImageReaderCallbackHandler(private val backgroundHandler : Handler, private val mainHandler: MainHandler) : ImageReader.OnImageAvailableListener
{
    private var _lastFrameTimeInMillis : Long = 0
    private var _startTime : Long = 0
    private var _totalImagesInProgress : Int = 0

    override fun onImageAvailable(p0: ImageReader?) {

        if (_totalImagesInProgress == p0?.maxImages)
        {
            println("Dropping frame as ${p0?.maxImages} still in progress so can't acquire anymore")
            return
        }

        val image = p0?.acquireLatestImage()

        if (image != null)
        {
            _totalImagesInProgress++

            if (_startTime == 0L)
            {
                _startTime = image.timestamp
            }
            _lastFrameTimeInMillis = image.timestamp

            backgroundHandler.post(ImageProcessor(image, image.timestamp - _startTime, mainHandler) {
                image.close()
                _totalImagesInProgress--
            })
        }
    }
}