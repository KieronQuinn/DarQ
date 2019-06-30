package com.kieronquinn.app.darq.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.preference.PreferenceScreen
import com.kieronquinn.app.darq.preferences.Preference

class Links {
    companion object {
        const val LINK_GITHUB = "https://kieronquinn.co.uk/redirect/DarQ/github"
        const val LINK_XDA = "https://kieronquinn.co.uk/redirect/DarQ/xda"
        const val LINK_DONATE = "https://kieronquinn.co.uk/redirect/DarQ/donate"
        const val LINK_TWITTER = "https://kieronquinn.co.uk/redirect/DarQ/twitter"
        const val LINK_XDA_LABS = "https://kieronquinn.co.uk/redirect/DarQ/xdalabs"

        private fun startLinkIntent(context: Context, link: String){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            context.startActivity(intent)
        }

        fun setupPreference(context: Context, preferenceScreen: PreferenceScreen, preferenceKey: String, link: String){
            val preference = preferenceScreen.findPreference<Preference>(preferenceKey)
            preference?.onPreferenceClickListener = androidx.preference.Preference.OnPreferenceClickListener {
                startLinkIntent(context, link)
                true
            }
        }
    }
}