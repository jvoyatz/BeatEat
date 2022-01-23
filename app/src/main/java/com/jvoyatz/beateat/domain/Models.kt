package com.jvoyatz.beateat.domain

import com.squareup.moshi.Json

data class Place(
    val categories: List<PlaceCategory> = listOf(),
    val distance: Int = 0,
    val fsqId: String = "",
    val location: Location = Location(),
    val name: String = "",
    val placePhotoUrl: String? = ""){

    val joinedCategories: String
        get() {
            return categories.joinToString { it.name }
        }
    }

data class PlaceCategory(
    val icon: String = "",
    val id: Int = 0,
    val name: String = ""
)


data class Location(
    val address: String = "",
    val country: String = "",
    val crossStreet: String = "",
    val locality: String = "",
    val postcode: String = "",
    val region: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
