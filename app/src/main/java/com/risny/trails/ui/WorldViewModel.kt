package com.risny.trails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.risny.trails.core.Countries
import com.risny.trails.core.Country
import com.risny.trails.core.CountrySort
import com.risny.trails.core.Favorites
import com.risny.trails.core.Load
import com.risny.trails.core.TravelRepository
import com.risny.trails.core.friendlyError
import com.risny.trails.data.FavoritesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorldViewModel(private val repo: TravelRepository, private val store: FavoritesStore) : ViewModel() {
    data class Filters(val text: String = "", val region: String? = null, val sort: CountrySort = CountrySort.NAME)
    data class UiState(val load: Load<List<Country>>, val visible: List<Country>, val regions: List<String>, val filters: Filters, val favorites: Favorites, val refreshing: Boolean)

    private val load = MutableStateFlow<Load<List<Country>>>(Load.Loading)
    private val filters = MutableStateFlow(Filters())
    private val refreshing = MutableStateFlow(false)

    val state: StateFlow<UiState> = combine(load, filters, store.favorites, refreshing) { l, f, fav, r ->
        val all = (l as? Load.Ready)?.value.orEmpty()
        UiState(l, Countries.filter(all, f.text, f.region, f.sort), Countries.regions(all), f, fav, r)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState(Load.Loading, emptyList(), emptyList(), Filters(), store.favorites.value, false))

    init { fetch(false) }

    fun fetch(force: Boolean) {
        if (load.value !is Load.Ready) load.value = Load.Loading else refreshing.value = true
        viewModelScope.launch {
            repo.countries(force).fold(
                { load.value = Load.Ready(it) },
                { if (load.value !is Load.Ready) load.value = Load.Failed(friendlyError(it)) },
            )
            refreshing.value = false
        }
    }

    fun setText(t: String) { filters.value = filters.value.copy(text = t) }
    fun setRegion(r: String?) { filters.value = filters.value.copy(region = r) }
    fun setSort(s: CountrySort) { filters.value = filters.value.copy(sort = s) }
    fun toggle(c: Country) = store.toggle(c.favoriteKey)
}
