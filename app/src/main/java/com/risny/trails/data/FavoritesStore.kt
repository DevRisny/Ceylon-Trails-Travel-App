package com.risny.trails.data

import android.content.Context
import com.risny.trails.core.Favorites
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Saved places, persisted in SharedPreferences and observable as a StateFlow. */
class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(Favorites.decode(prefs.getString(KEY, null)))

    val favorites: StateFlow<Favorites> = state.asStateFlow()

    /** Returns true when the item is now saved. */
    fun toggle(key: String): Boolean {
        val next = state.value.toggle(key)
        state.value = next
        prefs.edit().putString(KEY, next.encode()).apply()
        return key in next
    }

    private companion object { const val KEY = "keys" }
}
