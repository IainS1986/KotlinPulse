package com.stanford.kotlinpulse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.stanford.kotlinpulse.Camera.CameraEngine
import com.stanford.kotlinpulse.Camera.PermissionsHelper
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {

    private var _cameraEngine : CameraEngine? = null

    private lateinit var _lineSeries : LineGraphSeries<DataPoint>

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

        // Test OpenCV
        if (!OpenCVLoader.initDebug()) {}

        _cameraEngine = CameraEngine(this)

        val surfaceReadyCallback = object : SurfaceHolder.Callback
        {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int){ }

            override fun surfaceDestroyed(p0: SurfaceHolder?){ }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                _cameraEngine?.buildMainHandler(_lineSeries)
                _cameraEngine?.start()
            }
        }

        surfaceView.holder.addCallback(surfaceReadyCallback)

        // Graph setup TODO Move to its own class
        _lineSeries = LineGraphSeries<DataPoint>()
        graph.addSeries(_lineSeries)
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinY(0.0)
        graph.viewport.setMaxY(256.0)
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(5000.0)

        // Hide all the graph lines, axis labels ect
        graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
        graph.gridLabelRenderer.isHorizontalLabelsVisible = false
        graph.gridLabelRenderer.isVerticalLabelsVisible = false
    }

    fun addPointToGraph(point: DataPoint)
    {

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
