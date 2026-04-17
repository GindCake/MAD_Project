package com.example.kotlin_project_1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                enableUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission required for map", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Default location: Madrid (ETSI Informática)
        val madrid = LatLng(40.4523, -3.7261)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15f))

        addRecyclingMarkers()
        checkLocationPermissions()
    }

    private fun addRecyclingMarkers() {
        CampusData.exampleBins.forEach { bin ->
            val position = LatLng(bin.latitude, bin.longitude)
            val markerColor = when (bin.type) {
                BinType.PAPER -> BitmapDescriptorFactory.HUE_BLUE
                BinType.GLASS -> BitmapDescriptorFactory.HUE_GREEN
                BinType.PLASTIC -> BitmapDescriptorFactory.HUE_YELLOW
                BinType.ORGANIC -> BitmapDescriptorFactory.HUE_ORANGE
                BinType.E_WASTE -> BitmapDescriptorFactory.HUE_RED
            }

            mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("${bin.type} Bin")
                    .snippet(bin.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
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
}
