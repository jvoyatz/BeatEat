package com.jvoyatz.beateat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jvoyatz.beateat.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val PERMISSIONS by lazy {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val noLocationDialog: AlertDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.warning))
            .setMessage(getString(R.string.location_permissions_not_granted_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                permissionLauncher.launch(
                    PERMISSIONS
                )
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
            .create()
    }
    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Toast.makeText(this, getString(R.string.loc_precise_granted), Toast.LENGTH_LONG)
                        .show()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Toast.makeText(this, getString(R.string.loc_approx_granted), Toast.LENGTH_LONG)
                        .show()
                }
                else -> {
                    if (!noLocationDialog.isShowing)
                        noLocationDialog.show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermissions()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun requestLocationPermissions() {
        when {
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                createLocationRequest()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                if (!noLocationDialog.isShowing)
                    noLocationDialog.show()
            }
            else -> {
                permissionLauncher.launch(PERMISSIONS)
            }
        }
    }

    fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onPause() {
        super.onPause()
        if (noLocationDialog.isShowing) {
            noLocationDialog.dismiss()
        }
    }
}
