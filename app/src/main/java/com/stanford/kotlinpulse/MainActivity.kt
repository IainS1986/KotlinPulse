package com.stanford.kotlinpulse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import com.stanford.kotlinpulse.Camera.CameraEngine
import com.stanford.kotlinpulse.Camera.PermissionsHelper
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {

    private var Engine : CameraEngine? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Request Camera Permission (TODO StartupActivity???)
        if (!PermissionsHelper.hasCameraPermission(this))
        {
            PermissionsHelper.requestCameraPermission(this)
            return
        }

        Engine = CameraEngine(this)

        val surfaceReadyCallback = object : SurfaceHolder.Callback
        {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) { }

            override fun surfaceDestroyed(p0: SurfaceHolder?) { }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                Engine?.start()
            }
        }

        surfaceView.holder.addCallback(surfaceReadyCallback)

        // Test OpenCV
        if (!OpenCVLoader.initDebug()) {}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (!PermissionsHelper.hasCameraPermission(this))
        {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
            if (!PermissionsHelper.shouldShowRequestPermissionRationale(this))
            {
                PermissionsHelper.launchPermissionSettings(this)
            }
            finish()
        }

        recreate()
    }
}
