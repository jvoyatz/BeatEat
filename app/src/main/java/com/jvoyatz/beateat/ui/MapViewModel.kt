package com.jvoyatz.beateat.ui

import androidx.lifecycle.ViewModel
import com.jvoyatz.beateat.domain.usecases.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(useCases: UseCases) : ViewModel() {
}