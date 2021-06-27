package com.kieronquinn.app.darq.service.root

import android.content.Intent
import android.os.IBinder
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.service.impl.DarqService
import com.topjohnwu.superuser.ipc.RootService

class DarqRootService: RootService() {

    override fun onBind(intent: Intent): IBinder {
        return DarqService(DarqServiceConnectionProvider.ServiceType.ROOT)
    }

    override fun onUnbind(intent: Intent): Boolean {
        return true
    }

}