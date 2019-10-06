package com.stanford.kotlinpulse.Camera

import android.media.ImageReader
import android.os.Handler

class ImageReaderCallbackHandler(handler : Handler) : ImageReader.OnImageAvailableListener
{
    var _backgroundHandler: Handler = handler

    private var _lastFrameTimeInMillis : Long = 0

    override fun onImageAvailable(p0: ImageReader?) {
        //_backgroundHandler.post(p0.acquireNextImage())

        val image = p0?.acquireNextImage()
        val currentTime = System.currentTimeMillis()
        if (_lastFrameTimeInMillis == 0L)
        {
            println("First frame arrived")
            _lastFrameTimeInMillis = currentTime
            image?.close()
            return
        }

        val diff = currentTime - _lastFrameTimeInMillis
        println("New Frame - Diff is ${diff}ms running at approx ${1000L / diff}FPS")
        _lastFrameTimeInMillis = currentTime
        image?.close()
    }
}