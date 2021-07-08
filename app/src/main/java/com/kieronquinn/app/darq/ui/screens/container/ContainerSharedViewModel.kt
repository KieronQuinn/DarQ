package com.kieronquinn.app.darq.ui.screens.container

import android.content.Context
import android.os.DeadObjectException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.github.UpdateChecker
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.model.darq.DarqConstants
import com.kieronquinn.app.darq.model.settings.IPCSetting
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.utils.extensions.isDarkTheme
import com.kieronquinn.app.darq.utils.extensions.secureSettingIntFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.reflect.KSuspendFunction0

abstract class ContainerSharedViewModel: ViewModel() {

    abstract val loadingState: Flow<ServiceState>
    abstract fun loadService()

    sealed class ServiceState {
        object Idle: ServiceState()
        object Loading: ServiceState()
        data class Loaded(val serviceCall: KSuspendFunction0<DarqServiceConnectionProvider.ServiceResult>): ServiceState()
        data class Error(val error: DarqServiceConnectionProvider.ServiceFailureReason): ServiceState()
    }

    abstract val syncState: Flow<SyncState>
    abstract val showSnackbar: Flow<Boolean>

    abstract val autoDarkServiceStartBus: Flow<Unit>
    abstract val autoDarkTheme: Flow<Boolean>

    abstract fun setAutoDarkThemeEnabled(enabled: Boolean)

    abstract val restoreBus: Flow<Unit>
    abstract fun onRestoreSuccess()

    sealed class SyncState {
        object NotSyncing: SyncState()
        data class Syncing(val syncQueue: Queue<IPCSetting>): SyncState()
        data class SyncComplete(val success: Boolean): SyncState()
    }

    abstract fun queueIPCSync(ipcSetting: IPCSetting)
    abstract suspend fun killOtherInstances(): Boolean

    abstract val darkModeState: Flow<DarkModeState>
    abstract val currentDarkModeEnabled: Flow<Boolean>
    abstract val switchWarning: Flow<Int?>

    abstract val update: Flow<UpdateChecker.Update?>
    abstract fun getAvailableUpdate(): UpdateChecker.Update?
    abstract fun clearUpdate()

    enum class DarkModeState(val value: Int) {
        AUTO(0), DISABLED(1), ENABLED(2), CUSTOM(3)
    }

}

class ContainerSharedViewModelImpl(context: Context, private val serviceProvider: DarqServiceConnectionProvider, private val settings: DarqSharedPreferences, private val updateChecker: UpdateChecker): ContainerSharedViewModel() {

    companion object {
        private const val TIMEOUT_SYNC_COMPLETE = 2500L
        private const val SERVICE_START_DEBOUNCE = 500L
    }

    private val _update = MutableStateFlow<UpdateChecker.Update?>(null).apply {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                updateChecker.clearCachedDownloads(context)
            }
            updateChecker.getLatestRelease().collect {
                emit(it)
            }
        }
    }

    override val update = _update.asStateFlow()

    override fun getAvailableUpdate(): UpdateChecker.Update? {
        return _update.value
    }

    override fun clearUpdate() {
        viewModelScope.launch {
            _update.emit(null)
        }
    }

    private val _loadingState = MutableStateFlow<ServiceState>(ServiceState.Idle).apply {
        viewModelScope.launch {
            collect {
                when(it){
                    is ServiceState.Loading -> loadService()
                }
            }
        }
    }

    private suspend fun MutableStateFlow<ServiceState>.loadService(){
        when(val result = serviceProvider.getService()){
            is DarqServiceConnectionProvider.ServiceResult.Success -> {
                emit(ServiceState.Loaded(serviceProvider::getService))
            }
            is DarqServiceConnectionProvider.ServiceResult.Failed -> {
                emit(ServiceState.Error(result.reason))
            }
        }
    }

    override fun loadService() {
        viewModelScope.launch {
            _loadingState.emit(ServiceState.Loading)
        }
    }

    override val loadingState = _loadingState.asSharedFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.NotSyncing).apply {
        viewModelScope.launch {
            collect {
                handleLocalSyncChanges(it)
            }
        }
    }

    private var syncCompleteTimeoutJob: Job? = null

    private suspend fun MutableStateFlow<SyncState>.handleLocalSyncChanges(syncState: SyncState) {
        if(syncState is SyncState.SyncComplete){
            syncCompleteTimeoutJob = viewModelScope.launch {
                delay(TIMEOUT_SYNC_COMPLETE)
                if(value is SyncState.SyncComplete){
                    syncCompleteTimeoutJob = null
                    emit(SyncState.NotSyncing)
                }
            }
        }else syncCompleteTimeoutJob?.cancelAndJoin()
        if(syncState is SyncState.Syncing){
            runSync()
        }
    }

    override fun queueIPCSync(ipcSetting: IPCSetting) {
        viewModelScope.launch(Dispatchers.IO) {
            (_syncState.value as? SyncState.Syncing)?.syncQueue?.add(ipcSetting) ?: run {
                startSyncing(ipcSetting)
            }
        }
    }

    override suspend fun killOtherInstances(): Boolean {
        val serviceCall = (_loadingState.value as? ServiceState.Loaded) ?: return false
        val service = serviceCall.serviceCall.invoke()
        return if(service is DarqServiceConnectionProvider.ServiceResult.Success){
            try {
                service.service.killOtherInstances()
                true
            }catch (e: DeadObjectException){
                false
            }
        }else false
    }

    private suspend fun startSyncing(ipcSetting: IPCSetting){
        _syncState.emit(SyncState.Syncing(ArrayDeque<IPCSetting>().apply {
            add(ipcSetting)
        }))
    }

    private suspend fun runSync(){
        var syncSuccess = true
        while(true){
            val syncQueue = (_syncState.value as? SyncState.Syncing)?.syncQueue ?: break
            if(syncQueue.size == 0) break
            val result = syncItem(syncQueue.remove())
            if(!result){
                syncSuccess = false
                break
            }
        }
        _syncState.emit(SyncState.SyncComplete(syncSuccess))
    }

    private suspend fun syncItem(ipcSetting: IPCSetting): Boolean {
        val serviceCall = (_loadingState.value as? ServiceState.Loaded) ?: return false
        val service = serviceCall.serviceCall.invoke()
        return if(service is DarqServiceConnectionProvider.ServiceResult.Success){
            try {
                service.service.notifySettingsChange(ipcSetting)
                true
            }catch (e: DeadObjectException){
                false
            }
        }else false
    }

    override fun setAutoDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.autoDarkTheme = enabled
            _autoDarkTheme.emit(enabled)
        }
    }

    override val darkModeState = context.secureSettingIntFlow(DarqConstants.SETTINGS_SECURE_UI_MODE, 0).map { setting ->
        DarkModeState.values().firstOrNull { it.value == setting } ?: DarkModeState.AUTO
    }

    override val currentDarkModeEnabled = flow {
        emit(context.isDarkTheme)
    }

    override val switchWarning = combine(darkModeState, currentDarkModeEnabled){ state, current ->
        when(state){
            DarkModeState.DISABLED -> R.string.item_switch_main_subtitle_off
            else -> {
                if(current){
                    null
                }else{
                    R.string.item_switch_main_subtitle
                }
            }
        }
    }

    private val _autoDarkTheme = MutableStateFlow(settings.autoDarkTheme)
    override val autoDarkTheme = _autoDarkTheme.asStateFlow()

    override val autoDarkServiceStartBus = autoDarkTheme.map { }.debounce(SERVICE_START_DEBOUNCE)

    override val syncState: Flow<SyncState> = _syncState
    override val showSnackbar: Flow<Boolean> = syncState.map { it !is SyncState.NotSyncing }.distinctUntilChanged { old, new -> old == new }

    private val _restoreBus = Channel<Unit>()
    override val restoreBus = _restoreBus.receiveAsFlow()
    override fun onRestoreSuccess() {
        viewModelScope.launch {
            _restoreBus.send(Unit)
        }
    }

}