package com.stanford.kotlinpulse.Camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object PermissionsHelper
{

    private const val CAMERA_PERMISSION_CODE = 0
    private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    /**
     * Checks if camera permissions have been granted
     */
    fun hasCameraPermission(activity: Activity): Boolean
    {
        return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests camera permissions
     */
    fun requestCameraPermission(activity: Activity)
    {
        ActivityCompat.requestPermissions(activity, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
    }

    /**
     * Checks to see if we need to show the permission rationale
     */
    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean
    {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)
    }

    /**
     * Show permission dialog/settings
     */
    fun launchPermissionSettings(activity: Activity)
    {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }
}