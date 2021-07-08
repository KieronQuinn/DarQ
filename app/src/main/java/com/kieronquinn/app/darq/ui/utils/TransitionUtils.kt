package com.kieronquinn.app.darq.ui.utils

import android.content.Context
import android.view.animation.AnimationUtils
import androidx.core.transition.doOnEnd
import com.google.android.material.transition.platform.FadeThroughProvider
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.google.android.material.transition.platform.SlideDistanceProvider
import com.kieronquinn.app.darq.R
import kotlin.math.roundToInt

object TransitionUtils {

    fun getMaterialSharedAxis(context: Context, forward: Boolean): MaterialSharedAxis {
        return MaterialSharedAxis(MaterialSharedAxis.X, forward).apply {
            (primaryAnimatorProvider as SlideDistanceProvider).slideDistance =
                context.resources.getDimension(R.dimen.shared_axis_x_slide_distance).roundToInt()
            duration = 450L
            (secondaryAnimatorProvider as FadeThroughProvider).progressThreshold = 0.22f
            interpolator = AnimationUtils.loadInterpolator(context, R.anim.fast_out_extra_slow_in)
        }
    }

}