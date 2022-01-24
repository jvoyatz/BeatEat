package com.jvoyatz.beatit.common

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jvoyatz.beatit.R

fun LatLng.toFourSquareLL():String{
    return "${this.latitude},${this.longitude}"
}

object Utils {
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission )== PackageManager.PERMISSION_GRANTED
    }

    fun getMarkerOptions(context: Context, latLng: LatLng, resId: Int)= MarkerOptions()
                //.title("your current location")
                .position(latLng)
                .draggable(false)
                .icon(getBitmap(context, resId))
               // .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location))
                .visible(false)

    fun getRationaleDialog(context: Context, ok: () -> Unit, cancel: () -> Unit):AlertDialog {
        return MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.warning))
                .setMessage(context.getString(R.string.location_permissions_not_granted_message))
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, which -> cancel() }
                .setPositiveButton(context.getString(R.string.ok)) { dialog, which -> ok()}
            .create()
    }

    private fun getBitmap(context: Context, resId: Int): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, resId)
        drawable?.let {
            drawable.bounds = Rect(0, 0, it.intrinsicWidth, it.intrinsicHeight)

            val bitmap = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.RGBA_F16)
            val canvas = Canvas(bitmap)
            it.draw(canvas)

            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        return null
    }

}