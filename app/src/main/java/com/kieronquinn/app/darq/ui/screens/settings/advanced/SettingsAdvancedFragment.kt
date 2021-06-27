package com.kieronquinn.app.darq.ui.screens.settings.advanced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.databinding.FragmentSettingsAdvancedBinding
import com.kieronquinn.app.darq.model.settings.SettingsItem
import com.kieronquinn.app.darq.ui.base.AutoExpandOnRotate
import com.kieronquinn.app.darq.ui.base.BackAvailable
import com.kieronquinn.app.darq.ui.screens.settings.BaseSettingsFragment
import com.kieronquinn.app.darq.ui.screens.settings.SettingsAdapter
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively

class SettingsAdvancedFragment : BaseSettingsFragment<FragmentSettingsAdvancedBinding>(FragmentSettingsAdvancedBinding::inflate), AutoExpandOnRotate, BackAvailable {

    override val settingsItems by lazy {
        listOf<SettingsItem>(
            SettingsItem.SwitchSetting(
                R.drawable.ic_advanced_always_use_force_dark,
                getString(R.string.item_always_use_force_dark_title),
                getString(R.string.item_always_use_force_dark_content),
                settings::alwaysForceDark
            ),
            SettingsItem.SwitchSetting(
                R.drawable.ic_advanced_send_app_closes,
                getString(R.string.item_send_app_closes_title),
                getString(R.string.item_send_app_closes_content),
                settings::sendAppCloses
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
    }

}