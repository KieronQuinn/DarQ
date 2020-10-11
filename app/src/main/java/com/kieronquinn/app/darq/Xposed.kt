package com.kieronquinn.app.darq

import android.content.Context
import android.view.View
import com.kieronquinn.app.darq.utils.PreferenceUtils
import com.kieronquinn.app.darq.utils.getSharedBoolPref
import com.kieronquinn.app.darq.utils.getSharedStringPref
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONArray


class Xposed : IXposedHookLoadPackage {

    private var enabledApps: List<String>? = null

    private fun getEnabledApps(context: Context) {
        val rawEnabledApps = getSharedStringPref(context, PreferenceUtils.KEY_ENABLED_APPS, "[]")
        val enabledAppsArray = JSONArray(rawEnabledApps)
        val enabledApps = ArrayList<String>()
        for (i in 0 until enabledAppsArray.length()) {
            enabledApps.add(enabledAppsArray.getString(i))
        }
        this.enabledApps = enabledApps
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(View::class.java, "setForceDarkAllowed", Boolean::class.java, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                (param.thisObject as View).run {
                    getEnabledApps(context)
                    val isOverrideEnabled = getSharedBoolPref(context, PreferenceUtils.KEY_GLOBAL_OVERRIDE, false)
                    if(enabledApps?.contains(lpparam.packageName) == true || isOverrideEnabled){
                        param.args[0] = true
                    }
                }
            }
        })
    }
}
