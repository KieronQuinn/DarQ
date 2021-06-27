package com.kieronquinn.app.darq.service.boot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.model.shizuku.ShizukuConstants
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.ui.activities.DarqActivity
import org.koin.android.ext.android.inject

class BootForegroundService : LifecycleService() {

    companion object {
        private const val NOTIFICATION_CHANNEL_BOOT_STARTUP = "channel_startup"
        private const val NOTIFICATION_ID_STARTUP = 1001
        private const val NOTIFICATION_ID_STARTUP_FAILED = 1002
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val connectionProvider by inject<DarqServiceConnectionProvider>()

    private val launchIntent by lazy {
        Intent(this, DarqActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
    }

    private val shizukuLaunchIntent by lazy {
        Intent(packageManager.getLaunchIntentForPackage(ShizukuConstants.SHIZUKU_PACKAGE_NAME)).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_BOOT_STARTUP,
                getString(R.string.notification_channel_boot_startup_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                this.description = getString(R.string.notification_channel_boot_startup_description)
            })

        val notification: Notification =
            Notification.Builder(this, NOTIFICATION_CHANNEL_BOOT_STARTUP)
                .setContentTitle(getText(R.string.boot_foreground_service_notification_title))
                .setContentText(getText(R.string.boot_foreground_service_notification_content))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(launchIntent)
                .setTicker(getText(R.string.boot_foreground_service_notification_content))
                .build()

        startForeground(NOTIFICATION_ID_STARTUP, notification)

        lifecycleScope.launchWhenCreated {
            val result = connectionProvider.getService()
            if (result.isFailure) {
                //Failed to start
                showFailedNotification()
            } else {
                if (result is DarqServiceConnectionProvider.ServiceResult.Failed &&
                    result.reason == DarqServiceConnectionProvider.ServiceFailureReason.SHIZUKU_NOT_STARTED) {
                    showShizukuNotification()
                }
            }
            stopForeground(true)
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showFailedNotification() {
        val notification: Notification =
            Notification.Builder(this, NOTIFICATION_CHANNEL_BOOT_STARTUP)
                .setContentTitle(getText(R.string.boot_foreground_service_notification_failed_title))
                .setContentText(getText(R.string.boot_foreground_service_notification_failed_content))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(launchIntent)
                .setAutoCancel(true)
                .setStyle(Notification.BigTextStyle().bigText(getString(R.string.boot_foreground_service_notification_failed_content)))
                .setTicker(getText(R.string.boot_foreground_service_notification_failed_content))
                .build()
        notificationManager.notify(NOTIFICATION_ID_STARTUP_FAILED, notification)
    }

    private fun showShizukuNotification() {
        val notification: Notification =
            Notification.Builder(this, NOTIFICATION_CHANNEL_BOOT_STARTUP)
                .setContentTitle(getText(R.string.boot_foreground_service_notification_shizuku_title))
                .setContentText(getText(R.string.boot_foreground_service_notification_shizuku_content))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(shizukuLaunchIntent)
                .setAutoCancel(true)
                .setStyle(Notification.BigTextStyle().bigText(getString(R.string.boot_foreground_service_notification_shizuku_content)))
                .setTicker(getText(R.string.boot_foreground_service_notification_shizuku_content))
                .build()
        notificationManager.notify(NOTIFICATION_ID_STARTUP_FAILED, notification)
    }

    private val DarqServiceConnectionProvider.ServiceResult.isFailure: Boolean
        get() {
            return when (this) {
                is DarqServiceConnectionProvider.ServiceResult.Success -> false
                is DarqServiceConnectionProvider.ServiceResult.Failed -> {
                    when (reason) {
                        DarqServiceConnectionProvider.ServiceFailureReason.TIMEOUT,
                        DarqServiceConnectionProvider.ServiceFailureReason.SHIZUKU_PERMISSION_REQUIRED,
                        DarqServiceConnectionProvider.ServiceFailureReason.SHIZUKU_NOT_INSTALLED -> true
                        DarqServiceConnectionProvider.ServiceFailureReason.SHIZUKU_NOT_STARTED -> false
                    }
                }
            }
        }

}