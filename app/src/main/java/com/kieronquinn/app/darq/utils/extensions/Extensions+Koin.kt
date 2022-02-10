package com.kieronquinn.app.darq.utils.extensions

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition

inline fun <reified T : ViewModel> Fragment.navGraphViewModel(
    @IdRes navGraphId: Int,
    navController: NavController? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val navBackStackEntry = (navController ?: findNavController()).getBackStackEntry(navGraphId)
        GlobalContext.get().getViewModel(
            owner = { ViewModelOwner.from(navBackStackEntry, navBackStackEntry) }, parameters = parameters
        )
    }
}