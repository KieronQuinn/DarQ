package com.kieronquinn.app.darq.model.settings

data class SettingsBackup(
    val enabled: Boolean,
    val autoDarkTheme: Boolean,
    //useLocation is backed up but not restored automatically - it shows a prompt first
    val useLocation: Boolean,
    val sendAppCloses: Boolean,
    val oxygenForceDark: Boolean,
    val alwaysForceDark: Boolean,
    val developerOptions: Boolean,
    val monetColor: Int,
    val xposedAggressiveDark: Boolean,
    val xposedInvertStatus: Boolean,
    val enabledApps: List<String>
)