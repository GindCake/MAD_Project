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
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        
        record?.let {
            tvLat.text = "Lat: %.4f".format(it.latitude.toDoubleOrNull() ?: 0.0)
            tvLon.text = "Lon: %.4f".format(it.longitude.toDoubleOrNull() ?: 0.0)
            tvAlt.text = "${it.altitude.toDoubleOrNull()?.let { "%.1f".format(it) } ?: it}m"
            
            // Assuming timestamp is "yyyy-MM-dd HH:mm:ss"
            val parts = it.timestamp.split(" ")
            if (parts.size >= 2) {
                tvDate.text = parts[0]
                tvTime.text = parts[1]
            } else {
                tvDate.text = it.timestamp
                tvTime.text = "--"
            }
        }
        
        return view
    }
}