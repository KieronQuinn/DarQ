package com.kieronquinn.app.darq.activities

import android.animation.Animator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.fragments.BottomSheetFragment
import com.kieronquinn.app.darq.holders.App
import com.kieronquinn.app.darq.interfaces.ActivityCallbacks
import com.kieronquinn.app.darq.root.DarqIPCReceiver
import com.kieronquinn.app.darq.services.DarqBackgroundService
import com.kieronquinn.app.darq.services.DarqBackgroundService.Companion.BROADCAST_PING
import com.kieronquinn.app.darq.services.DarqBackgroundService.Companion.BROADCAST_PONG
import com.kieronquinn.app.darq.utils.*
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_dark_mode_warning.*
import kotlinx.coroutines.*
import java.io.File
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    val allApps = ArrayList<App>()

    private var isWaitingForDismiss = false

    private var isWaitingToPause = false

    private var isDarkTheme: Boolean = false

    var activityCallbacks : ActivityCallbacks? = null

    var preferenceUtils: PreferenceUtils? = null

    private var currentBottomSheet: BottomSheetFragment? = null

    companion object {
        private var statusBarHeight: Int? = null
        var navBarHeight: Int? = null
        private var actionBarHeight: Int? = null
        private var INTENT_RELOAD = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceUtils = PreferenceUtils(this)
        isDarkTheme = isDarkTheme(this)
        if (isDarkTheme) setTheme(R.style.AppTheme_Dark)
        else setTheme(R.style.AppTheme_Light)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        adjustToolbar()
        supportActionBar?.title = ""
        checkRootAndSecure()
        setupLottieListener()
        setupNavController()
        dark_mode_warning.visibility = if(isDarkTheme) View.GONE else View.VISIBLE
    }

    private fun setupNavController() {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id){
                R.id.appsFragment -> {
                    home.visibility = View.VISIBLE
                }
                R.id.faqFragment -> {
                    home.visibility = View.VISIBLE
                }
                R.id.mainFragment -> {
                    home.visibility = View.GONE
                }
            }
            val dots = findViewById<ImageView>(R.id.menu)
            if(destination.id != R.id.mainFragment){
                dark_mode_warning.visibility = View.GONE
            }else{
                dots.visibility = View.GONE
                dark_mode_warning.visibility = if(isDarkTheme) View.GONE else View.VISIBLE
            }
            if(destination.id == R.id.appsFragment){
                toolbar.elevation = 0f
            }else{
                toolbar.elevation = resources.getDimension(R.dimen.toolbar_elevation)
            }
            toolbar_title.text = destination.label
        }
        home.setOnClickListener {
            navController.navigateUp()
        }
        home.visibility = View.INVISIBLE
    }

    private fun adjustToolbar() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        //Adjust toolbar height to account for transparent status bar
        window.decorView.rootView.setOnApplyWindowInsetsListener { view, insets ->
            val statusBarSize = insets.systemWindowInsetTop
            val navBarSize = insets.systemWindowInsetBottom
            if (statusBarSize != 0) {
                statusBarHeight = statusBarSize
                toolbar.post {
                    val tv = TypedValue()
                    if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                        val actionBarHeight = TypedValue.complexToDimensionPixelSize(
                            tv.data,
                            resources.displayMetrics
                        )
                        Companion.actionBarHeight = actionBarHeight
                        setupStatusPadding()
                    }
                }
            }
            if(navBarSize != 0){
                navBarHeight = navBarSize
                val params = fragment_container.layoutParams as FrameLayout.LayoutParams
                params.bottomMargin = navBarSize
            }
            insets
        }
    }

    private fun setInitialDarkTheme(){
        lottie.frame = 21
        loadingState.background = ColorDrawable(getColor(android.R.color.black))
        toolbar_background.background =
                ColorDrawable(getColor(R.color.toolbar_dark))
        loading_text.setTextColor(Color.WHITE)
        toolbar_title.setTextColor(Color.WHITE)
        home.imageTintList = ColorStateList.valueOf(Color.WHITE)
        menu.imageTintList = ColorStateList.valueOf(Color.WHITE)
        window.decorView.systemUiVisibility = 0
    }

    private fun setupLottieListener() {
        var isDarkBackground = false
        if(isDarkTheme){
            setInitialDarkTheme()
            isDarkBackground = true
        }
        lottie.playAnimation()
        lottie.addAnimatorUpdateListener {
            if (lottie.frame == 20 && !isDarkBackground) {
                if (isWaitingForDismiss) stopLoading(true)
                else {
                    createCircularAnimation(true, darkAnimationReveal, {
                        darkAnimationReveal.visibility = View.VISIBLE
                    }, {
                        loadingState.background = ColorDrawable(getColor(android.R.color.black))
                        darkAnimationReveal.visibility = View.INVISIBLE
                    }, { anim ->
                        delay((anim.duration * 0.1).toLong())
                        loading_text.setTextColor(Color.WHITE)
                        delay((anim.duration * 0.9).toLong())
                        toolbar_title.setTextColor(Color.WHITE)
                        home.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        menu.imageTintList = ColorStateList.valueOf(Color.WHITE)
                        delay((anim.duration * 0.05).toLong())
                        window.decorView.systemUiVisibility = 0
                    })?.start()
                    createCircularAnimation(true, toolbar_background_dark, {
                        toolbar_background_dark.visibility = View.VISIBLE
                    }, {
                        toolbar_background.background =
                                ColorDrawable(getColor(R.color.toolbar_dark))
                        toolbar_background_dark.visibility = View.INVISIBLE
                    }, null)?.start()
                    isDarkBackground = true
                }
                if(isWaitingToPause && isDarkTheme) lottie.pauseAnimation()
            } else if (lottie.frame == 65 && isDarkBackground) {
                if (isWaitingForDismiss) stopLoading(false)
                else {
                    createCircularAnimation(false, lightAnimationReveal, {
                        lightAnimationReveal.visibility = View.VISIBLE
                    }, {
                        loadingState.background = ColorDrawable(getColor(R.color.windowBackground))
                        lightAnimationReveal.visibility = View.INVISIBLE
                    }, { anim ->
                        delay((anim.duration * 0.1).toLong())
                        loading_text.setTextColor(Color.BLACK)
                        delay((anim.duration * 0.9).toLong())
                        toolbar_title.setTextColor(Color.BLACK)
                        home.imageTintList = ColorStateList.valueOf(Color.BLACK)
                        menu.imageTintList = ColorStateList.valueOf(Color.BLACK)
                        delay((anim.duration * 0.05).toLong())
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    })?.start()
                    createCircularAnimation(false, toolbar_background_light, {
                        toolbar_background_light.visibility = View.VISIBLE
                    }, {
                        toolbar_background.background =
                                ColorDrawable(getColor(android.R.color.white))
                        toolbar_background_light.visibility = View.INVISIBLE
                    }, null)?.start()
                    isDarkBackground = false
                }
                if(isWaitingToPause && !isDarkTheme) lottie.pauseAnimation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupStatusPadding()
    }

    private fun setupStatusPadding() {
        if (actionBarHeight != null && statusBarHeight != null) {
            toolbar?.layoutParams?.height = actionBarHeight!! + statusBarHeight!!
            fragment_container.setPadding(0, statusBarHeight!!, 0, 0)
        }
    }

    private fun checkRootAndSecure() {
        uiScope.launch {
            var isRooted = false
            loading_text.text = getString(R.string.checking_root)
            withContext(Dispatchers.Default) {
                isRooted = Shell.rootAccess()
            }
            loading_text.text = getString(R.string.loading_apps)
            withContext(Dispatchers.Default){
                loadApps()
                preferenceUtils?.getEnabledApps(this@MainActivity)
                preferenceUtils?.enabledApps?.let {
                    activityCallbacks?.onEnabledAppListLoaded(it)
                }
            }
            Log.d("DarQ", "isRooted $isRooted")
            if (isRooted) {
                launchScript()
                checkServiceRunning()
                hasCheckedRoot = true
            }else{
                generateShellScript()
                checkServiceRunning()
            }
        }
    }

    private fun launchScript(){
        uiScope.launch {
            withContext(Dispatchers.Default){
                val command = DarqIPCReceiver.getLaunchScript(this@MainActivity)
                runRootCommand(command)
            }
        }
    }

    private fun checkServiceRunning() {
        loading_text.text = getString(R.string.checking_service)

        if (!isAccessibilityServiceEnabled(this, DarqBackgroundService::class.java)) {
            showAccessibilitySheet()
            return
        }

        var hasReceived = false
        var isIpcEnabled = false

        //Register a receiver to wait for the response
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                hasReceived = true
                isIpcEnabled = intent?.getBooleanExtra(DarqBackgroundService.KEY_IPC_FOUND, false) ?: false
                if(isIpcEnabled) isWaitingForDismiss = true
                unregisterReceiver(this)
            }
        }, IntentFilter(BROADCAST_PONG))

        //Setup a timeout
        uiScope.launch {
            withContext(Dispatchers.Default) {
                delay(5000L)
            }
            if (!isIpcEnabled){
               if(isRoot){
                   showNoServiceRootSheet()
               }else{
                   showNoServiceSheet()
               }
            }
            if (!hasReceived) {
                showAccessibilityRunningSheet()
            }
        }

        //Send a broadcast and wait for the reply
        val intent = Intent(BROADCAST_PING)
        intent.`package` = packageName
        sendBroadcast(intent)
    }

    private fun showNoServiceSheet(){
        isWaitingToPause = true
        val bottomSheet = BottomSheetFragment()
        bottomSheet.layout =
            R.layout.bottom_sheet_no_service
        bottomSheet.cancelListener = {
            finish()
            true
        }
        bottomSheet.okLabel =
            R.string.bottom_sheet_no_service_steps
        bottomSheet.okListener = {
            startActivity(Intent(this, ModalFaqActivity::class.java))
            false
        }
        if(!supportFragmentManager.isDestroyed) {
            bottomSheet.show(supportFragmentManager, "bs_no_service")
        }
    }

    private fun showNoServiceRootSheet(){
        isWaitingToPause = true
        val bottomSheet = BottomSheetFragment()
        bottomSheet.layout =
            R.layout.bottom_sheet_no_service_root
        bottomSheet.okListener = {
            finish()
            true
        }
        if(!supportFragmentManager.isDestroyed) {
            bottomSheet.show(supportFragmentManager, "bs_no_service_root")
        }
    }

    private fun showAccessibilitySheet(){
        isWaitingToPause = true
        val bottomSheet = BottomSheetFragment()
        bottomSheet.layout =
            R.layout.bottom_sheet_accessibility
        bottomSheet.cancelListener = {
            finish()
            true
        }
        bottomSheet.okListener = {
            currentBottomSheet = bottomSheet
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(
                intent,
                INTENT_RELOAD
            )
            false
        }
        if(!supportFragmentManager.isDestroyed) {
            bottomSheet.show(supportFragmentManager, "bs_accessibility")
        }
    }

    private fun showAccessibilityRunningSheet(){
        isWaitingToPause = true
        val bottomSheet = BottomSheetFragment()
        bottomSheet.layout =
            R.layout.bottom_sheet_accessibility_not_running
        bottomSheet.cancelListener = {
            finish()
            true
        }
        bottomSheet.okListener = {
            currentBottomSheet = bottomSheet
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent,
                INTENT_RELOAD
            )
            false
        }
        if(!supportFragmentManager.isDestroyed) {
            bottomSheet.show(supportFragmentManager, "bs_accessibility_not_running")
        }
    }

    private fun generateShellScript(){
        val rootCommand = DarqIPCReceiver.getLaunchShellScript(this)
        val file = File(getExternalFilesDir(null), "script.sh")
        file.parentFile?.mkdirs()
        file.writer(Charset.defaultCharset()).buffered().run {
            write("#!/system/bin/sh")
            newLine()
            for((count, line) in rootCommand.withIndex()){
                write(line)
                if(count < rootCommand.size - 1) {
                    newLine()
                }else{
                    write(" &")
                }
            }
            close()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INTENT_RELOAD){
            currentBottomSheet?.dismiss()
            currentBottomSheet = null
            recreate()
        }
    }

    private fun stopLoading(isEnd: Boolean) {
        isWaitingForDismiss = false
        lottie.pauseAnimation()
        loading_text.text = getString(R.string.loading)
        root.background =
                if (isDarkTheme) ColorDrawable(Color.BLACK) else ColorDrawable(getColor(
                    R.color.windowBackground
                ))
        createCircularAnimation(isEnd, fragment_container, {
            fragment_container.visibility = View.VISIBLE
        }, {
            loadingState.visibility = View.GONE
        }, { anim ->
            val color = if (isDarkTheme) Color.WHITE else Color.BLACK
            delay((anim.duration * 0.1).toLong())
            loading_text.setTextColor(color)
            delay((anim.duration * 0.9).toLong())
            toolbar_title.setTextColor(color)
            home.imageTintList = ColorStateList.valueOf(color)
            menu.imageTintList = ColorStateList.valueOf(color)
            delay((anim.duration * 0.05).toLong())
            window.decorView.systemUiVisibility =
                    if (isDarkTheme) 0 else View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        })?.start()
        val view = if (isDarkTheme) toolbar_background_dark else toolbar_background_light
        val color = if (isDarkTheme) getColor(R.color.toolbar_dark) else Color.WHITE
        createCircularAnimation(isEnd, view, {
            view.visibility = View.VISIBLE
        }, {
            toolbar_background.background =
                    ColorDrawable(color)
            view.visibility = View.INVISIBLE
        }, null)?.start()
    }

    private fun loadApps() {
        allApps.clear()
        allApps.addAll(packageManager.getInstalledPackages(0).map {
            App(it.packageName, packageManager.getApplicationLabel(it.applicationInfo), it.applicationInfo.flags.and(ApplicationInfo.FLAG_SYSTEM) != 0)
        })
        allApps.sortWith(compareBy{it.appName.toString().toLowerCase()})
        activityCallbacks?.onAppListLoaded(allApps)
    }

    /**
     * Creates a circular reveal animation for a given "reveal" view, and optional three listeners
     * @param animationRevealView The view to reveal
     * @param startListener Method to invoke from onAnimationStart of the animation
     * @param endListener Method to invoke from onAnimationStart of the animation
     * @param asyncTask Suspend function to invoke while animation is running, passes animation object (to be used with arbitrary delays to invert text)
     */
    private fun createCircularAnimation(
        isEnd: Boolean = false,
        animationRevealView: View,
        startListener: (() -> Unit)?,
        endListener: (() -> Unit)?,
        asyncTask: (suspend (anim: Animator) -> Unit)?
    ): Animator? {
        val cx = lottie.x + (lottie.width * (if (isEnd) 0.65 else 0.35))
        val cy = lottie.y + (lottie.height / 2)
        val finalRadius = Math.hypot(cx, cy.toDouble())
        //if(!animationRevealView.isAttachedToWindow) return null
        val anim = ViewAnimationUtils.createCircularReveal(
            animationRevealView,
            cx.toInt(),
            cy.toInt(),
            0f,
            finalRadius.toFloat()
        )
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
                startListener?.invoke()
            }

            override fun onAnimationEnd(p0: Animator?) {
                endListener?.invoke()
            }
        })
        uiScope.launch {
            asyncTask?.invoke(anim)
        }
        anim.duration = (anim.duration / 1.25).toLong()
        return anim
    }
}
