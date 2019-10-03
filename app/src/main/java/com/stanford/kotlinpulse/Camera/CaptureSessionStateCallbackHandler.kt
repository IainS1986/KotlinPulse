package com.stanford.kotlinpulse.Camera

import android.hardware.camera2.CameraCaptureSession

class CaptureSessionStateCallbackHandler(onConfigured : (session : CameraCaptureSession) -> Unit) : CameraCaptureSession.StateCallback()
{
    var OnConfigured: (session : CameraCaptureSession) -> Unit

    init {
        this.OnConfigured = onConfigured
    }

    override fun onConfigured(p0: CameraCaptureSession) {
        OnConfigured(p0)
    }

    override fun onConfigureFailed(p0: CameraCaptureSession) { }
}