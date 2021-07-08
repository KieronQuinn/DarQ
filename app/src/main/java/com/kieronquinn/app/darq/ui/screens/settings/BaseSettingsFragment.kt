package com.kieronquinn.app.darq.ui.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.model.settings.SettingsItem
import com.kieronquinn.app.darq.ui.base.BoundFragment
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.darq.ui.utils.TransitionUtils
import com.kieronquinn.app.darq.utils.extensions.navGraphViewModel
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject

abstract class BaseSettingsFragment<T: ViewBinding>(inflate: (LayoutInflater, ViewGroup?, Boolean) -> T): BoundFragment<T>(inflate){

    internal val sharedViewModel by navGraphViewModel<ContainerSharedViewModel>(R.id.nav_graph_main)
    internal abstract val settingsItems: MutableList<SettingsItem>
    internal val settings by inject<DarqSharedPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), true)
        enterTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), true)
        returnTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), false)
        reenterTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), false)
    }

    internal fun setupRecyclerView(recyclerView: RecyclerView, settingsAdapter: SettingsAdapter) {
        recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView){ view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = bottomInset)
            insets
        }
    }

    internal fun setupSnackbarPadding(recyclerView: RecyclerView){
        lifecycleScope.launchWhenResumed {
            sharedViewModel.showSnackbar.collect {
                if(it){
                    addSnackbarPadding(recyclerView)
                }else{
                    removeSnackbarPadding(recyclerView)
                }
            }
        }
    }

    private fun addSnackbarPadding(recyclerView: RecyclerView){
        if(!settingsItems.contains(SettingsItem.SnackbarPadding)){
            settingsItems.add(SettingsItem.SnackbarPadding)
            recyclerView.adapter?.notifyItemInserted(settingsItems.size - 1)
        }
    }

    private fun removeSnackbarPadding(recyclerView: RecyclerView){
        if(settingsItems.contains(SettingsItem.SnackbarPadding)){
            settingsItems.remove(SettingsItem.SnackbarPadding)
            recyclerView.adapter?.notifyItemRemoved(settingsItems.size)
        }
    }

}