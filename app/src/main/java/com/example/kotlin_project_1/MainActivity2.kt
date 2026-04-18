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
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity2 : AppCompatActivity() {
    private val TAG = "EcoRouteMainActivity2"
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity 2 created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        
        database = AppDatabase.getDatabase(this)

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
        
        // Add Header
        val header = LayoutInflater.from(this).inflate(R.layout.list_header_location, listView, false)
        listView.addHeaderView(header, null, false)

        // Observe Room Database instead of reading from file
        database.locationDao().getAllLocations().observe(this, Observer { locations ->
            // Map LocationEntity to LocationRecord for the adapter
            val dataList = locations.map { entity ->
                LocationRecord(
                    entity.latitude.toString(),
                    entity.longitude.toString(),
                    entity.altitude.toString(),
                    entity.timestamp
                )
            }

            val adapter = LocationAdapter(this, R.layout.list_item_location, dataList)
            listView.adapter = adapter
            
            listView.setOnItemClickListener { _, _, position, _ ->
                val actualPosition = position - 1
                if (actualPosition >= 0) {
                    val selectedEntity = locations[actualPosition]
                    val intent = Intent(this, MainActivity3::class.java)
                    intent.putExtra("CLICKED_DATA", "User: ${selectedEntity.userId}\nLat: ${selectedEntity.latitude}\nLon: ${selectedEntity.longitude}\nAlt: ${selectedEntity.altitude}m\nTime: ${selectedEntity.timestamp}")
                    startActivity(intent)
                }
            }
        })
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_list
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, OpenStreetMapsActivity::class.java))
                    true
                }
                R.id.nav_list -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_list
    }
}