package com.example.kotlin_project_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class MainActivity2 : AppCompatActivity() {
    private val TAG = "btaMainActivity2"
    private val FILE_NAME = "location_data.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity 2 created")
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
        val dataList = mutableListOf<String>()

        // Load data from internal storage file
        val file = File(filesDir, FILE_NAME)
        if (file.exists()) {
            file.forEachLine { dataList.add(it) }
        } else {
            // Dummy data if file doesn't exist yet
            dataList.add("No recorded data found.")
            dataList.add("Example Point: 40.3323, -3.7653")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = dataList[position]
            Log.d(TAG, "Item clicked: $selectedItem")
            val intent = Intent(this, MainActivity3::class.java)
            intent.putExtra("CLICKED_DATA", selectedItem)
            startActivity(intent)
        }
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