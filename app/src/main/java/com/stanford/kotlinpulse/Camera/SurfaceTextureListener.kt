package com.stanford.kotlinpulse.Camera

import android.graphics.SurfaceTexture
import android.view.TextureView

class SurfaceTextureListener : TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) { }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) { }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean { return false }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) { }
}