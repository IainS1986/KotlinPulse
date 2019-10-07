package com.stanford.kotlinpulse.Camera

import android.media.ImageReader
import android.os.Handler

class ImageReaderCallbackHandler(private val backgroundHandler : Handler, private val mainHandler: MainHandler) : ImageReader.OnImageAvailableListener
{
    private var _lastFrameTimeInMillis : Long = 0
    private var _startTime : Long = 0

    override fun onImageAvailable(p0: ImageReader?) {
        val currentTime = System.currentTimeMillis()

        if (_startTime == 0L)
        {
            _startTime = currentTime
        }

        val diff = currentTime - _lastFrameTimeInMillis

        println("New Frame - Diff is ${diff}ms running at approx ${1000L / diff}FPS")

        backgroundHandler.post(ImageProcessor(p0?.acquireNextImage()!!, currentTime - _startTime, mainHandler))
        _lastFrameTimeInMillis = currentTime
    }
}