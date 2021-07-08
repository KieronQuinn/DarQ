package com.kieronquinn.app.darq

import android.app.Activity
import android.app.AndroidAppHelper
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View
import com.kieronquinn.app.darq.components.settings.XposedSharedPreferences
import com.kieronquinn.app.darq.model.xposed.XposedSettings
import com.kieronquinn.app.darq.utils.extensions.isDarkTheme
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage


class Xposed : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "DarQXposed"
        private const val SHARED_PREFS_FILENAME = "${BuildConfig.APPLICATION_ID}_prefs"
    }

    private var xposedSettings: XposedSettings? = null

    private val context by lazy {
        AndroidAppHelper.currentApplication() as Context
    }
    
    private val isDarkMode: Boolean
        get() = Resources.getSystem().configuration.isDarkTheme

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if(lpparam.packageName == BuildConfig.APPLICATION_ID){
            setupSelfHooks(lpparam.classLoader)
        }
        XposedHelpers.findAndHookMethod(View::class.java, "setForceDarkAllowed", Boolean::class.java, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                if(xposedSettings == null) {
                    xposedSettings = getXposedSettings(context)
                }
                if(xposedSettings?.enabled == true){
                    param.args[0] = true
                }
            }
        })
        XposedHelpers.findAndHookMethod(Activity::class.java, "onResume", object: XC_MethodHook(){
            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
                if(xposedSettings == null) {
                    xposedSettings = getXposedSettings(context)
                }
                if(xposedSettings?.enabled == true && xposedSettings?.invertStatus == true){
                    val activity = param.thisObject as? Activity ?: return
                    if(isDarkMode) {
                        activity.window.decorView.run {
                            post {
                                systemUiVisibility =
                                    systemUiVisibility.and(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
                            }
                        }
                    }
                }
            }
        })
        XposedHelpers.findAndHookMethod("android.graphics.HardwareRenderer", lpparam.classLoader, "setForceDark", Boolean::class.java, object: XC_MethodHook(){
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                if(xposedSettings?.enabled == true && xposedSettings?.aggressiveDark == true) {
                    Log.i(TAG, "Overriding setForceDark to $isDarkMode for ${lpparam.packageName}")
                    param.args[0] = isDarkMode
                }
            }
        })
    }

    private fun setupSelfHooks(classLoader: ClassLoader){
        XposedHelpers.findAndHookMethod("com.kieronquinn.app.darq.model.xposed.XposedSelfHooks", classLoader, "isXposedModuleEnabled", object: XC_MethodReplacement(){
            override fun replaceHookedMethod(param: MethodHookParam): Any {
                param.result = true
                return true
            }
        })
        XposedHelpers.findAndHookMethod("com.kieronquinn.app.darq.model.xposed.XposedSelfHooks", classLoader, "getXSharedPrefsPath", object: XC_MethodReplacement(){
            override fun replaceHookedMethod(param: MethodHookParam): Any {
                val path = XSharedPreferences(BuildConfig.APPLICATION_ID, SHARED_PREFS_FILENAME).file.absolutePath
                param.result = path
                return path
            }
        })
    }

    private fun getXposedSettings(context: Context): XposedSettings? {
        return try {
            val xposedPreferences = XposedSharedPreferences(SHARED_PREFS_FILENAME)
            val darqEnabled = xposedPreferences.enabled
            val appSelected = xposedPreferences.enabledApps.contains(context.packageName)
            val alwaysUseForceDark = xposedPreferences.alwaysForceDark
            Log.d(TAG, "Enabled apps ${xposedPreferences.enabledApps.joinToString(", ")}")
            return if(!darqEnabled || (!appSelected && !alwaysUseForceDark)){
                XposedSettings(enabled = false).apply {
                    Log.d(TAG, "Got XposedSettings disabled for ${context.packageName}")
                }
            }else{
                XposedSettings(enabled = true, aggressiveDark = xposedPreferences.xposedAggressiveDark, invertStatus = xposedPreferences.xposedInvertStatus).apply {
                    Log.d(TAG, "Got XposedSettings enabled for ${context.packageName}")
                }
            }
        }catch (e: Exception){
            //Don't crash the app
            if(BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to get XposedSettings for ${context.packageName}", e)
            }
            null
        }
    }

}
