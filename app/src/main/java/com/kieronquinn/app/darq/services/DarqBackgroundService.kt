package com.kieronquinn.app.darq.services

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.kieronquinn.app.darq.BuildConfig
import com.kieronquinn.app.darq.DarqApplication
import com.kieronquinn.app.darq.IDarqIPC
import com.kieronquinn.app.darq.receivers.SunriseSunsetReceiver
import com.kieronquinn.app.darq.root.DarqIPCReceiver
import com.kieronquinn.app.darq.utils.PreferenceUtils
import com.kieronquinn.app.darq.utils.TimeZoneUtils
import com.kieronquinn.app.darq.utils.runRootCommand
import com.kieronquinn.app.darq.utils.sendBroadcast
import eu.chainfire.librootjava.RootIPCReceiver
import eu.chainfire.libsuperuser.Shell
import kotlinx.coroutines.*
import org.shredzone.commons.suncalc.SunTimes
import java.util.*


class DarqBackgroundService : AccessibilityService() {

    private val job = Job()

    private val reloadScope = CoroutineScope(Dispatchers.Main + job)

    companion object {
        const val BROADCAST_PING = "${BuildConfig.APPLICATION_ID}.DARQ_PING"
        const val BROADCAST_PONG = "${BuildConfig.APPLICATION_ID}.DARQ_PONG"
        const val BROADCAST_SET_DARK = "${BuildConfig.APPLICATION_ID}.SET_DARK"
        const val BROADCAST_TEST = "${BuildConfig.APPLICATION_ID}.TEST"
        const val BROADCAST_SET_FORCE_DARK = "${BuildConfig.APPLICATION_ID}.SET_FORCE_DARK"
        const val BROADCAST_SET_FORCE_DARK_SUCCESS = "${BuildConfig.APPLICATION_ID}.SET_FORCE_DARK_SUCCESS"
        const val BROADCAST_SUNRISE = "${BuildConfig.APPLICATION_ID}.SUNRISE"
        const val BROADCAST_SUNSET = "${BuildConfig.APPLICATION_ID}.SUNSET"
        const val REFRESH = "${BuildConfig.APPLICATION_ID}.REFRESH"
        const val REFRESH_TOGGLES = "${BuildConfig.APPLICATION_ID}.REFRESH_TOGGLES"
        const val REFRESH_COMPLETE = "${BuildConfig.APPLICATION_ID}.REFRESH_COMPLETE"
        const val EXTRA_FORCE_STOP = "${BuildConfig.APPLICATION_ID}.EXTRA_FORCE_STOP"
        const val EXTRA_ACTION = "${BuildConfig.APPLICATION_ID}.EXTRA_ACTION"
        const val KEY_ENABLED = "enabled"
        const val KEY_IPC_FOUND = "ipc_found"
    }

    private val enabledApps = arrayListOf<String>()

    private var isGlobalForceDarkEnabled = false

    private val sunriseIntent: Intent
        get() {
            val intent = Intent(this, SunriseSunsetReceiver::class.java)

            intent.`package` = packageName
            intent.putExtra(EXTRA_ACTION, BROADCAST_SUNRISE)
            return intent
        }

    private val sunrisePendingIntent: PendingIntent
        get() = PendingIntent.getBroadcast(this, 1, sunriseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    private val sunsetIntent: Intent
        get() {
            val intent = Intent(this, SunriseSunsetReceiver::class.java)
            intent.`package` = packageName
            intent.putExtra(EXTRA_ACTION, BROADCAST_SUNSET)
            return intent
        }

    private val sunsetPendingIntent: PendingIntent
        get() = PendingIntent.getBroadcast(this, 2, sunsetIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    private val isAutoDarkEnabled: Boolean
        get() {
            val preferenceUtils = PreferenceUtils(this@DarqBackgroundService)
            return preferenceUtils.isAutoDarkEnabled
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive ${intent?.action}")
            when (intent?.action) {
                BROADCAST_PING -> {
                    sendBroadcast(BROADCAST_PONG, Pair(KEY_IPC_FOUND, ipc != null))
                }
                BROADCAST_SET_DARK -> setDark(intent.getBooleanExtra(KEY_ENABLED, false))
                BROADCAST_SET_FORCE_DARK -> setForceDark(intent.getBooleanExtra(KEY_ENABLED, false))
                REFRESH -> refreshEnabledApps(intent.getStringExtra(EXTRA_FORCE_STOP))
                BROADCAST_TEST -> ipc?.hookService()
                BROADCAST_SUNRISE -> {
                    if(isAutoDarkEnabled) {
                        setDark(false)
                    }
                    getAlarmTimes()
                }
                BROADCAST_SUNSET -> {
                    if(isAutoDarkEnabled) {
                        setDark(true)
                    }
                    getAlarmTimes()
                }
            }
        }
    }

    private fun setForceDark(enabled: Boolean) {
        if (enabled) enableForceDark()
        else disableForceDark()
        refreshToggles()
        sendBroadcast(BROADCAST_SET_FORCE_DARK_SUCCESS)
    }

    private fun setDark(enabled: Boolean) {
        Log.d(TAG, "Set dark $enabled")
        ipc?.setDarkEnabled(enabled)
    }

    private var currentPackageName = ""
    private var isForceDarkTheme: Boolean
        get() {
            return ipc?.isForceDarkEnabled ?: false
        }
        set(value) {
            ipc?.isForceDarkEnabled = value
        }

    private val ignoredPackages = arrayOf("com.android.systemui", "com.google.android.apps.nexuslauncher")

    private val TAG = javaClass.simpleName

    private var ipc: IDarqIPC? = null

    private val darqIPCReceiver: RootIPCReceiver<IDarqIPC> = object : RootIPCReceiver<IDarqIPC>(null, 0) {
        override fun onConnect(ipc: IDarqIPC?) {
            Log.d(TAG, "Connected")
            this@DarqBackgroundService.ipc = ipc
            (application as? DarqApplication)?.isRoot = ipc?.isRoot ?: false
            (application as? DarqApplication)?.uid = ipc?.uid
        }

        override fun onDisconnect(ipc: IDarqIPC?) {
            Log.d(TAG, "Disconnected")
            this@DarqBackgroundService.ipc = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        checkRootAndSecure()
        registerReceiver(receiver, IntentFilter(BROADCAST_PING))
        registerReceiver(receiver, IntentFilter(BROADCAST_SET_DARK))
        registerReceiver(receiver, IntentFilter(BROADCAST_SET_FORCE_DARK))
        registerReceiver(receiver, IntentFilter(REFRESH))
        registerReceiver(receiver, IntentFilter(BROADCAST_TEST))
        registerReceiver(receiver, IntentFilter(BROADCAST_SUNRISE))
        registerReceiver(receiver, IntentFilter(BROADCAST_SUNSET))
        refreshEnabledApps("")
        refreshToggles()
        getAlarmTimes()
    }

    private fun getAlarmTimes() {
        reloadScope.launch {
            var times: SunTimes? = null
            withContext(Dispatchers.Default) {
                val latLng =
                    if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val criteria = Criteria()
                        val provider = locationManager.getBestProvider(criteria, true)!!
                        val location = locationManager.getLastKnownLocation(provider)
                        if (location != null) Pair(location.latitude, location.longitude)
                        else getTimeZoneLatLng()
                    } else getTimeZoneLatLng()
                latLng?.let {
                    times = SunTimes.compute().on(Calendar.getInstance()).at(it.first, it.second).execute()
                }
            }
            times?.let {
                registerAlarmTimes(it)
            }
        }

    }

    private fun registerAlarmTimes(times: SunTimes) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sunrisePendingIntent)
        alarmManager.cancel(sunsetPendingIntent)

        times.rise?.let {
            Log.d(TAG, "Registering sunrise time $it")
            alarmManager.set(AlarmManager.RTC, it.time, sunrisePendingIntent)
        }

        times.set?.let {
            Log.d(TAG, "Registering sunset time ${it.time}")
            alarmManager.set(AlarmManager.RTC, it.time, sunsetPendingIntent)
        }
    }

    private fun getTimeZoneLatLng(): Pair<Double, Double>? {
        return TimeZoneUtils.getLatLngForTimezone(this, TimeZone.getDefault())
    }

    private fun checkRootAndSecure() {
        val isRooted: Boolean = Shell.SU.available()
        if (isRooted) {
            val rootCommand = DarqIPCReceiver.getLaunchScript(this)
            runRootCommand(rootCommand)
            darqIPCReceiver.setContext(this)
        } else {
            darqIPCReceiver.setContext(this)
            //TODO no root screen
        }
    }

    override fun onDestroy() {
        darqIPCReceiver.release()
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (isGlobalForceDarkEnabled) return
        val packageName = event.packageName.toString()
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && packageName != currentPackageName && !ignoredPackages.contains(
                packageName
            )
        ) {
            Log.d(TAG, "Package $packageName")
            if (enabledApps.contains(packageName)) enableForceDark()
            else disableForceDark()
            currentPackageName = packageName

        }
    }

    private fun disableForceDark() {
        Log.d(TAG, "Disabling force dark pre")
        if (!isForceDarkTheme) return
        Log.d(TAG, "Disabling force dark")
        ipc?.setForceDarkEnabled(false)
        ipc?.pokeServices()
        isForceDarkTheme = false
    }

    private fun enableForceDark() {
        Log.d(TAG, "Enabling force dark pre")
        if (isForceDarkTheme) return
        Log.d(TAG, "Enabling force dark ipc ${ipc != null}")
        ipc?.setForceDarkEnabled(true)
        ipc?.pokeServices()
        isForceDarkTheme = true
    }

    private fun refreshEnabledApps(forceStopApp: String?) {
        //job.cancel()

        Log.d(TAG, "Refresh start")
        reloadScope.launch {
            val preferenceUtils = PreferenceUtils(this@DarqBackgroundService)
            preferenceUtils.getEnabledApps(this@DarqBackgroundService)
            enabledApps.clear()
            enabledApps.addAll(preferenceUtils.enabledApps)
            Log.d(TAG, "Refresh done ${enabledApps.size}")
            if (forceStopApp != null) {
                ipc?.forceStopApp(forceStopApp)
            }
        }

        Log.d(TAG, "Refresh send broadcast")
        sendBroadcast(REFRESH_COMPLETE)
    }

    private fun refreshToggles() {
        val preferenceUtils = PreferenceUtils(this)
        isGlobalForceDarkEnabled = preferenceUtils.isGlobalForceDark
    }


    override fun onInterrupt() {
    }
}
