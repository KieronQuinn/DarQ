package com.kieronquinn.app.darq.ui.screens.bottomsheets.errors

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.model.shizuku.ShizukuConstants
import com.kieronquinn.app.darq.ui.base.BaseBottomSheetDialogFragment

class ShizkuNotRunningBottomSheetFragment: BaseBottomSheetDialogFragment() {

    override val title by lazy {
        getString(R.string.bottom_sheet_shizuku_not_running_title)
    }

    override val content by lazy {
        getString(R.string.bottom_sheet_shizuku_not_running_content)
    }

    override val positiveText by lazy {
        getString(R.string.bottom_sheet_shizuku_not_running_positive)
    }

    override val negativeText by lazy {
        getString(R.string.bottom_sheet_shizuku_not_running_negative)
    }

    override val cancelable = false

    override fun onPositiveClicked(dialog: BottomSheetDialog) {
        super.onPositiveClicked(dialog)
        val packageManager = requireContext().packageManager
        startActivity(packageManager.getLaunchIntentForPackage(ShizukuConstants.SHIZUKU_PACKAGE_NAME))
    }

    override fun onNegativeClicked(dialog: BottomSheetDialog) {
        super.onNegativeClicked(dialog)
        requireActivity().finish()
    }

}