package com.kieronquinn.app.darq.ui.screens.bottomsheets.backuprestore.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kieronquinn.app.darq.BuildConfig
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.utils.extensions.gzip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BackupRestoreBackupBottomSheetViewModel: ViewModel() {

    abstract val state: Flow<State>
    abstract fun setOutputUri(uri: Uri)

    sealed class State {
        object Idle: State()
        data class Backup(val uri: Uri): State()
        data class Complete(val result: Result): State()
    }

    enum class Result {
        SUCCESS, FAILED
    }

}

class BackupRestoreBackupBottomSheetViewModelImpl(private val applicationContext: Context, private val settings: DarqSharedPreferences, private val navigation: Navigation): BackupRestoreBackupBottomSheetViewModel() {

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
                _state.emit(State.Backup(uri))
            }
        }
    }

    private suspend fun handleState(state: State): State? {
        return when(state){
            is State.Backup -> State.Complete(createBackup(state.uri))
            is State.Complete -> {
                navigation.navigateUpTo(R.id.settingsFragment)
                null
            }
            else -> null
        }
    }

    private suspend fun createBackup(outputUri: Uri): Result {
        val startTime = System.currentTimeMillis()
        return withContext(Dispatchers.IO){
            runCatching {
                val settingsBackup = Gson().toJson(settings.getSettingsBackup())
                val file = DocumentFile.fromSingleUri(applicationContext, outputUri) ?: return@withContext Result.FAILED
                if(!file.canWrite()) return@withContext Result.FAILED
                val outputStream = applicationContext.contentResolver.openOutputStream(outputUri) ?: return@withContext Result.FAILED
                outputStream.use {
                    it.write(settingsBackup.gzip())
                    it.flush()
                }
            }.onFailure {
                if(BuildConfig.DEBUG){
                    it.printStackTrace()
                }
            }
            val remainingTime = MINIMUM_SHOW_TIME - (System.currentTimeMillis() - startTime)
            if(remainingTime > 0L){
                delay(remainingTime)
            }
            return@withContext Result.SUCCESS
        }
    }

}
