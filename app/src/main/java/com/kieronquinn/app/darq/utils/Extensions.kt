package com.kieronquinn.app.darq.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kieronquinn.app.darq.DarqApplication
import com.kieronquinn.app.darq.providers.SharedPrefsProvider
import java.io.DataOutputStream
import java.io.IOException
import java.io.Serializable

const val KEY_FORCE_DARK = "debug.hwui.force_dark"

/*
    OEM checks
 */

val isOnePlus : Boolean
    get() = Build.MANUFACTURER == "OnePlus"

var Activity.hasCheckedRoot : Boolean
    get() {
        return (application as? DarqApplication)?.hasCheckedRoot == true
    }
    set(value) {
        (application as? DarqApplication)?.hasCheckedRoot = value
    }

var Activity.isRoot : Boolean
    get() {
        return (application as? DarqApplication)?.isRoot == true
    }
    set(value) {
        (application as? DarqApplication)?.isRoot = value
    }

var Fragment.isRoot : Boolean
    get() {
        return (activity?.application as? DarqApplication)?.isRoot == true
    }
    set(value) {
        (activity?.application as? DarqApplication)?.isRoot = value
    }

var Activity.uid : String?
    get() {
        return (application as? DarqApplication)?.uid
    }
    set(value) {
        (application as? DarqApplication)?.uid = value
    }

var Fragment.uid : String?
    get() {
        return (activity?.application as? DarqApplication)?.uid
    }
    set(value) {
        (activity?.application as? DarqApplication)?.uid = value
    }

fun getIsForceDarkTheme() : Boolean {
    return getSystemProperty(KEY_FORCE_DARK) == "true"
}

fun runRootCommand(command: String) {
    try {
        val p = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(p.outputStream)
        os.writeBytes(command + "\n")
        os.writeBytes("exit\n")
        os.flush()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun isDarkTheme(activity: Activity): Boolean {
    return activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun ContextWrapper.sendBroadcast(broadcast: String, vararg extras: Pair<String, Serializable>) {
    val intent = Intent(broadcast)
    intent.`package` = packageName
    for(extra in extras){
        intent.putExtra(extra.first, extra.second)
    }
    sendBroadcast(intent)
}

/**
 * Returns the app's UID
 */
fun getUID(): Int {
    return android.os.Process.myUid()
}

/**
 * Based on [com.android.settingslib.accessibility.AccessibilityUtils.getEnabledServicesFromSettings]
 * @see [AccessibilityUtils](https://github.com/android/platform_frameworks_base/blob/d48e0d44f6676de6fd54fd8a017332edd6a9f096/packages/SettingsLib/src/com/android/settingslib/accessibility/AccessibilityUtils.java.L55)
 */
fun isAccessibilityServiceEnabled(context: Context, accessibilityService: Class<*>): Boolean {
    val expectedComponentName = ComponentName(context, accessibilityService)

    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
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
@SuppressLint("ResourceAsColor")
@ColorInt
fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    this.theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}

//Following methods based off https://code.highspec.ru/Mikanoshi/CustoMIUIzer
fun stringPrefToUri(name: String, defValue: String): Uri {
    return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/string/" + name + "/" + defValue)
}

fun boolPrefToUri(name: String, defValue: Boolean): Uri {
    return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/boolean/" + name + "/" + if (defValue) '1' else '0')
}

fun getSharedStringPref(context: Context, name: String, defValue: String): String? {
    val uri: Uri = stringPrefToUri(name, defValue)
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    return if (cursor != null) {
        cursor.moveToFirst()
        val prefValue: String = cursor.getString(0)
        cursor.close()
        prefValue
    } else null
}

fun getSharedBoolPref(context: Context, name: String, defValue: Boolean): Boolean {
    val uri: Uri = boolPrefToUri(name, defValue)
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    return if (cursor != null) {
        cursor.moveToFirst()
        val prefValue: Int = cursor.getInt(0)
        cursor.close()
        prefValue == 1
    } else defValue
}
