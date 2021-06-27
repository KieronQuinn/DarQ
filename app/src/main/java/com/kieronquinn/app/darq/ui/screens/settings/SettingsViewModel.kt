package com.kieronquinn.app.darq.ui.screens.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class SettingsViewModel: ViewModel() {

    abstract val developerOptionsVisible: Flow<Boolean>

    abstract fun onAdvancedOptionsClicked()
    abstract fun onAppWhitelistClicked()
    abstract fun onDeveloperOptionsClicked()
    abstract fun onAutoDarkThemeCheckedChange(checked: Boolean, sharedViewModel: ContainerSharedViewModel): Boolean
    abstract fun onFaqClicked()
    abstract fun onAboutTripleTapped()
    abstract fun onOssLicencesClicked()
    abstract fun isOxygenForceDarkSupported(): Boolean

}

class SettingsViewModelImpl(private val navigation: Navigation, private val settings: DarqSharedPreferences): SettingsViewModel() {

    private val _developerOptionsVisible = MutableStateFlow(settings.developerOptions)
    override val developerOptionsVisible = _developerOptionsVisible.asStateFlow()

    override fun onAdvancedOptionsClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsFragmentDirections.actionSettingsFragmentToSettingsAdvancedFragment())
        }
    }

    override fun onAppWhitelistClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsFragmentDirections.actionSettingsFragmentToSettingsAppPickerFragment())
        }
    }

    override fun onDeveloperOptionsClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsFragmentDirections.actionSettingsFragmentToSettingsDeveloperOptionsFragment())
        }
    }

    override fun onAutoDarkThemeCheckedChange(checked: Boolean, sharedViewModel: ContainerSharedViewModel): Boolean {
        if(!checked){
            sharedViewModel.setAutoDarkThemeEnabled(false)
            return true
        }
        viewModelScope.launch {
            navigation.navigate(SettingsFragmentDirections.actionSettingsFragmentToLocationPermissionDialogFragment())
        }
        return false
    }

    override fun onFaqClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsFragmentDirections.actionSettingsFragmentToSettingsFaqFragment())
        }
    }

    override fun onAboutTripleTapped() {
        viewModelScope.launch {
            val newValue = !settings.developerOptions
            settings.developerOptions = newValue
            _developerOptionsVisible.emit(newValue)
        }
    }

    override fun onOssLicencesClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsFragmentDirections.actionSettingsFragmentToOssLicencesActivity())
        }
    }

    override fun isOxygenForceDarkSupported(): Boolean {
        return Build.MANUFACTURER == "OnePlus" && Build.VERSION.SDK_INT < Build.VERSION_CODES.R
    }

}