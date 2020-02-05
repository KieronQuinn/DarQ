package com.kieronquinn.app.darq.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.kieronquinn.app.darq.BuildConfig
import org.json.JSONArray

class PreferenceUtils(context: Context) {

    companion object {
        const val SHARED_PREFS_NAME = "${BuildConfig.APPLICATION_ID}_preferences"
        const val KEY_ENABLED_APPS = "enabled_apps"
    }

    var enabledApps: ArrayList<String> = ArrayList()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    val isGlobalForceDark
        get() = sharedPreferences.getBoolean("switch_force_dark_mode", false)

    val isAutoDarkEnabled
        get() = sharedPreferences.getBoolean("switch_auto_night_dark", false)

    fun getEnabledApps(context: Context) {
        val packageManager = context.packageManager
        val rawEnabledApps = sharedPreferences.getString(KEY_ENABLED_APPS, "[]")
        val enabledAppsArray = JSONArray(rawEnabledApps)
        val enabledApps = ArrayList<String>()
        for (i in 0 until enabledAppsArray.length()) {
            val packageName = enabledAppsArray.getString(i)
            try {
                packageManager.getPackageInfo(packageName, 0)
                enabledApps.add(packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                //App is no longer installed
            }
        }
        this.enabledApps = enabledApps
    }

    fun saveApps(context: Context, packageNames: List<String>) {
        val jsonArray = JSONArray()
        for(packageName in packageNames){
            jsonArray.put(packageName)
        }
        sharedPreferences.edit().putString(KEY_ENABLED_APPS, jsonArray.toString()).apply()
    }

}