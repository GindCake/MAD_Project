package com.example.kotlin_project_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class MainActivity2 : AppCompatActivity() {
    private val TAG = "EcoRouteMainActivity2"
    private val FILE_NAME = "location_data.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: What goes where? activity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListView()
        setupNavigation()
    }

    private fun setupListView() {
        val listView: ListView = findViewById(R.id.listView)
        
        // Using Bin Data instead of random location data for "What goes where"
        val bins = CampusData.exampleBins

        // Add Header
        val header = LayoutInflater.from(this).inflate(R.layout.list_header_location, listView, false)
        listView.addHeaderView(header, null, false)

        // Map RecyclingBin to LocationRecord for reuse of existing adapter/layout
        // or we could create a new adapter. For now, let's keep it simple.
        val dataList = bins.map { bin ->
            LocationRecord(bin.latitude.toString(), bin.longitude.toString(), bin.type.name, bin.address)
        }

        val adapter = LocationAdapter(this, R.layout.list_item_location, dataList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val actualPosition = position - 1
            if (actualPosition >= 0) {
                val selectedBin = bins[actualPosition]
                val intent = Intent(this, MainActivity3::class.java)
                intent.putExtra("CLICKED_DATA", "Type: ${selectedBin.type}\nAddress: ${selectedBin.address}\nLat: ${selectedBin.latitude}, Lon: ${selectedBin.longitude}")
                startActivity(intent)
            }
        }
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_list
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    false
                }
                R.id.nav_list -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_list
    }
}