package com.kieronquinn.app.darq.service.impl

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.app.IProcessObserver
import android.app.IUiModeManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.location.ILocationManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.darq.BuildConfig
import com.kieronquinn.app.darq.IDarqService
import com.kieronquinn.app.darq.model.location.LatLng
import com.kieronquinn.app.darq.model.root.RootConstants
import com.kieronquinn.app.darq.model.settings.IPCSetting
import com.kieronquinn.app.darq.model.shizuku.ShizukuConstants
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.utils.SystemProperties
import com.topjohnwu.superuser.internal.Utils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import rikka.shizuku.SystemServiceHelper
import kotlin.streams.toList
import kotlin.system.exitProcess


@SuppressLint("RestrictedApi")
class DarqService(private val serviceType: DarqServiceConnectionProvider.ServiceType = DarqServiceConnectionProvider.ServiceType.SHIZUKU) : IDarqService.Stub(), LifecycleOwner {

    companion object {
        private const val TAG = "DarQService"
        private const val FORCE_DARK_PROP = "debug.hwui.force_dark"
        private const val OXYGEN_OS_WORLD_FORCE_DARK = "op_force_dark_entire_world"
        private const val OXYGEN_OS_AOSP_FORCE_DARK = "aosp_force_dark_mode"
        private const val SHIZUKU_SERVICE_ID = "${BuildConfig.APPLICATION_ID}:${ShizukuConstants.SERVICE_NAME}"
        private const val ROOT_SERVICE_ID = "${BuildConfig.APPLICATION_ID}:${RootConstants.SERVICE_NAME}"
    }

    private var isEnabled = true
    private var isAlwaysForceDarkEnabled = false
    private var isOxygenForceDarkEnabled = false
    private val appWhitelist = emptySet<String>().toMutableSet()
    private var tempAppList = emptyList<String>().toMutableList()
    private var currentApp = MutableStateFlow<String?>(null)

    private val mDispatcher by lazy {
        ServiceLifecycleDispatcher(this)
    }

    override fun getLifecycle(): Lifecycle {
        return mDispatcher.lifecycle
    }

    //Whether to send `null` when an app is closed and no new one is opened (causes more incorrect Force Dark changes)
    private var sendAppCloses = false

    //This is the "current application" context, in this case the system context. It's only used to query PackageManager.
    private val context by lazy {
        Utils.getContext()
    }

    //This is the internal ActivityManager via SystemServiceHelper
    private val activityManager by lazy {
        val activityManagerProxy = SystemServiceHelper.getSystemService("activity")
        IActivityManager.Stub.asInterface(activityManagerProxy)
    }

    private val uiModeManager by lazy {
        val uiModeManagerProxy = SystemServiceHelper.getSystemService("uimode")
        IUiModeManager.Stub.asInterface(uiModeManagerProxy)
    }

    private val locationManager by lazy {
        val locationManagerProxy = SystemServiceHelper.getSystemService("location")
        ILocationManager.Stub.asInterface(locationManagerProxy)
    }

    //Packages to ignore foreground changes to. These include the launcher, recents provider and android itself.
    private val ignoredPackages = arrayOf("android", "com.android.systemui", getLauncherPackage(), getQuickstepPackage()).distinct().filterNotNull()

    init {
        mDispatcher.onServicePreSuperOnCreate()
        mDispatcher.onServicePreSuperOnStart()
    }

    /**
     *  Called from DarqServiceConnectionProvider when the service is bound to
     */
    override fun onBind() {
        Handler(Looper.getMainLooper()).post {
            mDispatcher.onServicePreSuperOnBind()
        }
        lifecycleScope.launch {
            currentApp.collect {
                if(it == null && !sendAppCloses) return@collect
                setForceDarkEnabled(appWhitelist.contains(it))
            }

        }
        registerProcessObserver()
    }


    /**
     *  Called for each whitelisted app when the service is started (Binder limit is 1Mb so it
     *  has to be split for users with huge app lists)
     */
    override fun setupWhitelist(isStart: Boolean, isEnd: Boolean, packageName: String) {
        if(isStart){
            tempAppList.clear()
        }
        tempAppList.add(packageName)
        if(isEnd){
            appWhitelist.clear()
            appWhitelist.addAll(tempAppList)
            tempAppList.clear()
        }
    }

    /**
     *  Setup with a given IPCSetting object. All the fields should be non-null here, except packageChange as that
     *  is handled separately.
     */
    override fun setupSettings(ipcSetting: IPCSetting) {
        if(ipcSetting.enabled != null) {
            isEnabled = ipcSetting.enabled
        }
        if(ipcSetting.alwaysForceDark != null) {
            isAlwaysForceDarkEnabled = ipcSetting.alwaysForceDark
            if(isAlwaysForceDarkEnabled){
                setForceDarkEnabled(true)
            }
        }
        if(ipcSetting.oxygenForceDark != null) {
            isOxygenForceDarkEnabled = ipcSetting.oxygenForceDark
            setOxygenOSEntireWorldEnabled(isOxygenForceDarkEnabled)
        }
        if(ipcSetting.sendAppCloses != null) sendAppCloses = ipcSetting.sendAppCloses
    }

    /**
     *  Settings change from the settings UI. This will only be called after the service is set up and the app is running
     *  Only one of the fields should be non-null
     */
    override fun notifySettingsChange(ipcSetting: IPCSetting) {
        when {
            ipcSetting.enabled != null -> {
                isEnabled = ipcSetting.enabled
                if(!isEnabled) {
                    setForceDarkEnabled(false)
                }else{
                    setForceDarkEnabled(isAlwaysForceDarkEnabled)
                }
            }
            ipcSetting.alwaysForceDark != null -> {
                isAlwaysForceDarkEnabled = ipcSetting.alwaysForceDark
                setForceDarkEnabled(isAlwaysForceDarkEnabled)
            }
            ipcSetting.oxygenForceDark != null -> {
                isOxygenForceDarkEnabled = ipcSetting.oxygenForceDark
                setOxygenOSEntireWorldEnabled(isOxygenForceDarkEnabled)
            }
            ipcSetting.sendAppCloses != null -> sendAppCloses = ipcSetting.sendAppCloses
            ipcSetting.packageChange != null -> {
                with(ipcSetting.packageChange){
                    if(enabled) appWhitelist.add(packageName)
                    else appWhitelist.remove(packageName)
                }
            }
        }
    }

    /**
     *  Sets Force Dark to a given value using the reflected SystemProperties rather than a shell
     *  command (for speed)
     */
    private fun setForceDarkEnabled(enabled: Boolean) {
        val currentValue = SystemProperties[FORCE_DARK_PROP]?.toBoolean() ?: false
        if(currentValue == enabled) return
        SystemProperties[FORCE_DARK_PROP] = enabled.toString()
    }

    /**
     *  Sets the Oxygen OS "entire world" force dark Settings.Secure setting
     *  This only seems to be used in Oxygen OS 10 and does nothing in 11
     */
    private fun setOxygenOSEntireWorldEnabled(enabled: Boolean){
        val enabledString = if(enabled) "1" else "0"
        val commands = arrayOf(OXYGEN_OS_WORLD_FORCE_DARK, OXYGEN_OS_AOSP_FORCE_DARK).map { "settings put secure $it $enabledString" }
        runCommands(*commands.toTypedArray())
    }

    /**
     *  Registers a Process Observer via IActivityManager - to the system Activity Manager service.
     *  The magic here is Shizuku's SystemServiceHelper, which uses reflection to get the system
     *  service, which we can then use registerProcessObserver() on and note the callbacks to
     *  onForegroundActivitiesChanged(), where foregroundActivities = true when an app is opened.
     */
    private val processObserver = object : IProcessObserver.Stub() {
        override fun onForegroundActivitiesChanged(pid: Int, uid: Int, foregroundActivities: Boolean) {
            if(!isEnabled || isAlwaysForceDarkEnabled) return
            val packageName = activityManager.getPackageNameForPid(pid) ?: return
            if(ignoredPackages.contains(packageName)) return
            if(foregroundActivities){
                //New app open
                currentApp.tryEmit(packageName)
            }else if(!foregroundActivities && sendAppCloses && packageName == currentApp.value){
                //App closed & user has chosen to send app closes
                currentApp.tryEmit(null)
            }
        }

        override fun onForegroundServicesChanged(pid: Int, uid: Int, serviceTypes: Int) {
            //Don't care
        }

        override fun onProcessDied(pid: Int, uid: Int) {
            //Don't care
        }
    }

    private fun registerProcessObserver() {
        activityManager.unregisterProcessObserver(processObserver)
        activityManager.registerProcessObserver(processObserver)
    }

    /**
     *  Called by Shizuku - NOT by libsu though. Clean up what we can nicely.
     *  libsu will simply kill the process as part of the restart
     */
    override fun destroy() {
        activityManager.unregisterProcessObserver(processObserver)
        mDispatcher.onServicePreSuperOnDestroy()
        exitProcess(0)
    }

    /**
     *  Finds a given PID in the running apps and returns its process name, which seems to be the
     *  package name.
     */
    private fun IActivityManager.getPackageNameForPid(pid: Int): String? {
        val rawName = runningAppProcesses.find { it.pid == pid }?.processName
        return if(rawName?.contains(":") == true){
            rawName.substring(0, rawName.indexOf(":"))
        }else rawName
    }

    /**
     *  Kills all other processes of either the :root or the :service IPCs.
     *  This is done by reading the whole list of processes, printing the PID and names,
     *  filtering the list by those that are the :root or :service processes, and killing the
     *  given pid using `kill`. If no other processes are found, nothing is killed.
     */
    override fun killOtherInstances(){
        val myPid = android.os.Process.myPid()
        val processesByName = runCommand("ps -A -o PID,NAME")
        //Find just my processes & retrieve the PIDs that don't match our own
        val otherPids = processesByName.map { it.trim() }
            .filter { it.endsWith(SHIZUKU_SERVICE_ID) || it.endsWith(ROOT_SERVICE_ID) }
            .mapNotNull {
                val split = it.split(" ")
                val pid = split[0]
                val name = split[1]
                if (name == SHIZUKU_SERVICE_ID || name == ROOT_SERVICE_ID) pid.toIntOrNull() else null
            }.filterNot { it == myPid }
        if (otherPids.isEmpty()) {
            return
        }
        val killCommands = otherPids.map { "kill $it" }
        runCommands(*killCommands.toTypedArray())
    }

    /**
     *  Gets the device's last location, this is ONLY used with user consent.
     */
    override fun getLocation(): LatLng? {
        return try {
            val location = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                locationManager.getLastLocation(null, BuildConfig.APPLICATION_ID, "darq") ?: return null
            }else{
                val locationRequestClass = Class.forName("android.location.LocationRequest")
                val lastLocationMethod = locationManager::class.java.getMethod("getLastLocation", locationRequestClass, String::class.java)
                val location = lastLocationMethod.invoke(locationManager, null, BuildConfig.APPLICATION_ID) as? Location
                location ?: return null
            }
            return LatLng(location.latitude, location.longitude)
        }catch (e: Exception){
            null
        }
    }

    /**
     *  Sets the night mode using UiModeManager
     */
    override fun setNightMode(nightMode: Boolean) {
        @Suppress("UsePropertyAccessSyntax")
        uiModeManager.setNightMode(if(nightMode) 2 else 1)
    }

    /**
     *  Runs a given command *without root*, wrapping it in `sh` and returning the output lines
     */
    private fun runCommand(command: String): List<String> {
        ProcessBuilder().command("sh", "-c", command).start().run {
            val lines = inputStream.bufferedReader().lines().toList()
            destroy()
            return lines
        }
    }

    /**
     *  Runs a set of commands, wrapping each in `sh`, ignoring output
     */
    private fun runCommands(vararg commands: String) {
        ProcessBuilder().run {
            commands.forEach {
                command("sh", "-c", it).start().waitFor()
            }
        }
    }

    /**
     *  Gets the default "Home" app if set, or null if not. We don't want to apply force dark to
     *  the home app, and don't want to disable/enable it when Home is briefly visible for speed.
     */
    private fun getLauncherPackage(): String? {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo: ResolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) ?: return null
        return resolveInfo.activityInfo.packageName
    }

    /**
     *  Gets the "Quickstep" (recents) package the device is using, so we don't apply changes to
     *  force dark when the recents is opened and closed, again for speed.
     *  If the user hasn't set a custom launcher, this will most likely be the same as the launcher
     *  package, and will be filtered out with distinct()
     */
    private fun getQuickstepPackage(): String? {
        val systemResources = Resources.getSystem()
        val recentsComponentNameId = systemResources.getIdentifier("config_recentsComponentName", "string", "android")
        if(recentsComponentNameId == 0) return null
        val recentsComponentName = systemResources.getString(recentsComponentNameId)
        return if(recentsComponentName != null) {
            ComponentName.unflattenFromString(recentsComponentName)?.packageName
        }else null
    }

    override fun ping() {
        //Hello!
    }

    override fun getServiceType(): String {
        return this.serviceType.toString()
    }

}