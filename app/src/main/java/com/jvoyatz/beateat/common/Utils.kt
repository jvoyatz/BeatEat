package com.jvoyatz.beateat.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jvoyatz.beateat.R

object Utils {
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
    }

    fun getMarkerOptions(context: Context, latLng: LatLng)= MarkerOptions()
                .title("your current location")
                .position(latLng)
                .draggable(false)
                .visible(false)

    fun getRationaleDialog(context: Context, ok: () -> Unit, cancel: () -> Unit):AlertDialog {
        return MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.warning))
                .setMessage(context.getString(R.string.location_permissions_not_granted_message))
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, which -> cancel() }
                .setPositiveButton(context.getString(R.string.ok)) { dialog, which -> ok()}
            .create()
    }

}