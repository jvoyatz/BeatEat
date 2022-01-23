package com.jvoyatz.beateat.data

import android.util.Log
import com.jvoyatz.beateat.common.Resource
import com.jvoyatz.beateat.data.network.FoursquareApiService
import com.jvoyatz.beateat.data.network.PlacePhotoDTO
import com.jvoyatz.beateat.data.network.toDomainModels
import com.jvoyatz.beateat.domain.Place
import com.jvoyatz.beateat.domain.repository.PlacesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.Exception

class PlacesRepositoryImpl(private val foursquareApi: FoursquareApiService, val ioDispatcher: CoroutineDispatcher): PlacesRepository {
    private val TAG = "PlacesRepositoryImpl"

    override fun getNearbyPlaces(latLonStr: String): Flow<Resource<List<Place>>> {
        return flow{
            try{
                val response = foursquareApi.getNearbyPlaces("food", latLonStr, 300)
                if(response.isSuccessful && response.body() != null){
                    response.body()?.let {
                        emit(Resource.Success(it.results.toDomainModels()))
                        it.results.forEach { place ->
                            val placePhotosResponse = foursquareApi.getPlacePhotos(place.fsqId)
                            if(placePhotosResponse.isSuccessful && !placePhotosResponse.body().isNullOrEmpty()){
                                val photos = placePhotosResponse.body()

                                photos?.get(0)?.let { photo ->
                                    place.placePhotoUrl = "${photo.prefix}400x400${photo.suffix}"
                                }
                            }
                        }
                        emit(Resource.Success(it.results.toDomainModels()))
                    } ?: emit(Resource.Success(listOf()))
                }else{
                    //emit(Resource.Error(
                    response.errorBody()?.let {
                        emit(Resource.Error(it.string()))
                    } ?: emit(Resource.Error("unknown exception"))
                }
            }catch (e: Exception){
                e.printStackTrace()
                emit(Resource.Error.create(e))
            }
        }.flowOn(ioDispatcher)
    }
}