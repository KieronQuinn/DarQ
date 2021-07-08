package com.kieronquinn.app.darq.ui.screens.bottomsheets.backuprestore

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.databinding.FragmentBottomSheetBackupRestoreBinding
import com.kieronquinn.app.darq.ui.base.BaseBottomSheetFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class BackupRestoreBottomSheetFragment: BaseBottomSheetFragment<FragmentBottomSheetBackupRestoreBinding>(FragmentBottomSheetBackupRestoreBinding::inflate) {

    private val viewModel by viewModel<BackupRestoreBottomSheetViewModel>()

    private val backupSelection = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        if(it != null){
            viewModel.onBackupSelected(it)
        }
    }

    private val restoreSelection = registerForActivityResult(ActivityResultContracts.OpenDocument()){
        if(it != null){
            viewModel.onRestoreSelected(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            backupRestoreCancel.setOnClickListener {
                viewModel.onCancelClicked()
            }
            backupRestoreBackup.setOnClickListener {
                viewModel.onBackupClicked(backupSelection)
            }
            backupRestoreRestore.setOnClickListener {
                viewModel.onRestoreClicked(restoreSelection)
            }
            val accent = ColorStateList.valueOf(monet.getAccentColor(requireContext()))
            binding.backupRestoreIcBackup.imageTintList = accent
            binding.backupRestoreIcRestore.imageTintList = accent
            binding.backupRestoreCancel.setTextColor(accent)
            val secondaryBackground = monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext())
            binding.backupRestoreRestore.backgroundTintList = ColorStateList.valueOf(secondaryBackground)
            binding.backupRestoreBackup.backgroundTintList = ColorStateList.valueOf(secondaryBackground)
            ViewCompat.setOnApplyWindowInsetsListener(root){ view, insets ->
                val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                val extraPadding = resources.getDimension(R.dimen.padding_8).toInt()
                view.updatePadding(bottom = bottomInset + extraPadding)
                insets
            }
        }
    }

}