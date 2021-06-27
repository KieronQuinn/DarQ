package com.kieronquinn.app.darq.ui.screens.dialogs

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.databinding.FragmentDialogLocationPermissionBinding
import com.kieronquinn.app.darq.ui.base.BaseDialogFragment
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.darq.utils.extensions.navGraphViewModel
import com.kieronquinn.monetcompat.extensions.views.overrideRippleColor
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocationPermissionDialogFragment: BaseDialogFragment<FragmentDialogLocationPermissionBinding>(FragmentDialogLocationPermissionBinding::inflate) {

    private val sharedViewModel by navGraphViewModel<ContainerSharedViewModel>(R.id.nav_graph_main)
    private val viewModel by viewModel<LocationPermissionDialogViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val background = monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext())
        dialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.background_dialog_location_permission)?.apply {
            setTint(background)
        })
        val accent = monet.getAccentColor(requireContext())
        binding.dialogLocationPermissionIcon.imageTintList = ColorStateList.valueOf(accent)
        binding.dialogLocationPermissionAccept.run {
            strokeColor = ColorStateList.valueOf(accent)
            overrideRippleColor(accent)
            setOnClickListener {
                viewModel.onUseLocationClicked(sharedViewModel)
            }
        }
        binding.dialogLocationPermissionDeny.run {
            strokeColor = ColorStateList.valueOf(accent)
            overrideRippleColor(accent)
            setOnClickListener {
                viewModel.onUseTimezoneClicked(sharedViewModel)
            }
        }
        binding.dialogLocationPermissionCancel.setOnClickListener {
            viewModel.onCancelClicked()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            it.attributes.windowAnimations = R.style.DialogAnimation
        }
        return dialog
    }

}