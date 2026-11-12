package com.risny.trails.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/** The three web APIs. The app implements this with Retrofit; tests use a fake. */
interface TravelApi {
    suspend fun summary(title: String): WikiSummaryDto
    suspend fun forecast(lat: Double, lon: Double): ForecastDto
    suspend fun countries(): List<CountryDto>
}

/** Loading → Ready / Failed, for any screen section. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Ready<T>(val value: T) : Load<T>
    data class Failed(val message: String) : Load<Nothing>
}

/** Turn any exception into a message a traveller understands. */
fun friendlyError(e: Throwable): String = when {
    e is IOException || e.javaClass.simpleName.contains("UnknownHost") || e.javaClass.simpleName.contains("Timeout") ->
        "You seem to be offline. Check your connection and try again."
    e.javaClass.simpleName == "HttpException" && e.message.orEmpty().contains("404") -> "This page isn't available right now."
    e.javaClass.simpleName == "HttpException" && e.message.orEmpty().contains("429") -> "The service is busy. Please try again in a minute."
    else -> "Something went wrong. Please try again."
}

/**
 * Caches API results in memory for the session (the HTTP layer adds a disk cache), so scrolling
 * back and forth never refetches. Weather is cached for 15 minutes.
 */
class TravelRepository(private val api: TravelApi, private val clock: () -> Long = System::currentTimeMillis) {
    private val places = ConcurrentHashMap<String, PlaceInfo>()
    private val weather = ConcurrentHashMap<String, Pair<Long, Weather>>()
    @Volatile private var countries: List<Country>? = null
    private val countriesLock = Mutex() // one download even if several screens ask at once

    suspend fun place(title: String): Result<PlaceInfo> = runCatching {
        places[title] ?: Wiki.toPlace(api.summary(Wiki.pathTitle(title)), title).also { places[title] = it }
    }

    /**
     * Wikipedia summary for a country. Names like "Georgia" or "Jordan" lead to a disambiguation
     * page, so those retry as "Georgia (country)".
     */
    suspend fun countryPlace(c: Country): Result<PlaceInfo> = runCatching {
        places[c.favoriteKey] ?: run {
            val first = api.summary(Wiki.pathTitle(c.name))
            val dto = if (first.type == "disambiguation") api.summary(Wiki.pathTitle("${c.name} (country)")) else first
            Wiki.toPlace(dto, c.name).also { places[c.favoriteKey] = it }
        }
    }

    suspend fun weather(d: Destination): Result<Weather> = runCatching {
        val cached = weather[d.id]
        if (cached != null && clock() - cached.first < WEATHER_TTL_MS) return@runCatching cached.second
        val w = WeatherCodes.toWeather(api.forecast(d.lat, d.lon)) ?: throw IllegalStateException("No weather data")
        weather[d.id] = clock() to w
        w
    }

    suspend fun countries(force: Boolean = false): Result<List<Country>> = runCatching {
        countriesLock.withLock {
            if (!force) countries?.let { return@withLock it }
            val list = Countries.fromDtos(api.countries())
            if (list.isEmpty()) throw IllegalStateException("No countries returned")
            countries = list
            list
        }
    }

    fun cachedPlace(title: String): PlaceInfo? = places[title]

    companion object {
        const val WEATHER_TTL_MS = 15 * 60 * 1000L
    }
}
