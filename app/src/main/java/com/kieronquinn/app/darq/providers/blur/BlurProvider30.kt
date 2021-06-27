package com.kieronquinn.app.darq.providers.blur

import android.content.res.Resources
import android.view.SurfaceControl
import android.view.View
import android.view.ViewRootImpl
import android.view.Window
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.utils.SystemProperties

class BlurProvider30(resources: Resources): BlurProvider() {

    private val blurDisabledSysProp by lazy {
        SystemProperties.getBoolean("persist.sys.sf.disable_blurs", false)
    }

    private val getViewRootImpl by lazy {
        try {
            View::class.java.getMethod("getViewRootImpl")
        }catch (e: NoSuchMethodException){
            null
        }
    }

    private val setBackgroundBlurRadius by lazy {
        try {
            SurfaceControl.Transaction::class.java.getMethod("setBackgroundBlurRadius", SurfaceControl::class.java, Integer.TYPE)
        }catch (e: NoSuchMethodException) {
            null
        }
    }

    override val maxBlurRadius by lazy {
        resources.getDimension(R.dimen.max_window_blur_radius)
    }

    override val minBlurRadius by lazy {
        resources.getDimension(R.dimen.min_window_blur_radius)
    }

    override fun applyBlur(dialogWindow: Window, appWindow: Window, ratio: Float) {
        if(blurDisabledSysProp){
            return
        }
        val radius = blurRadiusOfRatio(ratio)
        runCatching {
            dialogWindow.addDimming()
            val viewRootImpl = getViewRootImpl!!.invoke(dialogWindow.decorView) as ViewRootImpl
            val surfaceControl = viewRootImpl.surfaceControl
            val transaction = SurfaceControl.Transaction()
            val setBackgroundBlurRadiusResult = setBackgroundBlurRadius!!.invoke(transaction, surfaceControl, radius) as SurfaceControl.Transaction
            setBackgroundBlurRadiusResult.apply()
        }
    }

}