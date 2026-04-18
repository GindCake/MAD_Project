package com.example.kotlin_project_1

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    
    private val rewardManager = RewardManager()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var lastBinIdTriggered: String? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                enableUserLocation()
                startLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Location permission required for map", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        
        view.findViewById<FloatingActionButton>(R.id.fabShowQR)?.setOnClickListener {
            showQRCodeDialog()
        }
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val madrid = LatLng(40.4523, -3.7261)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 17f))

        addRecyclingMarkers()
        checkLocationPermissions()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    checkProximity(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
    }

    private fun checkProximity(userLocation: Location) {
        CampusData.exampleBins.forEach { bin ->
            val results = FloatArray(1)
            Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                bin.latitude, bin.longitude, results
            )
            val distance = results[0]

            if (distance <= 10.0) {
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
        val points = rewardManager.getPointsForBin(bin.type)
        AlertDialog.Builder(requireContext())
            .setTitle("Recycle here?")
            .setMessage("Recycling this ${bin.type} bin will earn you $points points!")
            .setPositiveButton("Recycle") { _, _ ->
                rewardManager.updatePoints(bin.type) { earned, total, needed ->
                    val message = if (needed > 0) {
                        "Recycled! Earned $earned pts. Total: $total. $needed more for next level!"
                    } else {
                        "Recycled! Earned $earned pts. Total: $total. Max level reached!"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showQRCodeDialog() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
            val level = snapshot.getString("level") ?: "Eco-Conscious"
            val qrBitmap = rewardManager.generateQRCodePlaceholder(level)
            
            val imageView = ImageView(context).apply {
                setImageBitmap(qrBitmap)
                setPadding(50, 50, 50, 50)
            }
            
            AlertDialog.Builder(requireContext())
                .setTitle("Your Reward QR ($level)")
                .setView(imageView)
                .setPositiveButton("Close", null)
                .show()
        }
    }

    private fun addRecyclingMarkers() {
        CampusData.exampleBins.forEach { bin ->
            val position = LatLng(bin.latitude, bin.longitude)
            val markerColor = when (bin.type) {
                BinType.PAPER -> BitmapDescriptorFactory.HUE_BLUE
                BinType.GLASS -> BitmapDescriptorFactory.HUE_GREEN
                BinType.PLASTIC -> BitmapDescriptorFactory.HUE_YELLOW
                BinType.ORGANIC -> BitmapDescriptorFactory.HUE_ORANGE
                BinType.BATTERY, BinType.E_WASTE -> BitmapDescriptorFactory.HUE_RED
            }

            mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("${bin.type} Bin (+${rewardManager.getPointsForBin(bin.type)} pts)")
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
            startLocationUpdates()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            startLocationUpdates()
        }
    }
}
