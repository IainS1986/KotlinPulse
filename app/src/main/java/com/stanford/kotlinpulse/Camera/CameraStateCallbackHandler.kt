package com.stanford.kotlinpulse.Camera

import android.hardware.camera2.CameraDevice

class CameraStateCallbackHandler(onOpened : (device : CameraDevice) -> Unit) : CameraDevice.StateCallback()
{
    var OnOpened: (device : CameraDevice) -> Unit

    init {
        this.OnOpened = onOpened
    }

    override fun onOpened(p0: CameraDevice) {
        OnOpened(p0)
    }

    override fun onDisconnected(p0: CameraDevice) { }

    override fun onError(p0: CameraDevice, p1: Int) { }

}