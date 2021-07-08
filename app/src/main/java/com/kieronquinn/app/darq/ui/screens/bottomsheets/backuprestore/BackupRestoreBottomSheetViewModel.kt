package com.kieronquinn.app.darq.ui.screens.bottomsheets.backuprestore

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.darq.components.navigation.Navigation
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class BackupRestoreBottomSheetViewModel : ViewModel() {

    abstract fun onBackupClicked(launcher: ActivityResultLauncher<String>)
    abstract fun onBackupSelected(uri: Uri)
    abstract fun onRestoreClicked(launcher: ActivityResultLauncher<Array<String>>)
    abstract fun onRestoreSelected(uri: Uri)
    abstract fun onCancelClicked()

}

class BackupRestoreBottomSheetViewModelImpl(private val navigation: Navigation) :
    BackupRestoreBottomSheetViewModel() {

    companion object {
        private const val backupConfigFilename = "darq-config-%s.darqbkp"
    }

    override fun onBackupClicked(launcher: ActivityResultLauncher<String>) {
        launcher.launch(getBackupFilename())
    }

    override fun onBackupSelected(uri: Uri) {
        viewModelScope.launch {
            navigation.navigate(
                BackupRestoreBottomSheetFragmentDirections.actionBackupRestoreBottomSheetFragmentToBackupRestoreBackupBottomSheetFragment(
                    uri
                )
            )
        }
    }

    override fun onRestoreClicked(launcher: ActivityResultLauncher<Array<String>>) {
        launcher.launch(arrayOf("*/*"))
    }

    override fun onRestoreSelected(uri: Uri) {
        viewModelScope.launch {
            navigation.navigate(
                BackupRestoreBottomSheetFragmentDirections.actionBackupRestoreBottomSheetFragmentToBackupRestoreRestoreBottomSheetFragment(
                    uri
                )
            )
        }
    }

    override fun onCancelClicked() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    private fun getBackupFilename(): String {
        return String.format(
            backupConfigFilename,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
        )
    }

}