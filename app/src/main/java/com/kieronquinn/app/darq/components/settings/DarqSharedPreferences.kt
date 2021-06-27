package com.kieronquinn.app.darq.components.settings

import com.kieronquinn.app.darq.model.settings.IPCSetting
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

abstract class DarqSharedPreferences: BaseSharedPreferences() {

    companion object {

        private const val KEY_ENABLED = "enabled"
        private const val DEFAULT_ENABLED = true

        private const val KEY_AUTO_DARK_THEME = "auto_dark_theme"
        private const val DEFAULT_AUTO_DARK_THEME = false

        private const val KEY_SEND_APP_CLOSES = "send_app_closes"
        private const val DEFAULT_SEND_APP_CLOSES = false

        private const val KEY_OXYGEN_FORCE_DARK = "oxygen_force_dark"
        private const val DEFAULT_OXYGEN_FORCE_DARK = false

        private const val KEY_ALWAYS_FORCE_DARK = "always_force_dark"
        private const val DEFAULT_ALWAYS_FORCE_DARK = false

        private const val KEY_USE_LOCATION = "use_location"
        private const val DEFAULT_USE_LOCATION = false

        private const val KEY_DEVELOPER_OPTIONS = "developer_options"
        private const val DEFAULT_DEVELOPER_OPTIONS = false

        private const val KEY_UI_MONET_COLOR = "monet_color"

        private const val KEY_ENABLED_APPS = "enabled_apps"

    }

    abstract val changed: Flow<IPCSetting>

    var enabled by this.shared(KEY_ENABLED, DEFAULT_ENABLED)
    var autoDarkTheme by this.shared(KEY_AUTO_DARK_THEME, DEFAULT_AUTO_DARK_THEME)
    var sendAppCloses by this.shared(KEY_SEND_APP_CLOSES, DEFAULT_SEND_APP_CLOSES)
    var oxygenForceDark by this.shared(KEY_OXYGEN_FORCE_DARK, DEFAULT_OXYGEN_FORCE_DARK)
    var alwaysForceDark by this.shared(KEY_ALWAYS_FORCE_DARK, DEFAULT_ALWAYS_FORCE_DARK)
    var useLocation by this.shared(KEY_USE_LOCATION, DEFAULT_USE_LOCATION)
    var developerOptions by this.shared(KEY_DEVELOPER_OPTIONS, DEFAULT_DEVELOPER_OPTIONS)
    var monetColor by this.shared(KEY_UI_MONET_COLOR, Integer.MAX_VALUE)
    var enabledApps by this.sharedJSONArray(KEY_ENABLED_APPS)

    fun getIPCSettingForKey(key: String): IPCSetting? {
        return when(key){
            KEY_ENABLED -> IPCSetting(enabled = enabled)
            KEY_ALWAYS_FORCE_DARK -> IPCSetting(alwaysForceDark = alwaysForceDark)
            KEY_OXYGEN_FORCE_DARK -> IPCSetting(oxygenForceDark = oxygenForceDark)
            KEY_SEND_APP_CLOSES -> IPCSetting(sendAppCloses = sendAppCloses)
            else -> null
        }
    }

    fun toIPCSetting(): IPCSetting {
        return IPCSetting(enabled, oxygenForceDark, alwaysForceDark, sendAppCloses)
    }

    internal fun JSONArray.toStringArray(): Array<String> {
        return ArrayList<String>().apply {
            for (i in 0 until this@toStringArray.length()) {
                add(this@toStringArray.getString(i))
            }
        }.toTypedArray()
    }

}