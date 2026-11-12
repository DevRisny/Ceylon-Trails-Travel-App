package com.risny.trails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.risny.trails.core.Category
import com.risny.trails.core.Destination
import com.risny.trails.core.Destinations
import com.risny.trails.core.Explore
import com.risny.trails.core.ExploreQuery
import com.risny.trails.core.Favorites
import com.risny.trails.core.SortOrder
import com.risny.trails.core.TravelRepository
import com.risny.trails.data.FavoritesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class ExploreViewModel(private val repo: TravelRepository, private val store: FavoritesStore, val month: Int = LocalDate.now().monthValue) : ViewModel() {

    data class UiState(val query: ExploreQuery, val items: List<Destination>, val favorites: Favorites, val inSeasonNow: List<Destination>)

    private val query = MutableStateFlow(ExploreQuery())

    val state: StateFlow<UiState> = combine(query, store.favorites) { q, fav ->
        UiState(q, Explore.apply(Destinations.ALL, q, month), fav, Destinations.ALL.filter { it.inSeason(month) && it.bestMonths.size < 12 })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState(ExploreQuery(), Explore.apply(Destinations.ALL, ExploreQuery(), month), store.favorites.value, emptyList()))

    fun setText(t: String) { query.value = query.value.copy(text = t) }
    fun setCategory(c: Category?) { query.value = query.value.copy(category = c) }
    fun setInSeason(on: Boolean) { query.value = query.value.copy(inSeasonOnly = on) }
    fun setSort(s: SortOrder) { query.value = query.value.copy(sort = s) }
    fun clear() { query.value = ExploreQuery(sort = query.value.sort) }
    fun toggle(d: Destination) = store.toggle(d.favoriteKey)

    suspend fun photoUrl(d: Destination): String? = repo.place(d.wikiTitle).getOrNull()?.thumbnailUrl
    fun cachedPhotoUrl(d: Destination): String? = repo.cachedPlace(d.wikiTitle)?.thumbnailUrl
}
