package com.kieronquinn.app.darq.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.darq.services.DarqBackgroundService

class SunriseSunsetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SunriseSunsetReceiver", "Received")
        intent?.getStringExtra(DarqBackgroundService.EXTRA_ACTION)?.let {
            Log.d("SunriseSunsetReceiver", "Got $it")
            val broacastIntent = Intent(it)
            broacastIntent.`package` = context?.packageName
            context?.sendBroadcast(broacastIntent)
            Log.d("SunriseSunsetReceiver", "Broadcast $it sent")
        }
    }
}