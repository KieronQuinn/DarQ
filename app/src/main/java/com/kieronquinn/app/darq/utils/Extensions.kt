package com.kieronquinn.app.darq.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiModeManager
import android.content.*
import android.content.res.Configuration
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import com.kieronquinn.app.darq.DarqApplication
import android.text.TextUtils
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

const val KEY_FORCE_DARK = "persist.hwui.force_dark"

var Activity.hasCheckedRoot : Boolean
    get() {
        return (application as? DarqApplication)?.hasCheckedRoot == true
    }
    set(value) {
        (application as? DarqApplication)?.hasCheckedRoot = value
    }

fun getIsDarkTheme(uiModeManager: UiModeManager?): Boolean {
    return uiModeManager?.currentModeType == UiModeManager.MODE_NIGHT_YES
}

fun getIsForceDarkTheme() : Boolean {
    return getSystemProperty(KEY_FORCE_DARK) == "true"
}

fun View.fadeOut(){
    AlphaAnimation(1f, 0f).run {
        duration = 1000L
        interpolator = AccelerateInterpolator()
        fillAfter = true
        this@fadeOut.startAnimation(this)
    }
}

fun isDarkTheme(activity: Activity): Boolean {
    return activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun ContextWrapper.sendBroadcast(broadcast: String, vararg extras: Pair<String, String>) {
    val intent = Intent(broadcast)
    intent.`package` = packageName
    for(extra in extras){
        intent.putExtra(extra.first, extra.second)
    }
    sendBroadcast(intent)
}

/**
 * Based on [com.android.settingslib.accessibility.AccessibilityUtils.getEnabledServicesFromSettings]
 * @see [AccessibilityUtils](https://github.com/android/platform_frameworks_base/blob/d48e0d44f6676de6fd54fd8a017332edd6a9f096/packages/SettingsLib/src/com/android/settingslib/accessibility/AccessibilityUtils.java.L55)
 */
fun isAccessibilityServiceEnabled(context: Context, accessibilityService: Class<*>): Boolean {
    val expectedComponentName = ComponentName(context, accessibilityService)

    val enabledServicesSetting = Settings.Secure.getString(
        context.getContentResolver(),
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
        ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)

    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledService = ComponentName.unflattenFromString(componentNameString)

        if (enabledService != null && enabledService == expectedComponentName)
            return true
    }

    return false
}

/*
    This works for now but might not in the future. If that happens we'll have to switch to reading the build.prop manually or using the root service
 */
@SuppressLint("PrivateApi")
fun getSystemProperty(key: String): String? {
    var value: String? = null
    try {
        value = Class.forName("android.os.SystemProperties").getMethod("get", String::class.java).invoke(null, key) as String
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return value
}

// From https://stackoverflow.com/a/55280832
@ColorInt
fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    this.theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}