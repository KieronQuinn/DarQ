package com.kieronquinn.app.darq.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.databinding.FragmentBottomSheetBinding
import org.koin.android.ext.android.inject


abstract class BaseBottomSheetDialogFragment : BaseBottomSheetFragment<FragmentBottomSheetBinding>(FragmentBottomSheetBinding::inflate) {

    internal val navigation by inject<Navigation>()

    abstract val title: CharSequence
    abstract val content: CharSequence

    open val positiveText: CharSequence? = null
    open val negativeText: CharSequence? = null
    open val neutralText: CharSequence? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root){ view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val extraPadding = resources.getDimension(R.dimen.padding_16).toInt()
            view.updatePadding(bottom = bottomInset + extraPadding)
            insets
        }
        val accentColor = monet.getAccentColor(requireContext())
        binding.bottomSheetPositive.setTextColor(accentColor)
        binding.bottomSheetNegative.setTextColor(accentColor)
        binding.bottomSheetNeutral.setTextColor(accentColor)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomSheetTitle.text = title
        binding.bottomSheetContent.text = content
        if(positiveText != null){
            binding.bottomSheetPositive.isVisible = true
            binding.bottomSheetPositive.text = positiveText
            binding.bottomSheetPositive.setOnClickListener {
                onPositiveClicked(dialog as BottomSheetDialog)
            }
        }
        if(negativeText != null){
            binding.bottomSheetNegative.isVisible = true
            binding.bottomSheetNegative.text = negativeText
            binding.bottomSheetNegative.setOnClickListener {
                onNegativeClicked(dialog as BottomSheetDialog)
            }
        }
        if(neutralText != null){
            binding.bottomSheetNeutral.isVisible = true
            binding.bottomSheetNeutral.text = neutralText
            binding.bottomSheetNeutral.setOnClickListener {
                onNeutralClicked(dialog as BottomSheetDialog)
            }
        }
    }

    open fun onPositiveClicked(dialog: BottomSheetDialog){
        dialog.dismiss()
    }

    open fun onNegativeClicked(dialog: BottomSheetDialog){
        dialog.dismiss()
    }

    open fun onNeutralClicked(dialog: BottomSheetDialog){
        dialog.dismiss()
    }

}