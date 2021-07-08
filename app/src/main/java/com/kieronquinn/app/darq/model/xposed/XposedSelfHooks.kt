package com.kieronquinn.app.darq.model.xposed

object XposedSelfHooks {

    fun isXposedModuleEnabled(): Boolean {
        return false
    }

    fun getXSharedPrefsPath(): String {
        return ""
    }

}