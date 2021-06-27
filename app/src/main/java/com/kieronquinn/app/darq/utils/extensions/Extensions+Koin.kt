package com.kieronquinn.app.darq.utils.extensions

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.androidx.viewmodel.scope.BundleDefinition
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

inline fun <reified T : ViewModel> Fragment.navGraphViewModel(
    @IdRes navGraphId: Int,
    navController: NavController? = null,
    qualifier: Qualifier? = null,
    noinline state: BundleDefinition? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        val navBackStackEntry = (navController ?: findNavController()).getBackStackEntry(navGraphId)

        require(navBackStackEntry.destination is NavGraph) {
            ("No NavGraph with ID $navGraphId is on the NavController's back stack")
        }

        GlobalContext.get().getViewModel(qualifier, state, { ViewModelOwner.from(navBackStackEntry, navBackStackEntry) }, T::class, parameters)
    }
}