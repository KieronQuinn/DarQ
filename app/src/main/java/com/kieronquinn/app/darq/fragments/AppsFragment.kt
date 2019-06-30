package com.kieronquinn.app.darq.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.darq.MainActivity
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.adapters.AppsAdapter
import com.kieronquinn.app.darq.holders.App
import com.kieronquinn.app.darq.services.DarqBackgroundService
import com.kieronquinn.app.darq.utils.PreferenceUtils
import com.kieronquinn.app.darq.utils.sendBroadcast

class AppsFragment : Fragment() {

    val preferenceUtils: PreferenceUtils?
        get() = (activity as? MainActivity)?.preferenceUtils

    private val checkboxHolder = ArrayList<CheckBox>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { context ->
            view as RecyclerView
            view.layoutManager = LinearLayoutManager(context)
            getApps()?.let {
                preferenceUtils?.enabledApps?.let {enabledApps ->
                    view.adapter = AppsAdapter(context, it, enabledApps) { packageName, isChecked, checkbox ->
                        checkboxHolder.add(checkbox)
                        checkbox.isEnabled = false
                        if (isChecked) enabledApps.add(packageName)
                        else enabledApps.remove(packageName)
                        preferenceUtils?.saveApps(context, enabledApps)
                        activity?.sendBroadcast(DarqBackgroundService.REFRESH, Pair(DarqBackgroundService.EXTRA_FORCE_STOP, packageName))
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(reloadListener, IntentFilter(DarqBackgroundService.REFRESH))
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(reloadListener)
    }

    private val reloadListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            for(checkbox in checkboxHolder){
                checkbox?.isEnabled = true
            }
        }
    }

    private fun getApps(): ArrayList<App>? {
        return (activity as? MainActivity)?.allApps
    }

}