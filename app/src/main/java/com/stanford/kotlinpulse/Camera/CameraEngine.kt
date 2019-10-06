package com.stanford.kotlinpulse.Camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.SurfaceView
import com.stanford.kotlinpulse.R
import java.lang.Exception
import java.lang.RuntimeException

class CameraEngine(activity: Activity) {

    private var _backgroundThread : HandlerThread? = null

    private var _backgroundHandler : Handler? = null

    private lateinit var _cameraManager : CameraManager

    private lateinit var _preview : SurfaceView

    private lateinit var _cameraDevice : CameraDevice

    private lateinit var _previewSurface : Surface

    private lateinit var _imageReader : ImageReader

    private lateinit var _imageReaderHandler : ImageReaderCallbackHandler

    private var _activity : Activity = activity

    @SuppressLint("MissingPermission")
    fun start()
    {
        startBackgroundThread()
        _preview = _activity.findViewById(R.id.surfaceView) as SurfaceView

        val cameraStateCallback = CameraStateCallbackHandler(::onCameraDeviceOpened)
        _cameraManager = _activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // First check we even have cameras
        if (_cameraManager.cameraIdList.isEmpty())
        {
            // TODO Return error or fire off error event
            return
        }

        // Get the first rear facing camera
        val camId = getFirstRearFacingCamera(_cameraManager)
        if (camId == "")
        {
            //TODO Return error or fire off error event for no rear camera
            return
        }

        // Open Camera
        _cameraManager.openCamera(camId, cameraStateCallback, Handler { true })
    }

    fun stop()
    {
        stopBackgroundThread()

        try {
            _cameraDevice.close()
            _imageReader.close()
        }
        catch(e : InterruptedException)
        {
            throw RuntimeException("Interrupted while trying to close Camera.", e)
        }
        finally {

        }
    }

    private fun onCameraDeviceOpened(device: CameraDevice) {
        _cameraDevice = device

        val cameraCharacteristics = _cameraManager.getCameraCharacteristics(device.id)

        val streamConfigurationMap =
            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
        val sizes = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)

        // TODO Get optimal size
        val resolution = Size(480,640) //sizes.last();

        val displayRotation = _activity.windowManager.defaultDisplay.rotation

        val swappedDimension = areDimensionsSwapped(displayRotation, cameraCharacteristics)

        val rotatedPreviewWidth = if (swappedDimension) resolution.height else resolution.width
        val rotatedPreviewHeight = if (swappedDimension) resolution.width else resolution.height

        _preview.holder.setFixedSize(rotatedPreviewWidth, rotatedPreviewHeight)

        val previewSurface = _preview.holder.surface
        val captureSessionStateCallbackHandler =
            CaptureSessionStateCallbackHandler(::onSessionConfigured)

        _previewSurface = previewSurface

        // Setup Image Reader
        _imageReaderHandler = ImageReaderCallbackHandler(_backgroundHandler!!)
        _imageReader = ImageReader.newInstance(
            rotatedPreviewWidth,
            rotatedPreviewHeight,
            ImageFormat.YUV_420_888,
            2
        )
        _imageReader.setOnImageAvailableListener(_imageReaderHandler, _backgroundHandler)

        device.createCaptureSession(mutableListOf(_previewSurface, _imageReader.surface), captureSessionStateCallbackHandler, null)
    }

    private fun onSessionConfigured(session : CameraCaptureSession)
    {
        val previewRequestBuilder = _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder.addTarget(_previewSurface)
        previewRequestBuilder.addTarget(_imageReader.surface)

        session.setRepeatingRequest(previewRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {}, _backgroundHandler)
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


    fun startBackgroundThread()
    {
        _backgroundThread = HandlerThread("Camera")
        _backgroundThread?.let { test -> test.start() }
        _backgroundThread?.let { test -> _backgroundHandler = Handler(test.looper) }
    }


    fun stopBackgroundThread()
    {
        _backgroundThread?.quitSafely()
        try {
            _backgroundThread?.join()
            _backgroundThread = null
            _backgroundHandler = null
        }
        catch(e : Exception)
        {
            println(e.stackTrace)
        }
    }
}