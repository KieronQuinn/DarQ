package com.kieronquinn.app.darq.utils.extensions

import android.graphics.Color
import kotlin.math.roundToInt

fun getColorWithAlpha(color: Int, ratio: Float): Int {
    return Color.argb(
        (Color.alpha(color) * ratio).roundToInt(),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )
}