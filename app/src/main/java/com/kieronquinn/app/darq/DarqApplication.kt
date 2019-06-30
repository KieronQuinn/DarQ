package com.kieronquinn.app.darq

import android.app.Application
import com.kieronquinn.app.darq.utils.AppIconRequestHandler
import com.squareup.picasso.Picasso



class DarqApplication : Application() {

    var hasCheckedRoot = false

    override fun onCreate() {
        super.onCreate()
        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .addRequestHandler(AppIconRequestHandler(this))
                .build()
        )
    }

}