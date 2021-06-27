package com.kieronquinn.app.darq.ui.screens.bottomsheets.errors

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.ui.base.BaseBottomSheetDialogFragment

class ServiceTimeoutBottomSheetFragment: BaseBottomSheetDialogFragment() {

    override val title by lazy {
        getString(R.string.bottom_sheet_service_timeout_title)
    }

    override val content by lazy {
        getString(R.string.bottom_sheet_service_timeout_content)
    }

    override val positiveText by lazy {
        getString(R.string.bottom_sheet_service_timeout_positive)
    }

    override val cancelable = false

    override fun onPositiveClicked(dialog: BottomSheetDialog) {
        super.onPositiveClicked(dialog)
        requireActivity().finish()
    }

}