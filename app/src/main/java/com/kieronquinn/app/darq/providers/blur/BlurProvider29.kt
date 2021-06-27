package com.kieronquinn.app.darq.providers.blur

import android.view.Window

class BlurProvider29: BlurProvider() {

    override val minBlurRadius = 0f
    override val maxBlurRadius = 0f

    override fun applyBlur(dialogWindow: Window, appWindow: Window, ratio: Float) {
        dialogWindow.addDimming()
    }

}