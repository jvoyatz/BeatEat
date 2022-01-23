package com.jvoyatz.beateat.common

import android.Manifest
import com.google.android.gms.maps.model.LatLng


val DEFAULT_POSITION = LatLng(37.9760314,23.7339412)
const val URL = "https://api.foursquare.com/"
const val TOKEN = "fsq3BGDj5HDjVjny9ZP6K0atxPAioyeSSakCKhyONkjuaqw="
val FOURSQUARE_PLACE_CATEGORIES = listOf("13052", "13053", "13054", "13065")

val PERMISSIONS  = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)


