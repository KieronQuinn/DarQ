package com.kieronquinn.app.darq.ui.screens.container

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuInflater
import android.view.View
import android.view.animation.Animation
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.components.navigation.Navigation
import com.kieronquinn.app.darq.components.settings.DarqSharedPreferences
import com.kieronquinn.app.darq.databinding.FragmentContainerBinding
import com.kieronquinn.app.darq.model.darq.DarqConstants
import com.kieronquinn.app.darq.providers.DarqServiceConnectionProvider
import com.kieronquinn.app.darq.service.autodark.DarqAutoDarkForegroundService
import com.kieronquinn.app.darq.ui.base.*
import com.kieronquinn.app.darq.utils.extensions.*
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import rikka.shizuku.Shizuku
import kotlin.math.roundToInt

class ContainerFragment: BoundFragment<FragmentContainerBinding>(FragmentContainerBinding::inflate) {

    internal val settings by inject<DarqSharedPreferences>()

    private val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    private val navController by lazy {
        navHostFragment.navController
    }

    private val sharedViewModel by lazy {
        navGraphViewModel<ContainerSharedViewModel>(R.id.nav_graph_main, navController).value
    }

    private val navigation by inject<Navigation>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackground()
        setupInsets()
        setupToolbar()
        setupFragmentListener()
        setupNavigation()
        setupLoading()
        setupSnackbar()
        setupSettingsListener()
        setupAutoDarkService()
        setupUpdateChecker()
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            lifecycleScope.launch {
                navigation.navigateBack()
            }
        }
    }

    private fun setupBackground(){
        binding.root.setBackgroundColor(monet.getBackgroundColor(requireContext()))
    }

    private fun setupInsets(){
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(left = navigationInsets.left, right = navigationInsets.right)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.collapsingToolbar){ view, insets ->
            view.updateLayoutParams<AppBarLayout.LayoutParams> {
                val appBarHeight = view.context.resources.getDimension(R.dimen.app_bar_height)
                height = (appBarHeight + insets.getInsets(WindowInsetsCompat.Type.statusBars()).top).roundToInt()
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar){ view, insets ->
            getToolbarHeight()?.let {
                val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                val overflowPadding = resources.getDimension(R.dimen.padding_8)
                val topInset = statusInsets.top
                view.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                    height = it + topInset
                }
                view.updatePadding(left = statusInsets.left, top = topInset, right = statusInsets.right + overflowPadding.toInt())
            }
            insets
        }
    }

    private fun getToolbarHeight(): Int? {
        val typedValue = TypedValue()
        return if (requireContext().theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        }else null
    }

    private fun setupToolbar(){
        with(binding){
            collapsingToolbar.title = getString(R.string.app_name)
            appBar.setExpanded(!requireContext().isLandscape && getTopFragment() is AutoExpandOnRotate)
            toolbar.setNavigationOnClickListener {
                lifecycleScope.launchWhenResumed {
                    navigation.navigateBack()
                }
            }
            collapsingToolbar.setBackgroundColor(monet.getBackgroundColor(requireContext()))
            collapsingToolbar.setContentScrimColor(monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext()))
            collapsingToolbar.setExpandedTitleTypeface(ResourcesCompat.getFont(requireContext(), R.font.google_sans_text_medium))
            collapsingToolbar.setCollapsedTitleTypeface(ResourcesCompat.getFont(requireContext(), R.font.google_sans_text_medium))
            toolbar.overflowIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.toolbar_overflow))
        }
    }

    private fun setupFragmentListener(){
        navHostFragment.childFragmentManager.addOnBackStackChangedListener {
            getTopFragment()?.let {
                onTopFragmentChanged(it)
            }
        }
        binding.navHostFragment.post {
            onTopFragmentChanged(getTopFragment() ?: return@post)
        }
    }

    private fun setupNavigation() = lifecycleScope.launchWhenResumed {
        navigation.navigationBus.collect {
            handleNavigationEvent(it)
        }
    }

    private fun handleNavigationEvent(navigationEvent: Navigation.NavigationEvent) {
        when (navigationEvent) {
            is Navigation.NavigationEvent.Directions -> navController.navigateSafely(navigationEvent.directions)
            is Navigation.NavigationEvent.Id -> navController.navigateSafely(navigationEvent.id)
            is Navigation.NavigationEvent.Back -> if(!navController.navigateUp()) activity?.finish()
            is Navigation.NavigationEvent.PopupTo -> navController.popBackStack(
                navigationEvent.id,
                navigationEvent.popInclusive
            )
        }
        if(activity?.isFinishing != true) {
            binding.appBar.setExpanded(true, true)
            activity?.hideKeyboard()
        }
    }

    private fun setupSnackbar(){
        ViewCompat.setOnApplyWindowInsetsListener(binding.snackbarContainer){ view, insets ->
            val bottomInsets = insets.getInsets(WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = bottomInsets)
            insets
        }
        val background = monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext())
        binding.snackbar.backgroundTintList = ColorStateList.valueOf(background)
        binding.snackbarRoot.snackbarProgress.applyMonet()
        lifecycleScope.launchWhenResumed {
            launch {
                sharedViewModel.showSnackbar.collect {
                    setSnackbarVisibility(it)
                }
            }
            launch {
                sharedViewModel.syncState.debounce(DarqConstants.IPC_SYNC_TIMEOUT_LONG).collect {
                    setSnackbarState(it)
                }
            }
        }
    }

    private fun setupLoading() = lifecycleScope.launchWhenResumed {
        //Debounce by 100ms to prevent the 'flash' of the loading screen when resuming and the service is already connected
        sharedViewModel.loadingState.debounce(DarqConstants.IPC_SYNC_TIMEOUT).collect {
            handleServiceState(it)
        }
    }

    private suspend fun handleServiceState(serviceState: ContainerSharedViewModel.ServiceState) {
        binding.loadingFragment.isVisible = serviceState is ContainerSharedViewModel.ServiceState.Loading
        binding.navHostFragment.isVisible = serviceState is ContainerSharedViewModel.ServiceState.Loaded
        if(serviceState is ContainerSharedViewModel.ServiceState.Error){
            when(serviceState.error){
                DarqServiceConnectionProvider.ServiceFailureReason.SHIZUKU_PERMISSION_REQUIRED -> {
                    if(!requestShizukuPermission()){
                        //Shizuku not running
                        showShizukuNotRunningSheet()
                    }
                }
                DarqServiceConnectionProvider.ServiceFailureReason.SHIZUKU_NOT_INSTALLED -> {
                    showShizukuNotInstalledSheet()
                }
                DarqServiceConnectionProvider.ServiceFailureReason.SHIZUKU_NOT_STARTED -> {
                    showShizukuNotRunningSheet()
                }
                DarqServiceConnectionProvider.ServiceFailureReason.TIMEOUT -> {
                    showTimeoutSheet()
                }
            }
        }
    }

    private fun onTopFragmentChanged(topFragment: Fragment){
        val backIcon = if(topFragment is BackAvailable){
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
        } else null
        if(topFragment is ProvidesOverflow){
            setupMenu(topFragment)
        }else{
            setupMenu(null)
        }
        binding.toolbar.navigationIcon = backIcon
        navController.currentDestination?.label?.let {
            if(it.isBlank()) return@let
            binding.collapsingToolbar.title = it
            binding.toolbar.title = it
        }
    }

    private fun setupMenu(menuProvider: ProvidesOverflow?){
        val menu = binding.toolbar.menu
        val menuInflater = MenuInflater(requireContext())
        menu.clear()
        menuProvider?.inflateMenu(menuInflater, menu)
        binding.toolbar.setOnMenuItemClickListener {
            menuProvider?.onMenuItemSelected(it) ?: false
        }
    }

    private fun getTopFragment(): Fragment? {
        if(!navHostFragment.isAdded) return null
        return navHostFragment.childFragmentManager.fragments.firstOrNull()
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.loadService()
    }

    private var snackbarAnimation: Animation? = null

    private fun setSnackbarVisibility(visible: Boolean){
        snackbarAnimation?.cancel()
        snackbarAnimation = if(visible) binding.snackbarContainer.slideIn {  }
        else binding.snackbarContainer.slideOut {  }
    }

    private fun setSnackbarState(syncState: ContainerSharedViewModel.SyncState){
        when(syncState){
            is ContainerSharedViewModel.SyncState.Syncing -> {
                binding.snackbarRoot.snackbarText.text = getString(R.string.snackbar_syncing_text)
                binding.snackbarRoot.snackbarAction.isVisible = false
                binding.snackbarRoot.snackbarProgress.isVisible = true
            }
            is ContainerSharedViewModel.SyncState.SyncComplete -> {
                binding.snackbarRoot.snackbarProgress.isVisible = false
                if(syncState.success){
                    binding.snackbarRoot.snackbarText.text = getString(R.string.snackbar_syncing_complete_text)
                    binding.snackbarRoot.snackbarAction.isVisible = false
                }else{
                    binding.snackbarRoot.snackbarText.text = getString(R.string.snackbar_syncing_failed_text)
                    binding.snackbarRoot.snackbarAction.text = getString(R.string.snackbar_syncing_failed_button)
                    binding.snackbarRoot.snackbarAction.isVisible = true
                }
            }
        }
    }

    private fun setupSettingsListener(){
        lifecycleScope.launchWhenResumed {
            settings.changed.collect {
                sharedViewModel.queueIPCSync(it)
            }
        }
    }

    private fun setupAutoDarkService(){
        lifecycleScope.launchWhenResumed {
            sharedViewModel.autoDarkServiceStartBus.collect {
                requireContext().startForegroundService(Intent(requireContext(), DarqAutoDarkForegroundService::class.java).apply {
                    putExtra(DarqAutoDarkForegroundService.KEY_JUST_RESCHEDULE, true)
                })
            }
        }
    }

    fun expandAppBar(){
        binding.appBar.setExpanded(true, true)
    }

    private fun requestShizukuPermission(): Boolean {
        return try {
            Shizuku.requestPermission(0)
            true
        }catch (e: IllegalStateException){
            //Shizuku failed to respond = not running
            false
        }
    }

    private fun setupUpdateChecker(){
        lifecycleScope.launchWhenResumed {
            sharedViewModel.update.collect {
                if(it != null){
                    navigation.navigate(R.id.action_global_updateAvailableBottomSheetFragment)
                }
            }
        }
    }

    private fun showShizukuNotRunningSheet() = lifecycleScope.launchWhenResumed {
        navigation.navigate(R.id.action_global_shizkuNotRunningBottomSheetFragment)
    }

    private fun showTimeoutSheet() = lifecycleScope.launchWhenResumed {
        navigation.navigate(R.id.action_global_serviceTimeoutBottomSheetFragment)
    }

    private fun showShizukuNotInstalledSheet() = lifecycleScope.launchWhenResumed {
        navigation.navigate(R.id.action_global_shizukuNotInstalledBottomSheetFragment)
    }

}