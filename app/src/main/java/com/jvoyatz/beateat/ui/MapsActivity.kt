package com.jvoyatz.beateat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jvoyatz.beateat.R
import com.jvoyatz.beateat.common.PERMISSIONS
import com.jvoyatz.beateat.databinding.ActivityMapsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsActivity : AppCompatActivity() {

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

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController)

        requestLocationPermissions()
    }

    override fun onPause() {
        super.onPause()
        if (noLocationDialog.isShowing) {
            noLocationDialog.dismiss()
        }
    }

    private fun requestLocationPermissions() {
        when {
            (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                //createLocationRequest()
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

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp() || super.onSupportNavigateUp()
    }
}
