package com.example.kotlin_project_1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class LocationAdapter(context: Context, resource: Int, objects: List<LocationRecord>) :
    ArrayAdapter<LocationRecord>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_location, parent, false)
        
        val record = getItem(position)
        
        val tvLat: TextView = view.findViewById(R.id.tvLat)
        val tvLon: TextView = view.findViewById(R.id.tvLon)
        val tvAlt: TextView = view.findViewById(R.id.tvAlt)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        
        record?.let {
            tvLat.text = "Lat: ${it.latitude}"
            tvLon.text = "Lon: ${it.longitude}"
            tvAlt.text = "Alt: ${it.altitude}"
            tvTime.text = it.timestamp
        }
        
        return view
    }
}