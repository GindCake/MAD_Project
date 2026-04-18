package com.example.kotlin_project_1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OpenStreetMapsActivity : AppCompatActivity(), LocationListener {
    private val tag = "EcoRouteMap"
    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var locationManager: LocationManager
    private var isRecording = false
    private lateinit var database: AppDatabase
    private val rewardManager = RewardManager()
    
    private var lastBinIdTriggered: String? = null

    private val upmLat = 40.4523
    private val upmLon = -3.7261

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                initMap()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        enableEdgeToEdge()
        setContentView(R.layout.activity_open_street_maps)
        
        database = AppDatabase.getDatabase(this)
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val switchRecord: SwitchCompat = findViewById(R.id.switchRecord)
        switchRecord.setOnCheckedChangeListener { _, isChecked ->
            isRecording = isChecked
        }

        checkPermissions()
        setupNavigation()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initMap() {
        val mapController = map.controller
        mapController.setZoom(18.5)
        val startPoint = GeoPoint(upmLat, upmLon)
        mapController.setCenter(startPoint)

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        addRecyclingMarkers()
        map.invalidate()
    }

    private fun addRecyclingMarkers() {
        CampusData.exampleBins.forEach { bin ->
            val marker = Marker(map)
            marker.position = GeoPoint(bin.latitude, bin.longitude)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            val points = rewardManager.getPointsForBin(bin.type)
            marker.title = "${bin.type} Bin: +$points points"
            marker.snippet = bin.address
            
            val emoji = when (bin.type) {
                BinType.PAPER -> "📄"
                BinType.GLASS -> "🫙"
                BinType.PLASTIC -> "🥤"
                BinType.ORGANIC -> "🍏"
                BinType.BATTERY -> "🔋"
                BinType.E_WASTE -> "💻"
            }
            marker.icon = createEmojiMarkerIcon(emoji)
            
            marker.setOnMarkerClickListener { _, _ ->
                showRecycleDialog(bin)
                true
            }
            
            map.overlays.add(marker)
        }
    }

    private fun createEmojiMarkerIcon(emoji: String): Drawable {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 60f
        }
        val width = paint.measureText(emoji).toInt()
        val height = (paint.descent() - paint.ascent()).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(emoji, 0f, -paint.ascent(), paint)
        return BitmapDrawable(resources, bitmap)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            initMap()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, this)
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        checkProximity(location)
        if (isRecording) {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(location.time))
            val userId = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userId", "User") ?: "User"
            lifecycleScope.launch {
                database.locationDao().insert(LocationEntity(
                    latitude = location.latitude, longitude = location.longitude,
                    altitude = location.altitude, timestamp = timestamp, userId = userId
                ))
            }
        }
    }

    private fun checkProximity(userLocation: Location) {
        CampusData.exampleBins.forEach { bin ->
            val results = FloatArray(1)
            Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                bin.latitude, bin.longitude, results
            )
            val distanceInMeters = results[0]

            if (distanceInMeters <= 10.0) {
                if (lastBinIdTriggered != bin.id) {
                    lastBinIdTriggered = bin.id
                    showRecycleDialog(bin)
                }
                return 
            }
        }
        lastBinIdTriggered = null
    }

    private fun showRecycleDialog(bin: RecyclingBin) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_recycle_bin, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val emoji = when (bin.type) {
            BinType.PAPER -> "📄"
            BinType.GLASS -> "🫙"
            BinType.PLASTIC -> "🥤"
            BinType.ORGANIC -> "🍏"
            BinType.BATTERY -> "🔋"
            BinType.E_WASTE -> "💻"
        }

        dialogView.findViewById<TextView>(R.id.tvBinEmoji).text = emoji
        dialogView.findViewById<TextView>(R.id.tvBinTitle).text = "${bin.type.name.lowercase().capitalize()} Bin"
        dialogView.findViewById<TextView>(R.id.tvBinAddress).text = bin.address
        val points = rewardManager.getPointsForBin(bin.type)
        dialogView.findViewById<TextView>(R.id.tvPointsValue).text = "+$points Points"

        dialogView.findViewById<Button>(R.id.btnRecycle).setOnClickListener {
            rewardManager.updatePoints(bin.type, 
                onComplete = { earned, total, needed, streak, isDoubled ->
                    var message = if (needed > 0) {
                        "Recycled! You just earned $earned points. Total: $total. You need $needed more for next level!"
                    } else {
                        "Recycled! You just earned $earned points. Total: $total. You reached the maximum level!"
                    }
                    if (isDoubled) {
                        message += " Double points applied (5+ day streak)!"
                    } else if (streak > 1) {
                        message += " Day $streak streak!"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_map
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, MainActivity::class.java)); true }
                R.id.nav_map -> true
                R.id.nav_list -> { startActivity(Intent(this, MainActivity2::class.java)); true }
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
                else -> false
            }
        }
    }

    override fun onResume() { 
        super.onResume()
        map.onResume()
        startLocationUpdates()
    }
    
    override fun onPause() { 
        super.onPause()
        map.onPause()
        stopLocationUpdates()
    }
}
