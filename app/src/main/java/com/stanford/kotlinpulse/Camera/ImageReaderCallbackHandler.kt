package com.stanford.kotlinpulse.Camera

import android.media.ImageReader
import android.os.Handler

class ImageReaderCallbackHandler(handler : Handler) : ImageReader.OnImageAvailableListener
{
    var _backgroundHandler: Handler = handler

    private var _lastFrameTimeInMillis : Long = 0

    override fun onImageAvailable(p0: ImageReader?) {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - _lastFrameTimeInMillis
        println("New Frame - Diff is ${diff}ms running at approx ${1000L / diff}FPS")

        _backgroundHandler.post(ImageProcessor(p0?.acquireNextImage()!!))
        _lastFrameTimeInMillis = currentTime
    }
}