package com.stanford.kotlinpulse.Camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Camera
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Handler
import android.view.Surface
import android.view.SurfaceView
import com.stanford.kotlinpulse.R
import kotlinx.android.synthetic.main.activity_main.view.*
import java.lang.Exception

class CameraEngine(activity: Activity) {

    private var Manager : CameraManager? = null

    private var Preview : SurfaceView? = null

    private var Device : CameraDevice? = null

    private var PreviewSurface : Surface? = null

    private var Activity : Activity? = null

    init
    {
        this.Activity = activity
    }

    @SuppressLint("MissingPermission")
    fun start()
    {
        Preview = Activity!!.findViewById(R.id.surfaceView) as SurfaceView

        val cameraStateCallback = CameraStateCallbackHandler(::OnCameraDeviceOpened)
        Manager = Activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // First check we even have cameras
        if (Manager?.cameraIdList?.isEmpty() == true)
        {
            // TODO Return error or fire off error event
            return
        }

        // Get the first rear facing camera
        val camId = getFirstRearFacingCamera(Manager!!)
        if (camId == "")
        {
            //TODO Return error or fire off error event for no rear camera
            return
        }

        // Open Camera
        Manager!!.openCamera(camId, cameraStateCallback, Handler { true })
    }

    private fun OnCameraDeviceOpened(device: CameraDevice)
    {
        Device = device

        val cameraCharacteristics = Manager?.getCameraCharacteristics(device.id)

        val streamConfigurationMap = cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
        val sizes = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)

        // TODO Get optimal size
        val resolution = sizes.last()

        val displayRotation = Activity!!.windowManager.defaultDisplay.rotation

        val swappedDimension = areDimensionsSwapped(displayRotation, cameraCharacteristics)

        val rotatedPreviewWidth = if (swappedDimension) resolution.height else resolution.width
        val rotatedPreviewHeight = if (swappedDimension) resolution.width else resolution.height

        Preview!!.holder.setFixedSize(rotatedPreviewWidth, rotatedPreviewHeight)

        val previewSurface = Preview!!.holder.surface
        val captureSessionStateCallbackHandler = CaptureSessionStateCallbackHandler(::OnSessionConfigured)

        PreviewSurface = previewSurface
        device.createCaptureSession(mutableListOf(previewSurface), captureSessionStateCallbackHandler, Handler { true })
    }

    private fun OnSessionConfigured(session : CameraCaptureSession)
    {
        val previewRequestBuilder = Device?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)!!.apply { addTarget(PreviewSurface!!) }

        session.setRepeatingRequest(previewRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {}, Handler { true })
    }

    private fun getFirstRearFacingCamera(manager:CameraManager): String
    {
        try
        {
            for (id in manager.cameraIdList)
            {
                val cam = manager.getCameraCharacteristics(id)
                if (cam.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK)
                {
                    return id
                }
            }
        }
        catch(e: Exception)
        {
        }

        return ""
    }

    /**
     * Helper function from the Camera2 example projects to check/ensure
     * orientation of the camera preview matches orientation of the device
     */
    private fun areDimensionsSwapped(displayRotation: Int,
                                     cameraCharacteristics: CameraCharacteristics) : Boolean
    {
        var swappedDimensions = false

        when(displayRotation)
        {
            Surface.ROTATION_0, Surface.ROTATION_180 ->
            {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 ||
                        cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 270)
                {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 ->
            {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 ||
                    cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 180)
                {
                    swappedDimensions = true
                }
            }
        }

        return swappedDimensions
    }
}