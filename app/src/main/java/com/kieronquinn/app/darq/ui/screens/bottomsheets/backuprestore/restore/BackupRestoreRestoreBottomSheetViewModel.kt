package com.kieronquinn.app.darq.ui.screens.bottomsheets.backuprestore.restore

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kieronquinn.app.darq.BuildConfig
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.model.settings.SettingsBackup
import com.kieronquinn.app.darq.utils.extensions.ungzip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BackupRestoreRestoreViewModel: ViewModel() {

    abstract val state: Flow<State>
    abstract fun setOutputUri(uri: Uri)

    sealed class State {
        object Idle: State()
        data class Restore(val uri: Uri): State()
        data class Complete(val result: Result, val showLocationPrompt: Boolean = false): State()
    }

    enum class Result {
        SUCCESS, FAILED
    }

}

class BackupRestoreRestoreBottomSheetViewModelImpl(private val applicationContext: Context, private val settings: DarqSharedPreferences, private val navigation: Navigation): BackupRestoreRestoreViewModel() {

    companion object {
        //Minimum time to show for so we don't dismiss immediately
        private const val MINIMUM_SHOW_TIME = 2000L
    }

    private val _state = MutableStateFlow<State>(State.Idle).apply {
        viewModelScope.launch {
            collect {
                val result = handleState(it)
                if(result != null){
                    emit(result)
                }
            }
        }
    }
    override val state: Flow<State> = _state

    override fun setOutputUri(uri: Uri) {
        viewModelScope.launch {
            if(_state.value is State.Idle){
                _state.emit(State.Restore(uri))
            }
        }
    }

    private suspend fun handleState(state: State): State? {
        return when(state){
            is State.Restore -> restoreBackup(state.uri)
            is State.Complete -> {
                navigation.navigateUpTo(R.id.settingsFragment)
                if(state.showLocationPrompt){
                    navigation.navigate(R.id.action_global_locationPermissionDialogFragment)
                }
                null
            }
            else -> null
        }
    }

    private suspend fun restoreBackup(inputUri: Uri): State {
        val startTime = System.currentTimeMillis()
        return withContext(Dispatchers.IO){
            runCatching {
                val fileInput = applicationContext.contentResolver.openInputStream(inputUri)
                    ?: return@withContext State.Complete(Result.FAILED)
                fileInput.use {
                    val unGzipped = it.readBytes().ungzip()
                    val settingsBackup = Gson().fromJson(unGzipped, SettingsBackup::class.java)
                    val showLocationPrompt = settings.fromSettingsBackup(settingsBackup)
                    val remainingTime = MINIMUM_SHOW_TIME - (System.currentTimeMillis() - startTime)
                    if (remainingTime > 0L) {
                        delay(remainingTime)
                    }
                    return@withContext State.Complete(Result.SUCCESS, showLocationPrompt)
                }
            }.onFailure {
                if(BuildConfig.DEBUG){
                    it.printStackTrace()
                }
            }
            State.Complete(Result.FAILED)
        }
    }

}
