package com.jvoyatz.beatit.data

import android.util.Log
import com.jvoyatz.beatit.data.network.FoursquareApiService
import com.jvoyatz.beatit.data.network.ResultDTO
import com.jvoyatz.beatit.data.network.toDomainModels
import com.jvoyatz.beatit.domain.Place
import com.jvoyatz.beatit.domain.repository.PlacesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlacesRepositoryImpl(
    private val foursquareApi: FoursquareApiService,
    val ioDispatcher: CoroutineDispatcher
) : PlacesRepository {
    private val TAG = "PlacesRepositoryImpl"

    override fun getNearbyPlaces(latLonStr: String): Flow<List<Place>> {
        return flow {
            val response = foursquareApi.getNearbyPlaces("food", latLonStr, 300)
            when (response.isSuccessful) {
                true -> {
                    response.body()?.let {
                        emit(it.results.toDomainModels()) //quick emit to show results
                        it.results.forEach { place ->
                            getPhotoForFsqID(place.fsqId, place)
                            Log.d(TAG, "getNearbyPlaces: url[${place.placePhotoUrl}]")
                        }
                        emit(it.results.toDomainModels()) //post again with url
                    }
                }
                else -> {
                    throw Exception(response.errorBody()?.string())
                }
            }
        }.flowOn(ioDispatcher)
    }

    private suspend fun getPhotoForFsqID(fsqId: String, place: ResultDTO) {
        // withContext(ioDispatcher) {
        val response = foursquareApi.getPlacePhotos(fsqId)
        when (response.isSuccessful) {
            true -> {
                response.body()?.let {
                    if (it.isNotEmpty())
                        it[0].let { photo ->
                            place.placePhotoUrl = "${photo.prefix}400x400${photo.suffix}"
                        }
                }
            }
            else -> {
                //print the error but do not throw the exception
                Log.d(TAG, "getPhotoForFsqID: ${response.errorBody()?.string()}")
            }
        }
    }
}