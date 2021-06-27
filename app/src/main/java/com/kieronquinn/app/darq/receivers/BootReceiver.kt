package com.kieronquinn.app.darq.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.darq.service.boot.BootForegroundService

class BootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null || intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context.startForegroundService(Intent(context, BootForegroundService::class.java))
    }

}