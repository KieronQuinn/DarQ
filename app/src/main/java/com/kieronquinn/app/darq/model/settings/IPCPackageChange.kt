package com.kieronquinn.app.darq.model.settings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IPCPackageChange(val packageName: String, val enabled: Boolean): Parcelable {
    override fun toString(): String {
        return "IPCPackageChange packageName=$packageName enabled=$enabled"
    }
}