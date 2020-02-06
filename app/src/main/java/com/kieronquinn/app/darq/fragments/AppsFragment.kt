package com.kieronquinn.app.darq.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.activities.MainActivity
import com.kieronquinn.app.darq.adapters.AppsAdapter
import com.kieronquinn.app.darq.holders.App
import com.kieronquinn.app.darq.services.DarqBackgroundService
import com.kieronquinn.app.darq.utils.PreferenceUtils
import com.kieronquinn.app.darq.utils.sendBroadcast
import kotlinx.android.synthetic.main.fragment_apps.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class AppsFragment : Fragment() {

    private val job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val preferenceUtils: PreferenceUtils?
        get() = (activity as? MainActivity)?.preferenceUtils

    private val checkboxHolder = ArrayList<CheckBox>()

    private var shouldShowSystemApps: Boolean = false
    private var searchTerm: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        context?.let { context ->
            recyclerView.layoutManager = LinearLayoutManager(context)
            getApps(shouldShowSystemApps, searchTerm){ apps ->
                preferenceUtils?.enabledApps?.let {enabledApps ->
                    swipeRefreshLayout.post {
                        swipeRefreshLayout.isEnabled = false
                        swipeRefreshLayout.isRefreshing = true
                    }
                    recyclerView.adapter = AppsAdapter(context, apps, enabledApps) { packageName, isChecked, checkbox ->
                        checkboxHolder.add(checkbox)
                        checkbox.isEnabled = false
                        if (isChecked) enabledApps.add(packageName)
                        else enabledApps.remove(packageName)
                        preferenceUtils?.saveApps(context, enabledApps)
                        activity?.sendBroadcast(DarqBackgroundService.REFRESH, Pair(DarqBackgroundService.EXTRA_FORCE_STOP, packageName))
                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                    swipeRefreshLayout.postDelayed({
                        swipeRefreshLayout.isRefreshing = false
                    }, 500)
                }
            }
        }
        searchBox.setOnEditorActionListener { v, actionId, event ->
            val result = if(actionId == EditorInfo.IME_ACTION_SEARCH){
                swipeRefreshLayout.isRefreshing = true
                val imm: InputMethodManager = swipeRefreshLayout.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                getApps(shouldShowSystemApps, searchBox.text.toString()){
                    searchTerm = searchBox.text.toString()
                    val adapter = recyclerView.adapter as AppsAdapter
                    adapter.apps = it
                    adapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                    if(it.isEmpty()){
                        recyclerView.visibility = View.GONE
                        empty_list.visibility = View.VISIBLE
                    }else{
                        recyclerView.visibility = View.VISIBLE
                        empty_list.visibility = View.GONE
                    }
                }
                true
            }else false
            result
        }
        searchBox.addTextChangedListener {
            if(it?.isNotEmpty() == true){
                search_clear.visibility = View.VISIBLE
            }else{
                search_clear.visibility = View.GONE
            }
        }
        search_clear.setOnClickListener {
            searchTerm = null
            searchBox.editableText.clear()
            swipeRefreshLayout.isRefreshing = true
            val imm: InputMethodManager = swipeRefreshLayout.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            getApps(shouldShowSystemApps, searchTerm){
                searchTerm = searchBox.text.toString()
                val adapter = recyclerView.adapter as AppsAdapter
                adapter.apps = it
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
                if(it.isEmpty()){
                    recyclerView.visibility = View.GONE
                    empty_list.visibility = View.VISIBLE
                }else{
                    recyclerView.visibility = View.VISIBLE
                    empty_list.visibility = View.GONE
                }
            }
        }
        search_clear.visibility = View.GONE
    }

    private fun getApps(showSystemApps: Boolean, searchString: String?, callback: ((apps: List<App>) -> Unit)? = null) {
        getApps()?.let { apps ->
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    val appList = apps.filter {
                        (!it.isSystemApp || showSystemApps) && (searchString == null || it.appName.toString().toLowerCase(Locale.getDefault()).contains(searchString.toLowerCase(Locale.getDefault())))
                    }
                    withContext(Dispatchers.Main){
                        callback?.invoke(appList)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(reloadListener, IntentFilter(DarqBackgroundService.REFRESH))
        setupDots()
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(reloadListener)
    }

    private val reloadListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            for(checkbox in checkboxHolder){
                checkbox.isEnabled = true
            }
        }
    }

    private fun getApps(): ArrayList<App>? {
        return (activity as? MainActivity)?.allApps
    }

    private fun toggleSystemApps(shouldShow: Boolean, callback: (() -> Unit)){
        swipeRefreshLayout.isRefreshing = true
        val adapter = recyclerView.adapter as AppsAdapter
        getApps(shouldShow, searchTerm){
            adapter.apps = it
            adapter.notifyDataSetChanged()
            shouldShowSystemApps = shouldShow
            swipeRefreshLayout.isRefreshing = false
            callback.invoke()
        }
    }

    private fun setupDots(){
        val dots = activity?.findViewById<ImageView>(R.id.menu)
        dots?.let {
            dots.visibility = View.VISIBLE
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.gravity = Gravity.END
            popupMenu.inflate(R.menu.menu_apps)
            val checkBox = popupMenu.menu.findItem(R.id.menu_show_system)
            checkBox.setOnMenuItemClickListener {
                checkBox.isEnabled = false
                checkBox.isChecked = !checkBox.isChecked
                toggleSystemApps(checkBox.isChecked) {
                    checkBox.isEnabled = true
                }
                true
            }
            dots.setOnClickListener {
                popupMenu.show()
            }
        }
    }

}