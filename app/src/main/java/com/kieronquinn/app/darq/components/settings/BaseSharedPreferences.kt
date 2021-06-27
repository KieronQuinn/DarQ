package com.kieronquinn.app.darq.components.settings

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty

abstract class BaseSharedPreferences {

    abstract fun shared(key: String, default: String): ReadWriteProperty<Any?, String>
    abstract fun shared(key: String, default: Int): ReadWriteProperty<Any?, Int>
    abstract fun shared(key: String, default: Boolean): ReadWriteProperty<Any?, Boolean>
    abstract fun shared(key: String, default: Double): ReadWriteProperty<Any?, Double>
    abstract fun shared(key: String, default: Long): ReadWriteProperty<Any?, Long>
    abstract fun shared(key: String, default: Float): ReadWriteProperty<Any?, Float>
    abstract fun sharedJSONArray(key: String): ReadWriteProperty<Any?, Array<String>>

    inline fun <reified T : Enum<T>> shared(key: String, default: Enum<T>): ReadWriteProperty<Any?, T> {
        return when(this){
            is AppSharedPreferences -> sharedEnum(key, default)
            else -> throw NotImplementedError("Unknown shared prefs type")
        }
    }

    abstract val sharedPreferences: SharedPreferences

}