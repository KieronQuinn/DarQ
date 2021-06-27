package com.kieronquinn.app.darq.work

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kieronquinn.app.darq.service.autodark.DarqAutoDarkForegroundService

class DarqSunriseSunsetWork(private val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    companion object {
        const val TAG_SUNRISE = "work_sunrise"
        const val TAG_SUNSET = "work_sunset"
    }

    private val isDarkMode = workerParams.tags.first() == TAG_SUNSET

    override fun doWork(): Result {
        //Start the foreground service
        context.startForegroundService(Intent(context, DarqAutoDarkForegroundService::class.java).apply {
            putExtra(DarqAutoDarkForegroundService.KEY_ENABLE_DARK, isDarkMode)
        })
        return Result.success()
    }

}