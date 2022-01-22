package com.jvoyatz.beateat.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.awaitAnimateCamera
import com.google.maps.android.ktx.awaitMap
import com.jvoyatz.beateat.R
import com.jvoyatz.beateat.common.DEFAULT_POSITION
import com.jvoyatz.beateat.common.PERMISSIONS
import com.jvoyatz.beateat.common.Utils
import com.jvoyatz.beateat.databinding.MapFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : Fragment() {
    private val TAG = "MapFragment"
    private val noLocationDialog: AlertDialog by lazy {
        Utils.getRationaleDialog(requireContext(),
            { permissionLauncher.launch(PERMISSIONS) },
            { onLocationPermissionsDenied() },
        )
    }

    @SuppressLint("MissingPermission")
    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)  -> {
                    Toast.makeText(requireContext(), getString(R.string.loc_precise_granted), Toast.LENGTH_LONG)
                        .show()
                    onLocationPermissionsGranted()
                }
                else -> {
                    onLocationPermissionsDenied()
                }
            }
        }


    private var _binding: MapFragmentBinding? = null
    private val binding: MapFragmentBinding
        get() = _binding!!

    private val viewModel: MapViewModel by viewModels()
    private lateinit var map: GoogleMap
    private lateinit var pointSelectionMarker: Marker
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                map = mapFragment.awaitMap()
                initMap(map)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMap(map: GoogleMap) {

        if(Utils.isPermissionGranted(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            onLocationPermissionsGranted()
            map.setOnMyLocationClickListener {
                Toast.makeText(requireActivity(), "Current Location [$it]", Toast.LENGTH_SHORT).show()
            }
        }else{
            requestLocationPermissions()
        }

        map.setOnCameraMoveStartedListener {
            Log.d(TAG, "initMap: loading")
        }
        map.setOnCameraMoveListener {
            Log.d(TAG, "initMap: move")
            if(::pointSelectionMarker.isInitialized)
                pointSelectionMarker?.position = map.cameraPosition.target
        }
        map.setOnCameraIdleListener {
            Log.d(TAG, "initMap: idle")
            if(::pointSelectionMarker.isInitialized && !pointSelectionMarker.isVisible){
                pointSelectionMarker.isVisible = true
            }
        }
//        map.setOnCameraMoveCanceledListener {}
//        map.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener{
//            override fun onMarkerDrag(p0: Marker) {
//            }
//            override fun onMarkerDragStart(p0: Marker) {
//            }
//            override fun onMarkerDragEnd(p0: Marker) {
//                map.animateCamera(CameraUpdateFactory.newLatLng(p0.position));
//            }
//        })
    }

    override fun onPause() {
        super.onPause()
        if (noLocationDialog.isShowing) {
            noLocationDialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("MissingPermission")
    private fun onLocationPermissionsGranted() {
        Log.d(TAG, "onLocationPermissionsGranted() called")

        if (::map.isInitialized)
            map.isMyLocationEnabled = true


        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            Log.d(TAG, "onLocationPermissionsGranted: location $location")
            val markerOptions = location?.let { location ->
                Utils.getMarkerOptions(
                    requireContext(),
                    LatLng(location.latitude, location.longitude)
                )
            } ?: Utils.getMarkerOptions(requireContext(), DEFAULT_POSITION)

            map.addMarker(markerOptions)?.let {
                pointSelectionMarker = it
                pointSelectionMarker.also {
                    //map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, 16.0f), 5000, null)
                    lifecycleScope.launch {
                        map.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, 16.0f))
                    }
                }
            } ?: Toast.makeText(
                requireContext(),
                "An error occured while adding the marker",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationPermissionsDenied(){
        if(::map.isInitialized)
            map.isMyLocationEnabled = false

        map.addMarker(
            Utils.getMarkerOptions(requireContext(), DEFAULT_POSITION)
        )?.also {
            pointSelectionMarker = it
            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_POSITION, 12.0f), 1000, null)
            lifecycleScope.launch {
                map.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_POSITION, 12.0f))
            }
        }
    }

    private fun requestLocationPermissions() {
        when {
            (Utils.isPermissionGranted(requireContext().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    Utils.isPermissionGranted(requireContext().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION))  -> {
                        onLocationPermissionsGranted()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Log.d(TAG, "requestLocationPermissions() called#rationale")
                if (!noLocationDialog.isShowing)
                    noLocationDialog.show()
            }
            else -> {
                permissionLauncher.launch(PERMISSIONS)
            }
        }
    }
}