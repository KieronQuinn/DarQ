package com.kieronquinn.app.darq.service.autodark

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.model.location.LatLng
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.ui.activities.DarqActivity
import com.kieronquinn.app.darq.utils.TimeZoneUtils
import com.kieronquinn.app.darq.work.DarqSunriseSunsetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.shredzone.commons.suncalc.SunTimes
import java.util.*
import java.util.concurrent.TimeUnit

class DarqAutoDarkForegroundService: LifecycleService() {

    companion object {
        private const val NOTIFICATION_CHANNEL_AUTO_DARK = "channel_auto_dark"
        private const val NOTIFICATION_ID_AUTO_DARK = 1003
        const val KEY_ENABLE_DARK = "enable_dark"
        const val KEY_JUST_RESCHEDULE = "just_reschedule"
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val launchIntent by lazy {
        Intent(this, DarqActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
    }

    private val workManager by lazy {
        WorkManager.getInstance(this)
    }

    private val serviceProvider by inject<DarqServiceConnectionProvider>()
    private val settings by inject<DarqSharedPreferences>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_AUTO_DARK,
                getString(R.string.notification_channel_boot_auto_dark_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                this.description = getString(R.string.notification_channel_boot_auto_dark_description)
            })

        val justReschedule = intent?.getBooleanExtra(KEY_JUST_RESCHEDULE, false) ?: false
        val notificationContent = if(justReschedule){
            R.string.auto_dark_foreground_notification_content_changes
        }else{
            R.string.auto_dark_foreground_notification_content
        }

        val notification: Notification =
            Notification.Builder(this, NOTIFICATION_CHANNEL_AUTO_DARK)
                .setContentTitle(getText(R.string.auto_dark_foreground_notification_title))
                .setContentText(getText(notificationContent))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(launchIntent)
                .setTicker(getText(notificationContent))
                .build()

        startForeground(NOTIFICATION_ID_AUTO_DARK, notification)

        lifecycleScope.launchWhenCreated {
            if(!justReschedule) {
                val enableDark = intent?.getBooleanExtra(KEY_ENABLE_DARK, false) ?: false
                setDarkModeEnabled(enableDark)
            }
            val nextTriggerTimes = getNextTriggerTimes()
            if(nextTriggerTimes != null) {
                cancelAndScheduleWork(nextTriggerTimes)
            }
            stopForeground(true)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun setDarkModeEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val service = serviceProvider.getService()
        if(service is DarqServiceConnectionProvider.ServiceResult.Success){
            service.service.setNightMode(enabled)
        }else{
            showFailedNotification()
        }
    }

    private suspend fun getNextTriggerTimes(): SunTimes? = withContext(Dispatchers.IO){
        val latLng = if(settings.useLocation){
            val service = serviceProvider.getService()
            if(service is DarqServiceConnectionProvider.ServiceResult.Success){
                service.service.location ?: getTimezoneLocation() ?: return@withContext null
            }else getTimezoneLocation() ?: return@withContext null
        }else getTimezoneLocation() ?: return@withContext null
        SunTimes.compute().on(Calendar.getInstance()).at(latLng.latitude, latLng.longitude).execute()
    }

    /**
     *  Gets the user's country location using their timezone. This is nowhere near accurate for
     *  location use, but is good *enough* for sunrise / sunset
     */
    private fun getTimezoneLocation(): LatLng? {
        return TimeZoneUtils.getLatLngForTimezone(this, TimeZone.getDefault())
    }

    /**
     *  Schedules the sunrise/sunset work for a given set of SunTimes
     *  This gets called every sunrise/sunset, somewhat counterintuitively, but that way it
     *  will stay at least semi-up-to-date for location changes (ie. only one sunrise/sunset would
     *  be out if the user changes timezones)
     */
    private fun cancelAndScheduleWork(sunTimes: SunTimes){
        workManager.cancelAllWorkByTag(DarqSunriseSunsetWork.TAG_SUNSET)
        workManager.cancelAllWorkByTag(DarqSunriseSunsetWork.TAG_SUNRISE)
        if(settings.autoDarkTheme) {
            val now = System.currentTimeMillis()
            val sunriseTime = sunTimes.rise?.time
            val sunsetTime = sunTimes.set?.time
            if (sunriseTime != null) {
                val sunriseDelay = sunriseTime - now
                val sunriseWork = OneTimeWorkRequestBuilder<DarqSunriseSunsetWork>()
                    .setInitialDelay(sunriseDelay, TimeUnit.MILLISECONDS)
                    .addTag(DarqSunriseSunsetWork.TAG_SUNRISE).build()
                workManager.enqueue(sunriseWork)
            }
            if (sunsetTime != null) {
                val sunsetDelay = sunsetTime - now
                val sunsetWork = OneTimeWorkRequestBuilder<DarqSunriseSunsetWork>()
                    .setInitialDelay(sunsetDelay, TimeUnit.MILLISECONDS)
                    .addTag(DarqSunriseSunsetWork.TAG_SUNSET).build()
                workManager.enqueue(sunsetWork)
            }
        }
    }

    private fun showFailedNotification() {
        val notification: Notification =
            Notification.Builder(this, NOTIFICATION_CHANNEL_AUTO_DARK)
                .setContentTitle(getText(R.string.auto_dark_foreground_notification_failed_title))
                .setContentText(getText(R.string.auto_dark_foreground_notification_failed_content))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(launchIntent)
                .setAutoCancel(true)
                .setStyle(Notification.BigTextStyle().bigText(getString(R.string.auto_dark_foreground_notification_failed_content)))
                .setTicker(getText(R.string.auto_dark_foreground_notification_failed_content))
                .build()
        notificationManager.notify(NOTIFICATION_ID_AUTO_DARK, notification)
    }

}