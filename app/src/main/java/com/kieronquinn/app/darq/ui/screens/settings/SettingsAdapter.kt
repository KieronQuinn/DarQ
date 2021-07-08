package com.kieronquinn.app.darq.ui.screens.settings

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.darq.databinding.*
import com.kieronquinn.app.darq.model.settings.SettingsItem
import com.kieronquinn.app.darq.model.settings.SettingsItemType
import com.kieronquinn.app.darq.utils.Links
import com.kieronquinn.app.darq.ui.utils.MultiTapDetector
import com.kieronquinn.app.darq.utils.openLink
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonet

class SettingsAdapter(context: Context, private var items: List<SettingsItem>): RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    private val visibleItems
        get() = items.filter { it.visible.invoke() }

    override fun getItemCount(): Int = visibleItems.size

    override fun getItemViewType(position: Int): Int {
        return visibleItems[position].itemType.ordinal
    }

    override fun getItemId(position: Int): Long {
        return when(val item = visibleItems[position]){
            is SettingsItem.SwitchSetting -> item.title.hashCode().toLong()
            is SettingsItem.Setting -> item.title.hashCode().toLong()
            is SettingsItem.AboutSetting -> item.title.hashCode().toLong()
            is SettingsItem.Header -> item.title.hashCode().toLong()
            is SettingsItem.SnackbarPadding -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(SettingsItemType.values()[viewType]){
            SettingsItemType.HEADER -> ViewHolder.Header(ItemHeaderBinding.inflate(layoutInflater, parent, false))
            SettingsItemType.SETTING -> ViewHolder.SettingsSetting(ItemSettingBinding.inflate(layoutInflater, parent, false))
            SettingsItemType.ABOUT_SETTING -> ViewHolder.SettingsAboutSetting(ItemSettingAboutBinding.inflate(layoutInflater, parent, false))
            SettingsItemType.SWITCH_SETTING -> ViewHolder.SettingsSwitchSetting(ItemSettingSwitchBinding.inflate(layoutInflater, parent, false))
            SettingsItemType.SNACKBAR_PADDING -> ViewHolder.SnackbarPadding(ItemSnackbarPaddingBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = visibleItems[holder.adapterPosition]
        when(holder){
            is ViewHolder.Header -> setupHeader(holder.binding, item as SettingsItem.Header)
            is ViewHolder.SettingsSetting -> setupSetting(holder.binding, item as SettingsItem.Setting)
            is ViewHolder.SettingsAboutSetting -> setupTripleTapActionSetting(holder.binding, item as SettingsItem.AboutSetting)
            is ViewHolder.SettingsSwitchSetting -> setupSettingSwitch(holder.binding, item as SettingsItem.SwitchSetting)
            is ViewHolder.SnackbarPadding -> {}
        }
    }

    private fun setupHeader(binding: ItemHeaderBinding, item: SettingsItem.Header) = with(binding) {
        itemHeadingTitle.text = item.title
        itemHeadingTitle.setTextColor(monet.getAccentColor(itemHeadingTitle.context))
    }

    private fun setupSetting(binding: ItemSettingBinding, item: SettingsItem.Setting) = with(binding) {
        itemSettingTitle.text = item.title
        if(item.content.isNullOrEmpty()){
            itemSettingContent.isVisible = false
        }else{
            itemSettingContent.isVisible = true
            itemSettingContent.text = item.content
        }
        if(item.icon != 0) {
            itemSettingIcon.setImageResource(item.icon)
        }else{
            itemSettingIcon.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        item.tapAction?.let { action ->
            root.setOnClickListener {
                action.invoke()
            }
        }
        if(!item.centerIconVertically){
            binding.root.gravity = Gravity.NO_GRAVITY
        }
    }

    private fun setupTripleTapActionSetting(binding: ItemSettingAboutBinding, item: SettingsItem.AboutSetting) = with(binding) {
        itemSettingTitle.text = item.title
        if(item.content.isNullOrEmpty()){
            itemSettingContent.isVisible = false
        }else{
            itemSettingContent.isVisible = true
            itemSettingContent.text = item.content
        }
        if(item.icon != 0) {
            itemSettingIcon.setImageResource(item.icon)
        }else{
            itemSettingIcon.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        item.tripleTapAction?.let { action ->
            MultiTapDetector(root){ tapCount, lastTap ->
                if(lastTap && tapCount >= 3){
                    action.invoke()
                }
            }
        }
        binding.chipDonate.setOnClickListener {
            it.context.openLink(Links.LINK_DONATE)
        }
        binding.chipTwitter.setOnClickListener {
            it.context.openLink(Links.LINK_TWITTER)
        }
        binding.chipGithub.setOnClickListener {
            it.context.openLink(Links.LINK_GITHUB)
        }
        binding.chipXda.setOnClickListener {
            it.context.openLink(Links.LINK_XDA)
        }
    }

    private fun setupSettingSwitch(binding: ItemSettingSwitchBinding, item: SettingsItem.SwitchSetting) = with(binding) {
        itemSettingSwitchTitle.text = item.title
        itemSettingSwitchSwitch.applyMonet()
        if(item.content.isNullOrEmpty()){
            itemSettingSwitchContent.isVisible = false
        }else{
            itemSettingSwitchContent.isVisible = true
            itemSettingSwitchContent.text = item.content
        }
        if(item.icon != 0) {
            itemSettingSwitchIcon.setImageResource(item.icon)
        }else{
            itemSettingSwitchIcon.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        itemSettingSwitchSwitch.setOnCheckedChangeListener(null)
        itemSettingSwitchSwitch.isChecked = item.setting.get()
        itemSettingSwitchSwitch.setOnCheckedChangeListener { button, isChecked ->
            if(item.tapAction != null){
                if(item.tapAction.invoke(isChecked)){
                    item.setting.set(isChecked)
                }else{
                    button.isChecked = !isChecked
                }
            }else {
                item.setting.set(isChecked)
            }
        }
        root.setOnClickListener {
            itemSettingSwitchSwitch.toggle()
        }
        if(!item.centerIconVertically){
            binding.root.gravity = Gravity.NO_GRAVITY
        }
    }

    fun notifySwitchSettings(){
        items.forEachIndexed { index, settingsItem ->
            if(settingsItem is SettingsItem.SwitchSetting) {
                notifyItemChanged(index)
            }
        }
    }

    sealed class ViewHolder(open val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {
        data class Header(override val binding: ItemHeaderBinding): ViewHolder(binding)
        data class SettingsSetting(override val binding: ItemSettingBinding): ViewHolder(binding)
        data class SettingsAboutSetting(override val binding: ItemSettingAboutBinding): ViewHolder(binding)
        data class SettingsSwitchSetting(override val binding: ItemSettingSwitchBinding): ViewHolder(binding)
        data class SnackbarPadding(override val binding: ItemSnackbarPaddingBinding): ViewHolder(binding)
    }

}