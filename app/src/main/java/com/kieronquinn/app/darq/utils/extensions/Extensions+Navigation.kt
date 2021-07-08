package com.kieronquinn.app.darq.utils.extensions

import androidx.navigation.NavController
import androidx.navigation.NavDirections

fun NavController.navigateSafely(directions: NavDirections){
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions) }
}

fun NavController.navigateSafely(actionId: Int){
    currentDestination?.getAction(actionId)?.let { navigate(actionId) }
}