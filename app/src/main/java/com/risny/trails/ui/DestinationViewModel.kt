package com.risny.trails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.risny.trails.core.Destination
import com.risny.trails.core.Load
import com.risny.trails.core.PlaceInfo
import com.risny.trails.core.TravelRepository
import com.risny.trails.core.Weather
import com.risny.trails.core.friendlyError
import com.risny.trails.data.FavoritesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/** Wikipedia text + photo and the live weather, each loading (and failing) independently. */
class DestinationViewModel(val destination: Destination, private val repo: TravelRepository, private val store: FavoritesStore) : ViewModel() {
    private val _place = MutableStateFlow<Load<PlaceInfo>>(Load.Loading)
    private val _weather = MutableStateFlow<Load<Weather>>(Load.Loading)
    val place: StateFlow<Load<PlaceInfo>> = _place
    val weather: StateFlow<Load<Weather>> = _weather
    val saved: StateFlow<Boolean> = store.favorites.map { destination.favoriteKey in it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, destination.favoriteKey in store.favorites.value)

    init {
        loadPlace()
        loadWeather()
    }

    fun loadPlace() {
        _place.value = Load.Loading
        viewModelScope.launch {
            _place.value = repo.place(destination.wikiTitle).fold({ Load.Ready(it) }, { Load.Failed(friendlyError(it)) })
        }
    }

    fun loadWeather() {
        _weather.value = Load.Loading
        viewModelScope.launch {
            _weather.value = repo.weather(destination).fold({ Load.Ready(it) }, { Load.Failed(friendlyError(it)) })
        }
    }

    fun toggle(): Boolean = store.toggle(destination.favoriteKey)
}
