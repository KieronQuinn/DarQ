package com.kieronquinn.app.darq.ui.screens.settings.developer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.databinding.FragmentSettingsDeveloperOptionsBinding
import com.kieronquinn.app.darq.model.settings.SettingsItem
import com.kieronquinn.app.darq.ui.base.AutoExpandOnRotate
import com.kieronquinn.app.darq.ui.base.BackAvailable
import com.kieronquinn.app.darq.ui.screens.settings.BaseSettingsFragment
import com.kieronquinn.app.darq.ui.screens.settings.SettingsAdapter
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsDeveloperOptionsFragment: BaseSettingsFragment<FragmentSettingsDeveloperOptionsBinding>(FragmentSettingsDeveloperOptionsBinding::inflate), BackAvailable,
    AutoExpandOnRotate {

    private val viewModel by viewModel<SettingsDeveloperOptionsViewModel>()

    override val settingsItems by lazy {
        listOf<SettingsItem>(
            SettingsItem.Setting(
                R.drawable.ic_monet,
                getString(R.string.item_developer_options_monet_color_picker_title),
                getString(R.string.item_developer_options_monet_color_picker_content),
                tapAction = viewModel::onMonetColorPickerClicked
            ),
            SettingsItem.Setting(
                R.drawable.ic_developer_options_kill,
                getString(R.string.item_developer_options_kill_service_title),
                getString(R.string.item_developer_options_kill_service_content),
                tapAction = this::onKillOtherInstancesClicked
            ),
            SettingsItem.Setting(
                R.drawable.ic_developer_options_service_info,
                getString(R.string.item_developer_options_service_info)
            )
        ).toMutableList()
    }

    private val adapter by lazy {
        SettingsAdapter(requireContext(), settingsItems)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.applyMonetRecursively()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(binding.recyclerView, adapter)
        setupSnackbarPadding(binding.recyclerView)
        loadServiceState()
    }

    private fun loadServiceState(){
        lifecycleScope.launchWhenResumed {
            viewModel.getServiceInfo(sharedViewModel).collect {
                (settingsItems[2] as SettingsItem.Setting).content = if(it.second == null){
                    getString(it.first)
                }else{
                    getString(it.first, it.second)
                }
                adapter.notifyItemChanged(2)
            }
        }
    }

    private fun onKillOtherInstancesClicked() {
        lifecycleScope.launchWhenResumed {
            if(sharedViewModel.killOtherInstances()){
                Toast.makeText(requireContext(), R.string.item_developer_options_kill_service_toast_success, Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(requireContext(), R.string.item_developer_options_kill_service_toast_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

}