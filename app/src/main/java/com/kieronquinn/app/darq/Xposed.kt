package com.kieronquinn.app.darq

import android.util.Log
import android.view.View
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Xposed : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(View::class.java, "setForceDarkAllowed", Boolean::class.java, object : XC_MethodReplacement() {
            override fun replaceHookedMethod(param: MethodHookParam?): Any? {
                Log.d("DarQXposed", "Preventing the use of setForceDarkAllowed for ${lpparam.packageName}")
                return null
            }
        })
    }
}
