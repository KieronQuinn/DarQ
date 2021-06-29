package com.kieronquinn.app.darq

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import com.kieronquinn.app.darq.components.github.UpdateChecker
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.components.navigation.NavigationImpl
import com.kieronquinn.app.darq.components.settings.AppSharedPreferences
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.providers.blur.BlurProvider
import com.kieronquinn.app.darq.ui.screens.bottomsheets.update.UpdateDownloadBottomSheetViewModel
import com.kieronquinn.app.darq.ui.screens.bottomsheets.update.UpdateDownloadBottomSheetViewModelImpl
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.darq.ui.screens.container.ContainerSharedViewModelImpl
import com.kieronquinn.app.darq.ui.screens.dialogs.LocationPermissionDialogViewModel
import com.kieronquinn.app.darq.ui.screens.dialogs.LocationPermissionDialogViewModelImpl
import com.kieronquinn.app.darq.ui.screens.settings.SettingsViewModel
import com.kieronquinn.app.darq.ui.screens.settings.SettingsViewModelImpl
import com.kieronquinn.app.darq.ui.screens.settings.apppicker.SettingsAppPickerViewModel
import com.kieronquinn.app.darq.ui.screens.settings.apppicker.SettingsAppPickerViewModelImpl
import com.kieronquinn.app.darq.ui.screens.settings.developer.SettingsDeveloperOptionsViewModel
import com.kieronquinn.app.darq.ui.screens.settings.developer.SettingsDeveloperOptionsViewModelImpl
import com.kieronquinn.app.darq.utils.AppIconRequestHandler
import com.kieronquinn.monetcompat.core.MonetCompat
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.sui.Sui

class DarqApplication : Application() {

    private val serviceModule = module {
        single { DarqServiceConnectionProvider(get(), get()) }
    }

    private val appComponentsModule = module {
        single<DarqSharedPreferences> { AppSharedPreferences(get()) }
        single<Navigation> { NavigationImpl() }
        single { UpdateChecker() }
    }

    private val viewModelsModule = module {
        viewModel<ContainerSharedViewModel>{ ContainerSharedViewModelImpl(get(), get(), get(), get()) }
        viewModel<SettingsViewModel>{ SettingsViewModelImpl(get(), get()) }
        viewModel<SettingsAppPickerViewModel>{ SettingsAppPickerViewModelImpl(get(), get()) }
        viewModel<SettingsDeveloperOptionsViewModel>{ SettingsDeveloperOptionsViewModelImpl(get()) }
        viewModel<UpdateDownloadBottomSheetViewModel>{ UpdateDownloadBottomSheetViewModelImpl(getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager) }
        viewModel<LocationPermissionDialogViewModel> { LocationPermissionDialogViewModelImpl(get(), get()) }
    }

    private val providersModule = module {
        single { BlurProvider.getBlurProvider(resources) }
    }

    override fun onCreate() {
        super.onCreate()
        runCatching {
            //Allows blurs on some devices, doesn't really matter if it fails
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .addRequestHandler(AppIconRequestHandler(this))
                .build()
        )
        startKoin {
            androidContext(this@DarqApplication)
            modules(serviceModule, appComponentsModule, viewModelsModule, providersModule)
        }
        Sui.init(packageName)
        setupMonet()
    }

    private fun setupMonet(){
        val settings = get<DarqSharedPreferences>()
        MonetCompat.wallpaperColorPicker = {
            val selectedColor = settings.monetColor
            if(selectedColor != Integer.MAX_VALUE && it?.contains(selectedColor) == true) selectedColor
            else it?.firstOrNull()
        }
    }

}