package com.kieronquinn.app.darq.utils.extensions

import android.content.pm.PackageManager

fun PackageManager.isAppInstalled(packageName: String): Boolean {
    return try {
        getApplicationInfo(packageName, 0) != null
    }catch (e: PackageManager.NameNotFoundException) {
        false
    }
}