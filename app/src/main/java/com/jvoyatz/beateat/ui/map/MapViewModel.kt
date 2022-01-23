package com.jvoyatz.beateat.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jvoyatz.beateat.common.Resource
import com.jvoyatz.beateat.domain.Place
import com.jvoyatz.beateat.domain.usecases.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MapViewModel"

@HiltViewModel
class MapViewModel @Inject constructor(private val useCases: UseCases) : ViewModel() {
    private val _placesState: MutableStateFlow<Resource<List<Place>>> = MutableStateFlow(Resource.Init())
    val placeState: StateFlow<Resource<List<Place>>> = _placesState

    private val _selectedPlaceState:MutableStateFlow<Pair<Place?, Long>> = MutableStateFlow(Pair(null, System.currentTimeMillis()))
    val selectedPlaceState: StateFlow<Pair<Place?, Long>> = _selectedPlaceState

    fun searchForPlaces(latLonStr: String){
        viewModelScope.launch {
            useCases.searchPlacesInteractor(latLonStr)
                .onStart {
                    emit(Resource.Loading())
                    delay(350)
                }
                .collect {
                try {
                    _placesState.value = it
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun onPlaceSelect(place: Place){
        _selectedPlaceState.value = Pair(place, System.currentTimeMillis())
    }

    fun onPlaceSelectDone(){
        _selectedPlaceState.value = Pair(null, System.currentTimeMillis())
    }
    fun isPlaceSelected() = _selectedPlaceState.value.first != null
}