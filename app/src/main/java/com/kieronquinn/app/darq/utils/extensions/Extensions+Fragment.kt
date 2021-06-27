package com.kieronquinn.app.darq.utils.extensions

import androidx.fragment.app.Fragment
import com.kieronquinn.app.darq.ui.screens.container.ContainerFragment

fun Fragment.expandAppBar(){
    (parentFragment as? ContainerFragment)?.expandAppBar()
}