package com.stanford.kotlinpulse.Camera

import android.hardware.camera2.CameraDevice

class CameraStateCallbackHandler(callback : (device : CameraDevice) -> Unit) : CameraDevice.StateCallback()
{
    var onOpenedCallback: (device : CameraDevice) -> Unit = callback

    override fun onOpened(p0: CameraDevice) {
        onOpenedCallback(p0)
    }

    override fun onDisconnected(p0: CameraDevice) { }

    override fun onError(p0: CameraDevice, p1: Int) { }

}