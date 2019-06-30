package com.kieronquinn.app.darq.interfaces

import com.kieronquinn.app.darq.holders.App

interface ActivityCallbacks {

    fun onAppListLoaded(apps : List<App>)

    fun onEnabledAppListLoaded(pNames : List<String>)

}