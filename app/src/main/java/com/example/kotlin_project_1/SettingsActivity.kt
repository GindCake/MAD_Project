package com.example.kotlin_project_1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {
    private val TAG = "btaSettingsActivity"
    private val PREFS_NAME = "MyPrefs"
    private val KEY_USER_ID = "userId"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Settings activity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvSavedUserId: TextView = findViewById(R.id.tvSavedUserId)
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = sharedPreferences.getString(KEY_USER_ID, "Not set")
        tvSavedUserId.text = savedUserId

        setupNavigation()
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_settings
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
                R.id.nav_list -> {
                    startActivity(Intent(this, MainActivity2::class.java))
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }
}