package com.kieronquinn.app.darq.ui.screens.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class LocationPermissionDialogViewModel: ViewModel() {

    abstract fun onUseLocationClicked(sharedViewModel: ContainerSharedViewModel)
    abstract fun onUseTimezoneClicked(sharedViewModel: ContainerSharedViewModel)
    abstract fun onCancelClicked()

}

class LocationPermissionDialogViewModelImpl(private val settings: DarqSharedPreferences, private val navigation: Navigation): LocationPermissionDialogViewModel() {

    override fun onUseLocationClicked(sharedViewModel: ContainerSharedViewModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                settings.useLocation = true
                sharedViewModel.setAutoDarkThemeEnabled(true)
            }
            navigation.navigateBack()
        }
    }

    override fun onUseTimezoneClicked(sharedViewModel: ContainerSharedViewModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                settings.useLocation = false
                sharedViewModel.setAutoDarkThemeEnabled(true)
            }
            navigation.navigateBack()
        }
    }

    override fun onCancelClicked() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}