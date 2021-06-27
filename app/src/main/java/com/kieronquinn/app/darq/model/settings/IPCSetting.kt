package com.kieronquinn.app.darq.model.settings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IPCSetting(
    val enabled: Boolean? = null,
    val oxygenForceDark: Boolean? = null,
    val alwaysForceDark: Boolean? = null,
    val sendAppCloses: Boolean? = null,
    val packageChange: IPCPackageChange? = null
): Parcelable {
    override fun toString(): String {
        return "IPCSetting enabled=$enabled oxygenForceDark=$oxygenForceDark sendAppCloses=$sendAppCloses packageChange=[$packageChange]"
    }
}
