package com.kieronquinn.app.darq.ui.screens.bottomsheets.errors

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.model.shizuku.ShizukuConstants
import com.kieronquinn.app.darq.ui.base.BaseBottomSheetDialogFragment

class ShizukuNotInstalledBottomSheetFragment: BaseBottomSheetDialogFragment() {

    override val title by lazy {
        getString(R.string.bottom_sheet_shizuku_not_installed_title)
    }

    override val content by lazy {
        getString(R.string.bottom_sheet_shizuku_not_installed_content)
    }

    override val positiveText by lazy {
        getString(R.string.bottom_sheet_shizuku_not_installed_positive)
    }

    override val negativeText by lazy {
        getString(R.string.bottom_sheet_shizuku_not_installed_negative)
    }

    override val neutralText by lazy {
        getString(R.string.bottom_sheet_shizuku_not_installed_neutral)
    }

    override val cancelable = false

    override fun onPositiveClicked(dialog: BottomSheetDialog) {
        openShizukuPlayStore()
    }

    override fun onNegativeClicked(dialog: BottomSheetDialog) {
        super.onNegativeClicked(dialog)
        requireActivity().finish()
    }

    override fun onNeutralClicked(dialog: BottomSheetDialog) {
        lifecycleScope.launchWhenResumed {
            navigation.navigate(R.id.action_global_shizukuInfoBottomSheetFragment)
        }
    }

    private fun openShizukuPlayStore(){
        val url = "market://details?id=${ShizukuConstants.SHIZUKU_PACKAGE_NAME}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        try {
            startActivity(intent)
        }catch (e: ActivityNotFoundException){
            openShizukuWeb()
        }
    }

    private fun openShizukuWeb(){
        val url = ShizukuConstants.SHIZUKU_WEB
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }.also {
            startActivity(it)
        }
    }

}