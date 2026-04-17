package com.example.kotlin_project_1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OpenStreetMapsActivity : AppCompatActivity(), LocationListener {
    private val TAG = "btaOpenStreetMap"
    private val FILE_NAME = "location_data.txt"
    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var locationManager: LocationManager
    private var isRecording = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                Log.d(TAG, "Permissions granted")
                initMap()
            } else {
                Log.e(TAG, "Permissions denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_street_maps)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val switchRecord: SwitchCompat = findViewById(R.id.switchRecord)
        switchRecord.setOnCheckedChangeListener { _, isChecked ->
            isRecording = isChecked
            Log.d(TAG, "Recording status: $isRecording")
            if (isRecording) {
                startLocationUpdates()
            } else {
                stopLocationUpdates()
            }
        }

        checkPermissions()
        setupNavigation()
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_map
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_map -> true
                R.id.nav_list -> {
                    startActivity(Intent(this, MainActivity2::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            initMap()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun initMap() {
        val mapController = map.controller
        mapController.setZoom(18.0)
        
        val startPoint = GeoPoint(40.3323, -3.7653)
        mapController.setCenter(startPoint)

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        map.overlays.add(locationOverlay)

        // Tour Markers
        val tourLocations = listOf(
            Triple(GeoPoint(40.3323, -3.7653), "Campus Entrance", "Start your tour here!"),
            Triple(GeoPoint(40.3325, -3.7645), "ETSISI Building", "Engineering School."),
            Triple(GeoPoint(40.3318, -3.7648), "Campus Library", "Study Area.")
        )

        tourLocations.forEach { (point, title, description) ->
            val marker = Marker(map)
            marker.position = point
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = title
            marker.snippet = description
            marker.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_map)
            map.overlays.add(marker)
        }

        map.invalidate()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, this)
            Log.d(TAG, "Started location updates (5s, 5m)")
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
        Log.d(TAG, "Stopped location updates")
    }

    override fun onLocationChanged(location: Location) {
        if (isRecording) {
            val lat = String.format("%.4f", location.latitude)
            val lon = String.format("%.4f", location.longitude)
            val alt = location.altitude
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.time))

            val dataString = "Lat: $lat, Lon: $lon, Alt: $alt m, Time: $timestamp\n"
            Log.d(TAG, "RECORDING - $dataString")
            
            saveDataToFile(dataString)
        }
    }

    private fun saveDataToFile(data: String) {
        try {
            val fileOutputStream: FileOutputStream = openFileOutput(FILE_NAME, Context.MODE_APPEND)
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.close()
            Log.d(TAG, "Data saved to file: $FILE_NAME")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving data to file", e)
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        if (isRecording) startLocationUpdates()
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_map
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        stopLocationUpdates()
    }
}