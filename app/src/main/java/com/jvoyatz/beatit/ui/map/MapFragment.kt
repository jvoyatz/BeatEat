package com.jvoyatz.beatit.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.maps.android.ktx.awaitAnimateCamera
import com.google.maps.android.ktx.awaitMap
import com.jvoyatz.beatit.R
import com.jvoyatz.beatit.common.*
import com.jvoyatz.beatit.databinding.MapFragmentBinding
import com.jvoyatz.beatit.domain.Place
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG = "MapFragment"

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val noLocationDialog: AlertDialog by lazy {
        Utils.getRationaleDialog(
            requireContext(),
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
    var bottomSheetCallback = object: BottomSheetCallback(){
        override fun onStateChanged(bottomSheet: View, newState: Int) {}

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            map.executeIfInitialized {
                if(slideOffset.roundToInt() == 1 ){
                    map.setPadding(0, 0, 0, 780)
                }else{
                    map.setPadding(0, 0, 0, 350)
                }
            }
        }
    }

    private var _binding: MapFragmentBinding? = null
    private val binding: MapFragmentBinding
        get() = _binding!!

    private lateinit var selectedPlaceMarker: Marker
    private lateinit var pointSelectionMarker: Marker
    private val selectedPlaceMarkerOptions by lazy{
        Utils.getMarkerOptions(requireContext(), DEFAULT_POSITION, R.drawable.ic_location_red)
    }
    private val viewModel: MapViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var map: GoogleMap
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var onBackCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        bottomSheetBehavior = from(binding.bottomsheet)
        bottomSheetBehavior.state = STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

        onBackCallback = requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                viewModel.onPlaceSelectDone()
            }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        //init adapter by passing the on click handler
        placesAdapter = PlacesAdapter(
            requireContext().applicationContext,
            {
            it.let { place ->
                viewModel.onPlaceSelect(place)
            }
        }, CoroutineScope(Dispatchers.Default))
        binding.placesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.placesRecyclerview.adapter = placesAdapter

        //map initialization
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                map = mapFragment.awaitMap()
                initMap(map)
            }
        }

        //watching the fetched results
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.placeState.collectLatest {
                    val placesAdapter = binding.placesRecyclerview.adapter as PlacesAdapter
                    when (it) {
                        is Resource.Success -> {
                            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            lifecycleScope.launch(Dispatchers.Default){
                                viewModel.selectedPlaceState.value.first?.let { selectedPlace ->
                                    it.data.firstOrNull { place -> place.fsqId.equals(selectedPlace.fsqId) }
                                        ?.let { foundPlace ->
                                            binding.navigationItem.place = foundPlace
                                            it.data.toMutableList()
                                                .also { llist ->
                                                    llist.remove(foundPlace)
                                                    placesAdapter.submit(llist)
                                                }
                                        }
                                } ?:  placesAdapter.submit(it.data)
                            }
                        }
                        is Resource.Error -> {
                            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            Toast.makeText(requireContext(), it.message ?: "Unknown Error Exception", Toast.LENGTH_SHORT).show()
                            placesAdapter.submit(listOf())
                        }
                        is Resource.Loading -> {
                            //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            placesAdapter.submitLoading()
                        }
                        else -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedPlaceState.collectLatest { pair ->
               onPlaceSelected(pair.first)
            }
        }
    }

    private fun onPlaceSelected(place: Place?) {
        when (place) {
            null -> {
                if (::selectedPlaceMarker.isInitialized) {
                    selectedPlaceMarker?.let {
                        it.isVisible = false
                    }
                }
                onBackCallback.setEnable(false)
            }
            else -> {
                binding.navigationItem.place = place
                onBackCallback.setEnable(true)
                if (::selectedPlaceMarker.isInitialized) {
                    selectedPlaceMarker.also {
                        it.position = LatLng(place.location.latitude, place.location.longitude)
                        it.isVisible = true
                        lifecycleScope.launch {
                            map.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceMarker.position, 17.0f))
                        }
                    }
                } else {
                    addMarker(Utils.getMarkerOptions(requireContext(), LatLng(place.location.latitude, place.location.longitude), R.drawable.ic_location_red))
                    ?.let {
                        selectedPlaceMarker = it
                        lifecycleScope.launch {
                            map.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceMarker.position, 17.0f))
                        }
                    }
                        //?: Toast.makeText(requireContext(), "An error occured while adding the marker", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    /**
     * this fragments needs to provide its custom onBackPressed logic
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(::onBackCallback.isInitialized && onBackCallback.isEnabled){
            viewModel.onPlaceSelectDone()
            return true
        }
        return false
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


    //helpers
    /**
     * Checks whether permissions have been granted, otherwise it requests them
     * If the first condition is true, executes whatever needed when we can get user's location,
     * meaning showing a marker onto user's location on the map
     *
     * Apart from that, is sets the map's camera listeners so as to observe when user is dragging
     * the map and update our ui elements
     */
    @SuppressLint("MissingPermission")
    private fun initMap(map: GoogleMap) {

        if(viewModel.isPlaceSelected()){
           onPlaceSelected(viewModel.selectedPlaceState.value.first!!)
        }

        if(Utils.isPermissionGranted(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ||
            Utils.isPermissionGranted(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            onLocationPermissionsGranted()
            map.setOnMyLocationClickListener {
                Toast.makeText(requireActivity(), "Current Location [$it]", Toast.LENGTH_SHORT).show()
            }
        }else{
            requestLocationPermissions()
        }

        //when camera starts to move and bottomsheet is not hidden
        //then show that our list is loading to get new places near us
        map.setOnCameraMoveStartedListener {
            if(!bottomSheetBehavior.isHidden() && !viewModel.isPlaceSelected()) {
                placesAdapter.submitLoading()
                //bottomSheetBehavior.state = STATE_COLLAPSED
                //bottomSheetBehavior.setBottomsheetPeekHeight()
            }
        }
        //update the map's marker to the camera's location
        map.setOnCameraMoveListener {
            if(::pointSelectionMarker.isInitialized && !viewModel.isPlaceSelected())
                pointSelectionMarker.position = map.cameraPosition.target
        }

        //when camera is finished moving
        //1. we show the selection marker, this executes only the first time
        //2. the first time our bottomsheet is hidden as well
        //3. search for places near the selected location
        map.setOnCameraIdleListener {
            if(::pointSelectionMarker.isInitialized) {
                viewModel.isPlaceSelected().also {
                    when(it){
                        true -> {
                            pointSelectionMarker.isVisible = false
                        }
                        false ->{
                            pointSelectionMarker.isVisible = true
                            viewModel.searchForPlaces(map.cameraPosition.target.toFourSquareLL())
                        }
                    }
                }
            }
        }
    }


    fun addMarker(markerOptions: MarkerOptions): Marker? {
        when(::map.isInitialized){
           true -> {
                return map.addMarker(markerOptions)?.let {
                    it.also {
                        it.isVisible = true
                        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, 16.0f), 5000, null)
//                        lifecycleScope.launch {
//                            map.awaitAnimateCamera(
//                                CameraUpdateFactory.newLatLngZoom(
//                                    markerOptions.position,
//                                    16.0f
//                                )
//                            )
//                        }
                    }
                }
            }
            else -> {return null}
        }
    }

    //permissions
    /**
     * Handles permissions
     */
    private fun requestLocationPermissions() {
        when {
            (Utils.isPermissionGranted(requireContext().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    Utils.isPermissionGranted(requireContext().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION))  -> {
                onLocationPermissionsGranted()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)-> {
                if (!noLocationDialog.isShowing)
                    noLocationDialog.show()
            }
            else -> {
                permissionLauncher.launch(PERMISSIONS)
            }
        }
    }

    /**
     * Shows users location on map and adds the corresponding marker for this
     * and animates to this location
     */
    @SuppressLint("MissingPermission")
    private fun onLocationPermissionsGranted() {
        map.executeIfInitialized {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val markerOptions =
                    location?.let { location ->
                        Utils.getMarkerOptions(
                            requireContext(),
                            LatLng(location.latitude, location.longitude),
                            R.drawable.ic_location
                        )
                    } //?: Utils.getMarkerOptions(requireContext(), DEFAULT_POSITION, R.drawable.ic_location)

                markerOptions?.let{addMarker(markerOptions)?.let{
                    pointSelectionMarker = it
                    pointSelectionMarker.isVisible = !viewModel.isPlaceSelected()
                    lifecycleScope.launch {
                        when (viewModel.isPlaceSelected()) {
                            true -> {
//                                val loc = viewModel.selectedPlaceState.value.first!!.location
//                                LatLng(loc.latitude, loc.longitude)
//                                CameraUpdateFactory.newLatLngZoom(markerOptions.position, 16.0f)
                            }
                            else -> {
                                map.awaitAnimateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        markerOptions.position,
                                        16.0f
                                    )
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    /**
     * When denied, user's location is not being shown on map
     * Instead, we show the marker with a default location
     */
    @SuppressLint("MissingPermission")
    private fun onLocationPermissionsDenied(){
        map.executeIfInitialized {
            map.isMyLocationEnabled = false
            addMarker(Utils.getMarkerOptions(requireContext(), DEFAULT_POSITION, R.drawable.ic_location))
                ?.also {
                    pointSelectionMarker = it
                    pointSelectionMarker.isVisible = !viewModel.isPlaceSelected()
                    lifecycleScope.launch {
                        map.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_POSITION, 12.0f))
                    }
                }
        }
    }


    //extension functions
    private fun <V : View?> BottomSheetBehavior<V>.setBottomsheetPeekHeight(height: Int){
        this.peekHeight = (height * resources.displayMetrics.density).toInt()
        bottomSheetBehavior.setPeekHeight(peekHeight, true)
    }

    private fun <V: View?> BottomSheetBehavior<V>.isHidden(): Boolean {
        return this.state == STATE_HIDDEN
    }

    private fun GoogleMap.executeIfInitialized(execute: () -> Unit){
        if(::map.isInitialized){
            execute()
        }
    }

    private fun OnBackPressedCallback.setEnable(enabled: Boolean){
        this.isEnabled = enabled
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
        when(enabled) {
            true -> {
                if(::pointSelectionMarker.isInitialized) {
                    pointSelectionMarker.isVisible = false //hide selection marker
                }
                //binding.navigationItem.root.visibility = View.VISIBLE //show navigation item on top
                binding.navigationItem.root.animate().alpha(1.0f)
            }
            else -> {
                binding.navigationItem.root.animate().alpha(0.0f)
                if(::pointSelectionMarker.isInitialized) {
                    pointSelectionMarker.also {
                        it.isVisible = true
                        lifecycleScope.launch { map.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 16.0f)) }
                    }
                }
                bottomSheetBehavior.state = STATE_COLLAPSED
            }
        }
    }

}
