package com.kieronquinn.app.darq.utils.extensions

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.monetcompat.core.MonetCompat

fun RecyclerView.applyMonetToFastScroller() {
    val monet = MonetCompat.getInstance()
    val fastScrollerClass = Class.forName("androidx.recyclerview.widget.FastScroller")
    for (i in 0 until itemDecorationCount) {
        val decoration = getItemDecorationAt(i)
        if (decoration::class.java == fastScrollerClass) {
            val mVerticalThumbDrawable =
                fastScrollerClass.getDeclaredField("mVerticalThumbDrawable").apply {
                    isAccessible = true
                }.get(decoration) as StateListDrawable
            val mVerticalTrackDrawable =
                fastScrollerClass.getDeclaredField("mVerticalTrackDrawable").apply {
                    isAccessible = true
                }.get(decoration) as Drawable
            mVerticalThumbDrawable.setTintList(ColorStateList.valueOf(monet.getAccentColor(context)))
            mVerticalTrackDrawable.setTintList(
                ColorStateList.valueOf(
                    monet.getBackgroundColorSecondary(context) ?: monet.getBackgroundColor(context)
                )
            )
        }
    }
}