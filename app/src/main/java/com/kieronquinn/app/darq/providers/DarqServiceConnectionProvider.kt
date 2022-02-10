package com.kieronquinn.app.darq.providers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.DeadObjectException
import android.os.IBinder
import com.kieronquinn.app.darq.BuildConfig
import com.kieronquinn.app.darq.IDarqService
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.model.shizuku.ShizukuConstants
import com.kieronquinn.app.darq.service.impl.DarqService
import com.kieronquinn.app.darq.service.root.DarqRootService
import com.kieronquinn.app.darq.utils.extensions.isShizukuInstalled
import com.kieronquinn.app.darq.utils.extensions.suspendCoroutineWithTimeout
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import kotlin.coroutines.resume

class DarqServiceConnectionProvider(private val context: Context, private val settings: DarqSharedPreferences) {

    companion object {
        private const val SERVICE_TIMEOUT = 25000L
    }

    private val rootServiceIntent = Intent(context, DarqRootService::class.java)
    private var rootService: IDarqService? = null
    private val serviceLock = Mutex()

    private val darqProcessArgs = UserServiceArgs(ComponentName(context, DarqService::class.java)).apply {
        processNameSuffix(ShizukuConstants.SERVICE_NAME)
        debuggable(BuildConfig.DEBUG)
        version(BuildConfig.VERSION_CODE)
    }

    sealed class ServiceResult {
        data class Success(val service: IDarqService, val serviceType: ServiceType): ServiceResult()
        data class Failed(val reason: ServiceFailureReason): ServiceResult()
    }

    enum class ServiceFailureReason {
        TIMEOUT, SHIZUKU_PERMISSION_REQUIRED, SHIZUKU_NOT_STARTED, SHIZUKU_NOT_INSTALLED
    }

    enum class ServiceType {
        SHIZUKU, ROOT, UNKNOWN
    }

    private var serviceType: ServiceType = ServiceType.UNKNOWN

    suspend fun getService(): ServiceResult {
        return serviceLock.withLock {
            withContext(Dispatchers.Main) {
                getServiceLocked() ?: ServiceResult.Failed(ServiceFailureReason.TIMEOUT)
            }
        }
    }

    private suspend fun getServiceLocked() = suspendCoroutineWithTimeout<ServiceResult>(
        SERVICE_TIMEOUT
    ) {
        rootService?.let { service ->
            try {
                service.ping()
                it.resume(ServiceResult.Success(service, serviceType))
                return@suspendCoroutineWithTimeout
            }catch (e: DeadObjectException){
                //Service has died but onServiceDisconnected has not been called, restart as normal
            }
        }
        val serviceConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                try {
                    val rootService = IDarqService.Stub.asInterface(service)
                    rootService.setupService()
                    this@DarqServiceConnectionProvider.rootService = rootService
                    it.resume(ServiceResult.Success(rootService, serviceType))
                }catch (e: DeadObjectException){
                    it.resume(ServiceResult.Failed(ServiceFailureReason.TIMEOUT))
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                rootService = null
            }
        }
        if (Shell.rootAccess()) {
            //libsu:service doesn't re-bind so we have to stop & start
            RootService.stop(rootServiceIntent)
            RootService.bind(rootServiceIntent, serviceConnection)
        } else if(context.isShizukuInstalled()) {
            GlobalScope.launch {
                runCatching {
                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                        Shizuku.bindUserService(darqProcessArgs, serviceConnection)
                    } else {
                        it.resume(ServiceResult.Failed(ServiceFailureReason.SHIZUKU_PERMISSION_REQUIRED))
                    }
                }.onFailure { e ->
                    it.resume(ServiceResult.Failed(ServiceFailureReason.SHIZUKU_NOT_STARTED))
                }
            }
        } else {
            it.resume(ServiceResult.Failed(ServiceFailureReason.SHIZUKU_NOT_INSTALLED))
        }
    }

    private fun IDarqService.setupService(){
        GlobalScope.launch {
            withContext(Dispatchers.IO){
                onBind()
                setupSettings(settings.toIPCSetting())
                val enabledApps = settings.enabledApps
                enabledApps.forEachIndexed { index, app ->
                    setupWhitelist(index == 0, index == enabledApps.size - 1, app)
                }
            }
        }
    }

}