package com.example.kotlin_project_1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private val TAG = "btaMainActivity"
    private val PREFS_NAME = "MyPrefs"
    private val KEY_USER_ID = "userId"

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val switchLocation: SwitchCompat = findViewById(R.id.switchLocation)
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            
            if (granted) {
                Log.d(TAG, "Location permissions granted via switch")
                showToast("Location Permissions Granted")
                switchLocation.isChecked = true
            } else {
                Log.w(TAG, "Location permissions denied via switch")
                showToast("Location Permissions Denied")
                switchLocation.isChecked = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: The activity is being created.")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_home, R.string.nav_home)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item ->
            handleNavigation(item.itemId)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        // User ID Logging Logic
        val editUserId: EditText = findViewById(R.id.editUserId)
        val btnLogUser: Button = findViewById(R.id.btnLogUser)
        
        // Initial load from SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = sharedPreferences.getString(KEY_USER_ID, "")
        editUserId.setText(savedUserId)

        btnLogUser.setOnClickListener {
            val userId = editUserId.text.toString()
            if (userId.isNotEmpty()) {
                showLogConfirmationDialog(userId)
            } else {
                Log.w(TAG, "Log button clicked but User ID field is empty")
                showToast("Please enter a User ID")
            }
        }

        // Enable Location Switch Logic
        val switchLocation: SwitchCompat = findViewById(R.id.switchLocation)
        
        // Initial state check
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        switchLocation.isChecked = hasPermission

        switchLocation.setOnClickListener {
            val isChecked = switchLocation.isChecked
            if (isChecked) {
                Log.d(TAG, "Attempting to turn location ON")
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                showRevokePermissionDialog()
                // Keep switch ON until user actually changes it in settings
                switchLocation.isChecked = true 
            }
        }

        findViewById<Button>(R.id.btnMap).setOnClickListener {
            startActivity(Intent(this, OpenStreetMapsActivity::class.java))
        }

        findViewById<Button>(R.id.btnWeather).setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun handleNavigation(itemId: Int) {
        when (itemId) {
            R.id.nav_home -> {
                // Already here
            }
            R.id.nav_map -> {
                startActivity(Intent(this, OpenStreetMapsActivity::class.java))
            }
            R.id.nav_list -> {
                startActivity(Intent(this, MainActivity2::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            handleNavigation(item.itemId)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        // Check SharedPreferences on start
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = sharedPreferences.getString(KEY_USER_ID, "")
        if (savedUserId.isNullOrEmpty()) {
            showIdentifyUserDialog()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showIdentifyUserDialog() {
        val input = EditText(this)
        input.hint = "User Identifier"
        
        AlertDialog.Builder(this)
            .setTitle("Identify User")
            .setMessage("Please enter your User ID to continue:")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val userId = input.text.toString()
                if (userId.isNotEmpty()) {
                    saveUserId(userId)
                    findViewById<EditText>(R.id.editUserId).setText(userId)
                } else {
                    showIdentifyUserDialog() // Re-show if empty
                }
            }
            .show()
    }

    private fun showLogConfirmationDialog(userId: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Log")
            .setMessage("Do you want to log and save User ID: $userId?")
            .setPositiveButton("Yes") { _, _ ->
                Log.d(TAG, "User ID logged: $userId")
                saveUserId(userId)
                showToast("Saved and Logged User ID: $userId")
            }
            .setNegativeButton("No") { dialog, _ ->
                Log.d(TAG, "User ID logging cancelled")
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun saveUserId(userId: String) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_USER_ID, userId)
            apply() // Asynchronous commit
        }
        Log.d(TAG, "User ID saved to SharedPreferences: $userId")
    }

    private fun showRevokePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Revoke Permission")
            .setMessage("To revoke permissions, you must do it in the system settings. Open settings now?")
            .setPositiveButton("Open Settings") { _, _ ->
                Log.d(TAG, "Opening settings to revoke permissions")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Log.d(TAG, "Revoke permission cancelled")
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Re-sync switch state when user returns from settings
        val switchLocation: SwitchCompat = findViewById(R.id.switchLocation)
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        switchLocation.isChecked = hasPermission
        
        // Update navigation selection
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_home
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}