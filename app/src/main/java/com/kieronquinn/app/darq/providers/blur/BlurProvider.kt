package com.kieronquinn.app.darq.providers.blur

import android.content.res.Resources
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.os.BuildCompat

abstract class BlurProvider {

    companion object {
        private fun lerp(start: Float, stop: Float, amount: Float): Float {
            return start + (stop - start) * amount
        }

        fun getBlurProvider(resources: Resources): BlurProvider {
            return when {
                Build.VERSION.SDK_INT >= 31 || BuildCompat.isAtLeastS() -> BlurProvider31(resources)
                Build.VERSION.SDK_INT >= 30 -> BlurProvider30(resources)
                else -> BlurProvider29()
            }
        }
    }

    abstract val minBlurRadius: Float
    abstract val maxBlurRadius: Float

    abstract fun applyBlur(dialogWindow: Window, appWindow: Window, ratio: Float)

    internal fun Window.clearDimming() {
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    internal fun Window.addDimming() {
        addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    internal fun blurRadiusOfRatio(ratio: Float): Int {
        return if (ratio == 0.0f) 0 else lerp(minBlurRadius, maxBlurRadius, ratio).toInt()
    }

}