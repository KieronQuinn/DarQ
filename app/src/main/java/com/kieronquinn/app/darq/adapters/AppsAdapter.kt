package com.kieronquinn.app.darq.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.darq.R
import com.kieronquinn.app.darq.holders.App
import kotlinx.android.synthetic.main.item_app.view.*
import com.kieronquinn.app.darq.utils.AppIconRequestHandler
import android.net.Uri
import android.widget.CheckBox
import com.squareup.picasso.Picasso


class AppsAdapter(context: Context, var apps : List<App>, private val enabledApps: List<String>, private val checkedChangeListener : (packageName: String, isChecked: Boolean, checkbox: CheckBox) -> Unit) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_app, parent, false))
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.itemView.title.text = app.appName
        val uri = Uri.parse("${AppIconRequestHandler.SCHEME_PNAME}:${app.packageName}")
        Picasso.get().load(uri).into(holder.itemView.icon)
        holder.itemView.setOnClickListener {
            holder.itemView.checkbox.toggle()
        }
        holder.itemView.checkbox.setOnCheckedChangeListener(null)
        holder.itemView.checkbox.isEnabled = true
        holder.itemView.checkbox.isChecked = enabledApps.contains(app.packageName)
        holder.itemView.checkbox.setOnCheckedChangeListener { checkbox, isChecked ->
            checkedChangeListener.invoke(app.packageName, isChecked, checkbox as CheckBox)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}