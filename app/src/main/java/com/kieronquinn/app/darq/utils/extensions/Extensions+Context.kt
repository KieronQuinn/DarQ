package com.kieronquinn.app.darq.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

val Context.isDarkTheme: Boolean
    get() = resources.configuration.isDarkTheme

val Configuration.isDarkTheme: Boolean
    get() {
        return uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

val Context.isLandscape: Boolean
    get() {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

// From https://stackoverflow.com/a/55280832
@SuppressLint("ResourceAsColor")
@ColorInt
fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    this.theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}