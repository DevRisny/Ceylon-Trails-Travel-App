package com.risny.trails

import android.app.Application
import com.risny.trails.core.TravelRepository
import com.risny.trails.data.FavoritesStore
import com.risny.trails.data.Network

/** Holds the app-wide objects (a tiny service locator instead of a DI framework). */
class TrailsApp : Application() {
    val repository: TravelRepository by lazy { TravelRepository(Network.api(this)) }
    val favorites: FavoritesStore by lazy { FavoritesStore(this) }
}
