package com.jvoyatz.beatit.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoursquareApiService {
    @GET("/v3/places/search")
    suspend fun getNearbyPlaces(
        @Query("query") query: String, @Query("ll") ll: String, @Query("radius") radius: Int = 100,
        /*@Query("categories") categories: String= FOURSQUARE_PLACE_CATEGORIES.joinToString { "$it," }*/
    ): Response<PlaceResultsDTO>


    @GET("/v3/places/{fsq_id}/photos")
    suspend fun getPlacePhotos(
        @Path("fsq_id") id: String,
        @Query("limit") limit: Int = 2,
    ):Response<List<PlacePhotoDTO>>

}