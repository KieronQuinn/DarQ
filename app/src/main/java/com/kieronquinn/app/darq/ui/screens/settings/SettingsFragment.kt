package com.kieronquinn.app.darq.ui.screens.settings

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.*
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kieronquinn.app.darq.BuildConfig
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.databinding.FragmentSettingsBinding
import com.kieronquinn.app.darq.model.settings.SettingsItem
import com.kieronquinn.app.darq.model.xposed.XposedSelfHooks
import com.kieronquinn.app.darq.ui.base.AutoExpandOnRotate
import com.kieronquinn.app.darq.ui.base.ProvidesOverflow
import com.kieronquinn.monetcompat.extensions.views.applyMonetRecursively
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseSettingsFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate), AutoExpandOnRotate, ProvidesOverflow {

    private val viewModel by viewModel<SettingsViewModel>()
    override val settingsItems by lazy {
        listOf(
            SettingsItem.Setting(
                R.drawable.ic_app_whitelist_round,
                getString(R.string.item_whitelist_title),
                getString(R.string.item_whitelist_content),
                tapAction = viewModel::onAppWhitelistClicked
            ),
            SettingsItem.Header(getString(R.string.item_header_options)),
            SettingsItem.SwitchSetting(
                R.drawable.ic_oxygen_round,
                getString(R.string.item_oxygen_force_title),
                getString(R.string.item_oxygen_force_content),
                settings::oxygenForceDark,
                visible = { viewModel.isOxygenForceDarkSupported() }
            ),
            SettingsItem.SwitchSetting(
                R.drawable.ic_day_night_auto_round,
                getString(R.string.item_day_night_auto_title),
                getString(R.string.item_day_night_auto_content),
                settings::autoDarkTheme,
                tapAction = { checked ->
                    viewModel.onAutoDarkThemeCheckedChange(checked, sharedViewModel)
                }
            ),
            SettingsItem.Setting(
                R.drawable.ic_advanced_round,
                getString(R.string.item_advanced_options_title),
                getString(R.string.item_advanced_options_content),
                tapAction = viewModel::onAdvancedOptionsClicked
            ),
            SettingsItem.Setting(
                R.drawable.ic_xposed_round,
                getString(R.string.item_xposed_title),
                getString(R.string.item_xposed_content),
                visible = { XposedSelfHooks.isXposedModuleEnabled() },
                tapAction = viewModel::onXposedClicked
            ),
            SettingsItem.Setting(
                R.drawable.ic_restore_round,
                getString(R.string.item_backup_restore_title),
                getString(R.string.item_backup_restore_content),
                tapAction = viewModel::onBackupRestoreClicked
            ),
            SettingsItem.Setting(
                R.drawable.ic_developer_options_round,
                getString(R.string.item_developer_options_title),
                visible = { settings.developerOptions },
                tapAction = viewModel::onDeveloperOptionsClicked
            ),
            SettingsItem.Header(getString(R.string.item_header_about)),
            SettingsItem.Setting(
                R.drawable.ic_faq_round,
                getString(R.string.item_about_faq_title),
                tapAction = viewModel::onFaqClicked
            ),
            SettingsItem.AboutSetting(
                R.drawable.ic_about_round,
                getString(R.string.item_about_title, BuildConfig.VERSION_NAME),
                getString(R.string.item_about_content),
                tripleTapAction = viewModel::onAboutTripleTapped
            )
        ).toMutableList()
    }

    private val adapter by lazy {
        SettingsAdapter(requireContext(), settingsItems)
    }

    private val googleSans by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.google_sans_text)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.applyMonetRecursively()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMainSwitch()
        setupRecyclerView(binding.recyclerView, adapter)
        setupSnackbarPadding(binding.recyclerView)
        setupAutoDarkTheme()
        setupDeveloperOptions()
        setupRestoreListener()
    }

    private fun setupMainSwitch() {
        binding.switchMain.run {
            mainSwitchSwitch.typeface = ResourcesCompat.getFont(requireContext(), R.font.google_sans_text_medium)
            updateMainSwitch()
        }
        lifecycleScope.launch {
            sharedViewModel.switchWarning.collect {
                val update = {
                    binding.switchMain.mainSwitchSwitch.text = getMainSwitchLabel(it)
                }
                if(!isAdded){
                    lifecycleScope.launchWhenResumed {
                        update.invoke()
                    }
                }else{
                    update.invoke()
                }
            }
        }
    }

    private fun updateMainSwitch() = with(binding.switchMain) {
        mainSwitchSwitch.setOnCheckedChangeListener(null)
        mainSwitchSwitch.isChecked = settings.enabled
        mainSwitchSwitch.setOnCheckedChangeListener { _, isChecked ->
            settings.enabled = isChecked
        }
    }

    //Fragment-level cache to prevent flickering
    private var isAutoDarkChecked: Boolean? = null
    private fun setupAutoDarkTheme(){
        lifecycleScope.launchWhenResumed {
            sharedViewModel.autoDarkTheme.collect {
                if(isAutoDarkChecked == it) return@collect
                isAutoDarkChecked = it
                adapter.notifyItemChanged(2)
            }
        }
    }

    private fun setupDeveloperOptions(){
        lifecycleScope.launchWhenResumed {
            viewModel.developerOptionsVisible.collect {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getMainSwitchLabel(@StringRes subtitle: Int?): CharSequence {
        return if(subtitle == null){
            getString(R.string.item_switch_main_title)
        }else{
            val sizeSpan = RelativeSizeSpan(0.75f)
            val fontSpan = TypefaceSpan(googleSans ?: Typeface.DEFAULT)
            val secondLine = SpannableString(getString(subtitle)).apply {
                setSpan(sizeSpan, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                setSpan(fontSpan, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
            SpannableStringBuilder().apply {
                appendLine(getString(R.string.item_switch_main_title))
                append(secondLine)
            }
        }
    }

    private fun setupRestoreListener(){
        lifecycleScope.launchWhenResumed {
            sharedViewModel.restoreBus.collect {
                adapter.notifySwitchSettings()
                updateMainSwitch()
            }
        }
    }

    override fun inflateMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.menu_open_source_libraries -> {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.menu_open_source_libraries))
                viewModel.onOssLicencesClicked()
            }
        }
        return true
    }

}