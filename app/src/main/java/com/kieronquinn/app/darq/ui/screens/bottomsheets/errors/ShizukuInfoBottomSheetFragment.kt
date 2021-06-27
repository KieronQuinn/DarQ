package com.kieronquinn.app.darq.ui.screens.bottomsheets.errors

import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.ui.base.BaseBottomSheetDialogFragment

class ShizukuInfoBottomSheetFragment: BaseBottomSheetDialogFragment() {

    override val title by lazy {
        getString(R.string.bottom_sheet_shizuku_more_info_title)
    }

    override val content by lazy {
        getText(R.string.bottom_sheet_shizuku_more_info_content)
    }

    override val positiveText by lazy {
        getString(android.R.string.ok)
    }

}