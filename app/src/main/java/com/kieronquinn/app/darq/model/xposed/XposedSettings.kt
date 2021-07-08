package com.kieronquinn.app.darq.model.xposed

data class XposedSettings(
    val enabled: Boolean,
    val aggressiveDark: Boolean? = null,
    val invertStatus: Boolean? = null
){
    override fun toString(): String {
        return "XposedSettings enabled $enabled aggressiveDark $aggressiveDark $invertStatus"
    }
}
