package com.kieronquinn.app.darq

import android.app.Application
import com.kieronquinn.app.darq.utils.AppIconRequestHandler
import com.squareup.picasso.Picasso
import com.topjohnwu.superuser.Shell.Config.*
import com.topjohnwu.superuser.Shell.FLAG_REDIRECT_STDERR


class DarqApplication : Application() {

    init {
        /* Shell.Config methods shall be called before any shell is created
     * This is the why in this example we call it in a static block
     * The followings are some examples, check Javadoc for more details */
        setFlags(FLAG_REDIRECT_STDERR);
        verboseLogging(BuildConfig.DEBUG);
        setTimeout(10);
    }

    var hasCheckedRoot = false
    var isRoot = false
    var uid: String? = null

    override fun onCreate() {
        super.onCreate()
        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .addRequestHandler(AppIconRequestHandler(this))
                .build()
        )
    }

}