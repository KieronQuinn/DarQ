package com.kieronquinn.app.darq.components.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavDirections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class Navigation {

    abstract val navigationBus: Flow<NavigationEvent>

    abstract suspend fun navigate(navDirections: NavDirections)
    abstract suspend fun navigate(@IdRes id: Int)
    abstract suspend fun navigateUpTo(@IdRes id: Int, popInclusive: Boolean = false)
    abstract suspend fun navigateBack()

    sealed class NavigationEvent {
        data class Directions(val directions: NavDirections): NavigationEvent()
        data class Id(@IdRes val id: Int): NavigationEvent()
        data class PopupTo(@IdRes val id: Int, val popInclusive: Boolean): NavigationEvent()
        object Back: NavigationEvent()
    }

}

class NavigationImpl: Navigation() {

    private val _navigationBus = MutableSharedFlow<NavigationEvent>()
    override val navigationBus = _navigationBus.asSharedFlow()

    override suspend fun navigate(id: Int) {
        _navigationBus.emit(NavigationEvent.Id(id))
    }

    override suspend fun navigate(navDirections: NavDirections) {
        _navigationBus.emit(NavigationEvent.Directions(navDirections))
    }

    override suspend fun navigateBack() {
        _navigationBus.emit(NavigationEvent.Back)
    }

    override suspend fun navigateUpTo(id: Int, popInclusive: Boolean) {
        _navigationBus.emit(NavigationEvent.PopupTo(id, popInclusive))
    }

}