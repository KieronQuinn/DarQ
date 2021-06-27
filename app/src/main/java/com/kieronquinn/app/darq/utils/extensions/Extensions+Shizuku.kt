package com.kieronquinn.app.darq.utils.extensions

import android.content.Context
import com.kieronquinn.app.darq.model.shizuku.ShizukuConstants
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume

fun Context.isShizukuInstalled(): Boolean {
    return packageManager.isAppInstalled(ShizukuConstants.SHIZUKU_PACKAGE_NAME)
}

suspend fun Shizuku_awaitBinderReceived() = suspendCancellableCoroutine<Unit> {
    val receiver = object: Shizuku.OnBinderReceivedListener {
        override fun onBinderReceived() {
            Shizuku.removeBinderReceivedListener(this)
            it.resume(Unit)
        }
    }
    Shizuku.addBinderReceivedListener(receiver)
    it.invokeOnCancellation {
        Shizuku.removeBinderReceivedListener(receiver)
    }
}