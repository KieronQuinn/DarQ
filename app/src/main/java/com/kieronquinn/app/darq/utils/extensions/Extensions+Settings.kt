package com.kieronquinn.app.darq.utils.extensions

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun Context.secureSettingIntFlow(setting: String, default: Int) = callbackFlow {
    val uri = Settings.Secure.getUriFor(setting)
    val onChange = {
        this.trySend(Settings.Secure.getInt(contentResolver, setting, default))
    }
    val contentObserver = object: ContentObserver(Handler(Looper.myLooper()!!)) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            onChange.invoke()
        }
    }
    contentResolver.registerContentObserver(uri, true, contentObserver)
    onChange.invoke()
    awaitClose {
        contentResolver.unregisterContentObserver(contentObserver)
    }
}