package com.kieronquinn.app.darq.ui.screens.loading

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.darq.databinding.FragmentLoadingServiceConnectBinding
import com.kieronquinn.app.darq.ui.base.AutoExpandOnRotate
import com.kieronquinn.app.darq.ui.base.BoundFragment
import com.kieronquinn.monetcompat.extensions.views.applyMonet

class ServiceConnectFragment: BoundFragment<FragmentLoadingServiceConnectBinding>(FragmentLoadingServiceConnectBinding::inflate), AutoExpandOnRotate {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingProgress.applyMonet()
    }

}