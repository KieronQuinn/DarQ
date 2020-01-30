package com.kieronquinn.app.darq.fragments

import android.Manifest
import android.app.UiModeManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kieronquinn.app.darq.BuildConfig
import com.kieronquinn.app.darq.MainActivity
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.holders.App
import com.kieronquinn.app.darq.interfaces.ActivityCallbacks
import com.kieronquinn.app.darq.preferences.Preference
import com.kieronquinn.app.darq.preferences.SwitchPreference
import com.kieronquinn.app.darq.services.DarqBackgroundService
import com.kieronquinn.app.darq.services.DarqBackgroundService.Companion.BROADCAST_SET_FORCE_DARK_SUCCESS
import com.kieronquinn.app.darq.utils.Links
import com.kieronquinn.app.darq.utils.getIsForceDarkTheme
import com.kieronquinn.app.darq.utils.isDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kieronquinn.app.darq.utils.isRoot


class MainFragment : PreferenceFragmentCompat(), ActivityCallbacks {

    private val job = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val onAutoDarkCheckedChangeListener = object: androidx.preference.Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(preference: androidx.preference.Preference?, newValue: Any?): Boolean {
            if(newValue == true) {
                if (activity?.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    showAutoDarkBottomSheet(preference as SwitchPreference)
                    return false
                }
            }
            return true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)
        val darkModePreference = findPreference<SwitchPreferenceCompat>("switch_dark_mode")
        val forceDarkModePreference = findPreference<SwitchPreferenceCompat>("switch_force_dark_mode")
        val autoDarkPreference = findPreference<SwitchPreferenceCompat>("switch_auto_night_dark")
        val oxygenOsPreference = findPreference<SwitchPreferenceCompat>("oxygen_os_toggle")
        val whiteListPreference = findPreference<Preference>("preference_whitelist")
        val faqAboutPreference = findPreference<Preference>("preference_faq_about")
        val debugPreference = findPreference<Preference>("debug")
        (activity as? MainActivity)?.let {
            it.activityCallbacks = this
            darkModePreference?.isChecked = isDarkTheme(it)
            darkModePreference?.onPreferenceClickListener = OnPreferenceClickListener {
                toggleDarkMode()
                true
            }
            forceDarkModePreference?.isChecked = getIsForceDarkTheme()
            forceDarkModePreference?.onPreferenceClickListener = OnPreferenceClickListener {
                toggleForceDarkMode(it as SwitchPreferenceCompat)
                true
            }
            whiteListPreference?.onPreferenceClickListener = OnPreferenceClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_appsFragment)
                true
            }
            debugPreference?.run {
                isVisible = BuildConfig.DEBUG
                setOnPreferenceClickListener {
                    activity?.let {activity ->
                        val isEnabled = isDarkTheme(activity)
                        val intent = Intent()
                        intent.action = DarqBackgroundService.BROADCAST_TEST
                        intent.`package` = activity.packageName
                        activity.sendBroadcast(intent)
                    }
                    true
                }
                summary = "Running with root: $isRoot"
            }
            autoDarkPreference?.onPreferenceChangeListener = onAutoDarkCheckedChangeListener
            faqAboutPreference?.setOnPreferenceClickListener { preference ->
                showFaqAbout(preference as Preference)
                true
            }
            oxygenOsPreference?.isVisible = Build.MANUFACTURER == "OnePlus"
        }

    }

    private fun toggleDarkMode() {
        activity?.let {
            val isEnabled = isDarkTheme(it)
            val intent = Intent()
            intent.action = DarqBackgroundService.BROADCAST_SET_DARK
            intent.putExtra(DarqBackgroundService.KEY_ENABLED, !isEnabled)
            intent.`package` = it.packageName
            it.sendBroadcast(intent)
        }
    }

    private fun toggleForceDarkMode(forceDarkModePreference: SwitchPreferenceCompat) {
        val isEnabled = getIsForceDarkTheme()
        forceDarkModePreference.isEnabled = false
        forceDarkModePreference.isChecked = !isEnabled
        //Register a receiver for success
        activity?.registerReceiver(object: BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                forceDarkModePreference.isEnabled = true
                forceDarkModePreference.isChecked = getIsForceDarkTheme()
                activity?.unregisterReceiver(this)
                showSnackbar(getString(R.string.setting_force_dark_mode_snackbar))
            }

        }, IntentFilter(BROADCAST_SET_FORCE_DARK_SUCCESS))
        activity?.let {
            val intent = Intent()
            intent.action = DarqBackgroundService.BROADCAST_SET_FORCE_DARK
            intent.putExtra(DarqBackgroundService.KEY_ENABLED, !isEnabled)
            intent.`package` = it.packageName
            it.sendBroadcast(intent)
        }
    }

    private fun showAutoDarkBottomSheet(switchPreference: SwitchPreference){
        val bottomSheetFragment = BottomSheetFragment()
        bottomSheetFragment.layout = R.layout.bottom_sheet_auto_dark_permission
        bottomSheetFragment.okLabel = R.string.bottom_sheet_auto_dark_location
        bottomSheetFragment.cancelLabel = R.string.bottom_sheet_auto_dark_timezone
        bottomSheetFragment.okListener = {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), 0)
            true
        }
        bottomSheetFragment.cancelListener = {
            switchPreference.onPreferenceChangeListener = null
            switchPreference.isChecked = true
            switchPreference.onPreferenceChangeListener = onAutoDarkCheckedChangeListener
            true
        }
        bottomSheetFragment.neutralListener = {
            true
        }
        bottomSheetFragment.isSwipeable = true
        bottomSheetFragment.showListener = {
            it.findViewById<TextView>(android.R.id.content).text = getString(R.string.bottom_sheet_auto_dark_desc, TimeZone.getDefault().id)
        }
        bottomSheetFragment.show(childFragmentManager, "bs_auto_dark")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            val autoDarkPreference = findPreference<SwitchPreferenceCompat>("switch_auto_night_dark")
            autoDarkPreference?.isChecked = true
        }else{
            view?.let {
                Snackbar.make(it, getString(R.string.permission_failed), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showSnackbar(message: String){
        view?.let {
            val snackbar = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            val snackBarView = snackbar.view
            snackBarView.setPadding(snackBarView.paddingLeft, snackBarView.paddingTop, snackBarView.paddingRight, snackBarView.paddingBottom + (MainActivity.navBarHeight ?: 0))
            snackbar.show()
        }
    }

    override fun onAppListLoaded(apps: List<App>) {

    }

    override fun onEnabledAppListLoaded(pNames: List<String>) {
        refreshEnabledApps(pNames)
        //Re-apply force dark toggle state (sometimes checks too soon after launch)
        val forceDarkModePreference = findPreference<SwitchPreferenceCompat>("switch_force_dark_mode")
        forceDarkModePreference?.isChecked = getIsForceDarkTheme()
    }

    private fun showFaqAbout(preference: Preference){
        preference.isVisible = false
        addPreferencesFromResource(R.xml.faq_about_preferences)
        context?.let {
            Links.setupPreference(it, preferenceScreen, "xda_thread", Links.LINK_XDA)
            Links.setupPreference(it, preferenceScreen, "twitter", Links.LINK_TWITTER)
            Links.setupPreference(it, preferenceScreen, "donate", Links.LINK_DONATE)
            Links.setupPreference(it, preferenceScreen, "github", Links.LINK_GITHUB)
            Links.setupPreference(it, preferenceScreen, "about", Links.LINK_XDA_LABS)
            val licences = findPreference<Preference>("licences")
            licences?.onPreferenceClickListener = OnPreferenceClickListener {
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                true
            }
            val faq = findPreference<Preference>("faq")
            faq?.onPreferenceClickListener = OnPreferenceClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_faqFragment)
                true
            }
            val about = findPreference<Preference>("about")
            about?.setSummary(getString(R.string.setting_about_desc, BuildConfig.VERSION_NAME))
        }
    }

    private fun refreshEnabledApps(pNames: List<String>){
        val preference = findPreference<Preference>("preference_whitelist")
        preference?.summary = resources.getQuantityString(R.plurals.setting_whitelist_desc, pNames.size, pNames.size)
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.preferenceUtils?.enabledApps?.let {
            refreshEnabledApps(it)
        }
        (view as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

}