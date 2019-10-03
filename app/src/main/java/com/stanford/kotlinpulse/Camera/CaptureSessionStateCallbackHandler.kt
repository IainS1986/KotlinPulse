package com.stanford.kotlinpulse.Camera

import android.hardware.camera2.CameraCaptureSession

class CaptureSessionStateCallbackHandler(callback : (session : CameraCaptureSession) -> Unit) : CameraCaptureSession.StateCallback()
{
    var onConfiguredCallback : (session : CameraCaptureSession) -> Unit = callback

    override fun onConfigured(p0: CameraCaptureSession) {
        onConfiguredCallback(p0)
    }

    override fun onConfigureFailed(p0: CameraCaptureSession) { }
}