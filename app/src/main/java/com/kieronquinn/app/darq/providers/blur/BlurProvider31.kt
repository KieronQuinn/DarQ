package com.kieronquinn.app.darq.providers.blur

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.RenderEffect
import android.graphics.Shader
import android.view.Window
import com.kieronquinn.app.darq.R

class BlurProvider31(resources: Resources): BlurProvider() {

    override val minBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.min_window_blur_radius).toFloat()
    }

    override val maxBlurRadius by lazy {
        resources.getDimensionPixelSize(R.dimen.max_window_blur_radius).toFloat()
    }

    @SuppressLint("NewApi")
    override fun applyBlur(dialogWindow: Window, appWindow: Window, ratio: Float) {
        val radius = blurRadiusOfRatio(ratio)
        if(radius == 0){
            appWindow.decorView.setRenderEffect(null)
        }else {
            val renderEffect = RenderEffect.createBlurEffect(radius.toFloat(), radius.toFloat(), Shader.TileMode.MIRROR)
            appWindow.decorView.setRenderEffect(renderEffect)
        }
    }

}