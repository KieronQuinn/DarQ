package com.kieronquinn.app.darq.ui.screens.settings.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class SettingsDeveloperOptionsViewModel: ViewModel() {

    abstract fun getServiceInfo(sharedViewModel: ContainerSharedViewModel): Flow<Pair<Int, String?>>
    abstract fun onMonetColorPickerClicked()

}

class SettingsDeveloperOptionsViewModelImpl(private val navigation: Navigation): SettingsDeveloperOptionsViewModel() {

    override fun getServiceInfo(sharedViewModel: ContainerSharedViewModel) = sharedViewModel.loadingState.map {
        if(it is ContainerSharedViewModel.ServiceState.Loaded){
            val serviceType = getServiceState(it.serviceCall)
            if(serviceType != null) {
                Pair(R.string.item_developer_options_service_info_connected, serviceType)
            }else{
                Pair(R.string.item_developer_options_service_info_not_connected, null)
            }
        }else{
            Pair(R.string.item_developer_options_service_info_not_connected, null)
        }
    }

    private suspend fun getServiceState(serviceCall: suspend () -> DarqServiceConnectionProvider.ServiceResult): String? {
        val service = serviceCall.invoke()
        if(service is DarqServiceConnectionProvider.ServiceResult.Success){
            return service.service.serviceType
        }
        return null
    }

    override fun onMonetColorPickerClicked() {
        viewModelScope.launch {
            navigation.navigate(SettingsDeveloperOptionsFragmentDirections.actionSettingsDeveloperOptionsFragmentToColorPickerBottomSheetFragment())
        }
    }

}