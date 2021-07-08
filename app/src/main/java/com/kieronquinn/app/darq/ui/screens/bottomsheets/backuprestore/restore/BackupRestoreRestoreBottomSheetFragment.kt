package com.kieronquinn.app.darq.ui.screens.bottomsheets.backuprestore.restore

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.databinding.FragmentBottomSheetRestoreBinding
import com.kieronquinn.app.darq.ui.base.BaseBottomSheetFragment
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.darq.utils.extensions.navGraphViewModel
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BackupRestoreRestoreBottomSheetFragment: BaseBottomSheetFragment<FragmentBottomSheetRestoreBinding>(FragmentBottomSheetRestoreBinding::inflate) {

    private val viewModel by viewModel<BackupRestoreRestoreViewModel>()
    private val arguments by navArgs<BackupRestoreRestoreBottomSheetFragmentArgs>()
    private val sharedViewModel by navGraphViewModel<ContainerSharedViewModel>(R.id.nav_graph_main)

    override val cancelable = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            fragmentRestoreProgress.applyMonet()
            ViewCompat.setOnApplyWindowInsetsListener(root){ view, insets ->
                val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                val extraPadding = resources.getDimension(R.dimen.padding_8).toInt()
                view.updatePadding(bottom = bottomInset + extraPadding)
                insets
            }
        }
        lifecycleScope.launchWhenResumed {
            launch {
                viewModel.state.collect {
                    if (it is BackupRestoreRestoreViewModel.State.Complete) {
                        handleComplete(it.result)
                    }
                }
            }
            launch {
                viewModel.setOutputUri(arguments.uri)
            }
        }
    }

    private fun handleComplete(result: BackupRestoreRestoreViewModel.Result){
        when(result){
            BackupRestoreRestoreViewModel.Result.SUCCESS -> {
                Toast.makeText(requireContext(), R.string.item_backup_restore_restore_success, Toast.LENGTH_LONG).show()
                sharedViewModel.onRestoreSuccess()
            }
            BackupRestoreRestoreViewModel.Result.FAILED -> {
                Toast.makeText(requireContext(), R.string.item_backup_restore_restore_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

}