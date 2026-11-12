package com.risny.trails.core

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CoreTest {
    private val gson = Gson()
    private fun res(name: String) = javaClass.classLoader!!.getResource(name)!!.readText()

    // ---------------------------------------------------------------- destinations & filters

    @Test fun curatedDataIsValid() {
        val all = Destinations.ALL
        assertTrue(all.size >= 25)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        assertEquals(all.size, all.map { it.wikiTitle }.toSet().size)
        assertTrue(all.all { it.highlights.isNotEmpty() && it.tagline.isNotBlank() })
        Category.entries.forEach { c -> assertTrue("$c has places", all.any { c in it.categories }) }
    }

    @Test fun filterByCategoryProvinceSeasonAndText() {
        val all = Destinations.ALL
        val beaches = Explore.apply(all, ExploreQuery(category = Category.BEACH), month = 1)
        assertTrue(beaches.isNotEmpty() && beaches.all { Category.BEACH in it.categories })
        val southern = Explore.apply(all, ExploreQuery(province = "Southern"), 1)
        assertTrue(southern.all { it.province == "Southern" })
        val julyInSeason = Explore.apply(all, ExploreQuery(category = Category.BEACH, inSeasonOnly = true), 7)
        assertTrue(julyInSeason.any { it.id == "arugam-bay" })
        assertFalse(julyInSeason.any { it.id == "mirissa" })
        assertEquals(listOf("ella"), Explore.apply(all, ExploreQuery(text = "nine arch"), 1).map { it.id })
        assertEquals(listOf("kandy"), Explore.apply(all, ExploreQuery(text = "TOOTH temple"), 1).map { it.id })
        assertTrue(Explore.apply(all, ExploreQuery(text = "zzz"), 1).isEmpty())
    }

    @Test fun recommendedSortPutsInSeasonPlacesFirst() {
        val list = Explore.apply(Destinations.ALL, ExploreQuery(), month = 8)
        val firstOut = list.indexOfFirst { !it.inSeason(8) }
        assertTrue(list.take(firstOut).all { it.inSeason(8) })
        assertTrue(list.drop(firstOut).none { it.inSeason(8) })
    }

    @Test fun otherSortsAndDistances() {
        val near = Explore.apply(Destinations.ALL, ExploreQuery(sort = SortOrder.NEAREST), 1)
        assertEquals("colombo", near.first().id)
        assertEquals("jaffna", Explore.apply(Destinations.ALL, ExploreQuery(sort = SortOrder.NORTH_TO_SOUTH), 1).first().id)
        val kandy = Explore.kmFromColombo(Destinations.byId("kandy")!!)
        assertTrue("Colombo-Kandy is about 95 km in a straight line, got $kandy", kandy in 85..105)
        assertEquals("In Colombo", Explore.distanceLabel(Destinations.byId("colombo")!!))
        assertEquals("aland islands", Explore.normalize(" Åland Islands "))
    }

    // ---------------------------------------------------------------- seasons

    @Test fun seasonLabelsWrapAroundTheYear() {
        assertEquals("Dec – Apr", Seasons.label(setOf(12, 1, 2, 3, 4)))
        assertEquals("Jan – Apr, Jul – Aug", Seasons.label(setOf(1, 2, 3, 4, 7, 8)))
        assertEquals("All year", Seasons.label((1..12).toSet()))
        assertEquals("Aug", Seasons.label(setOf(8)))
        assertEquals("Nov – Apr", Seasons.label(setOf(11, 12, 1, 2, 3, 4)))
        assertEquals("Any time", Seasons.label(emptySet()))
    }

    @Test fun seasonStatus() {
        val mirissa = Destinations.byId("mirissa")!!
        assertEquals("In season now", Seasons.status(mirissa, 1))
        assertEquals("Season starts next month", Seasons.status(mirissa, 10))
        assertEquals("Best from Nov", Seasons.status(mirissa, 6))
        assertEquals("Good all year", Seasons.status(Destinations.byId("udawalawe")!!, 6))
        assertEquals(0, Seasons.monthsUntil(setOf(12), 12))
        assertEquals(1, Seasons.monthsUntil(setOf(1), 12))
    }

    // ---------------------------------------------------------------- API models

    @Test fun parsesWikipediaSummary() {
        val p = Wiki.toPlace(gson.fromJson(res("wiki_sigiriya.json"), WikiSummaryDto::class.java), "Sigiriya")
        assertEquals("Sigiriya", p.title)
        assertEquals("Ancient rock fortress in Sri Lanka", p.description)
        assertTrue(p.extract.startsWith("Sigiriya or Sinhagiri"))
        assertEquals("https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Beauty_of_Sigiriya_by_Binuka.jpg/500px-Beauty_of_Sigiriya_by_Binuka.jpg", p.thumbnailUrl)
        assertTrue(p.imageUrl!!.contains("/1280px-"))
        assertEquals("https://en.m.wikipedia.org/wiki/Sigiriya", p.pageUrl)
        assertEquals("Ella,_Sri_Lanka", Wiki.pathTitle("Ella, Sri Lanka"))
        val small = WikiSummaryDto(originalImage = WikiSummaryDto.Image("https://x/small.jpg", 400, 300), thumbnail = WikiSummaryDto.Image("https://x/thumb/a/b/s.jpg/320px-s.jpg"))
        assertEquals("never upscale a small original", "https://x/small.jpg", Wiki.sized(small, 500))
        assertEquals("No description available.", Wiki.toPlace(WikiSummaryDto(), "X").extract)
    }

    @Test fun parsesOpenMeteoForecast() {
        val w = WeatherCodes.toWeather(gson.fromJson(res("forecast.json"), ForecastDto::class.java))!!
        assertEquals(22, w.temperature)
        assertEquals(23, w.feelsLike)
        assertEquals(88, w.humidity)
        assertEquals("Light showers", w.summary)
        assertEquals(2, w.days.size) // the day with a missing minimum is skipped
        assertEquals("Today", w.days[0].dayLabel)
        assertEquals("Sat", w.days[1].dayLabel)
        assertEquals(75, w.days[0].rainChance)
        assertNull(WeatherCodes.toWeather(ForecastDto()))
    }

    @Test fun weatherCodes() {
        assertEquals("Clear sky", WeatherCodes.label(0))
        assertEquals("Thunderstorm", WeatherCodes.label(95))
        assertEquals("⛈️", WeatherCodes.emoji(99))
        assertTrue(WeatherCodes.tip(95, 10).contains("Thunderstorm"))
        assertTrue(WeatherCodes.tip(1, 80).contains("rain jacket"))
        assertTrue(WeatherCodes.tip(1, 10).contains("Good conditions"))
    }

    @Test fun parsesRestCountries() {
        val type = object : TypeToken<List<CountryDto>>() {}.type
        val list = Countries.fromDtos(gson.fromJson<List<CountryDto>>(res("countries.json"), type))
        assertEquals(listOf("Antarctica", "Japan", "Sri Lanka", "Åland Islands"), list.map { it.name }) // the broken entry is dropped
        val lk = list.first { it.code == "LK" }
        assertEquals("Sri Jayawardenepura Kotte", lk.capital)
        assertEquals(listOf("Sinhala", "Tamil"), lk.languages)
        assertEquals(listOf("Sri Lankan rupee (Rs) · LKR"), lk.currencies)
        assertEquals("21.8 million", Countries.population(lk.population))
        assertEquals("country:LK", lk.favoriteKey)
        assertNull(list.first { it.code == "AQ" }.capital)
        assertEquals(listOf("Antarctic", "Asia", "Europe"), Countries.regions(list))
        assertEquals(listOf("JP", "LK"), Countries.filter(list, "", "Asia", CountrySort.NAME).map { it.code })
        assertEquals(listOf("JP", "LK"), Countries.filter(list, "", "Asia", CountrySort.POPULATION).map { it.code })
        assertEquals(listOf("AX"), Countries.filter(list, "aland", null, CountrySort.NAME).map { it.code })
        assertEquals(listOf("JP"), Countries.filter(list, "tokyo", null, CountrySort.NAME).map { it.code })
    }

    @Test fun numberAndFlagFormatting() {
        assertEquals("1.43 billion", Countries.population(1_428_000_000))
        assertEquals("29,458", Countries.population(29_458))
        assertEquals("21.8M", Countries.populationShort(21_763_170))
        assertEquals("29K", Countries.populationShort(29_458))
        assertEquals("🇱🇰", Countries.flagEmoji("LK"))
        assertEquals("🏳️", Countries.flagEmoji("l"))
    }

    // ---------------------------------------------------------------- favourites

    @Test fun favoritesRoundTripAndRejectJunk() {
        val f = Favorites().toggle("dest:ella").toggle("country:JP").toggle("dest:sigiriya").toggle("dest:ella")
        assertEquals(setOf("country:JP", "dest:sigiriya"), f.keys)
        assertEquals(f, Favorites.decode(f.encode()))
        assertEquals(listOf("sigiriya"), f.destinations().map { it.id })
        assertEquals(listOf("JP"), f.countryCodes())
        assertEquals(Favorites(setOf("dest:ella")), Favorites.decode("dest:ella||<script>|country:"))
        assertEquals(Favorites(), Favorites.decode(null))
    }

    // ---------------------------------------------------------------- repository

    private class FakeApi : TravelApi {
        var summaryCalls = 0
        var forecastCalls = 0
        var countryCalls = 0
        var fail: Throwable? = null
        override suspend fun summary(title: String): WikiSummaryDto {
            summaryCalls++; fail?.let { throw it }
            if (title == "Georgia") return WikiSummaryDto(type = "disambiguation", title = "Georgia", extract = "Georgia may refer to:")
            return WikiSummaryDto(type = "standard", title = title.replace('_', ' '), extract = "About $title")
        }
        override suspend fun forecast(lat: Double, lon: Double): ForecastDto {
            forecastCalls++; fail?.let { throw it }
            return ForecastDto(ForecastDto.Current(temperature = 27.4, code = 1), null)
        }
        override suspend fun countries(): List<CountryDto> { countryCalls++; fail?.let { throw it }; return listOf(CountryDto(CountryDto.Name("Japan"), "JP")) }
    }

    @Test fun repositoryCachesAndReportsFriendlyErrors() = runBlocking {
        val api = FakeApi()
        var now = 0L
        val repo = TravelRepository(api) { now }
        val ella = Destinations.byId("ella")!!
        assertEquals("Ella, Sri Lanka", repo.place(ella.wikiTitle).getOrThrow().title)
        repo.place(ella.wikiTitle)
        assertEquals(1, api.summaryCalls)
        assertNotNull(repo.cachedPlace(ella.wikiTitle))

        assertEquals(27, repo.weather(ella).getOrThrow().temperature)
        now += 10 * 60 * 1000
        repo.weather(ella)
        assertEquals("cached for 15 minutes", 1, api.forecastCalls)
        now += 6 * 60 * 1000
        repo.weather(ella)
        assertEquals(2, api.forecastCalls)

        assertEquals(listOf("JP"), repo.countries().getOrThrow().map { it.code })
        repo.countries()
        assertEquals(1, api.countryCalls)
        repo.countries(force = true)
        assertEquals(2, api.countryCalls)

        val georgia = Country("GE", "Georgia", "Georgia", "Tbilisi", "Asia", null, 3_700_000, emptyList(), emptyList(), null, null, emptyList())
        assertEquals("Georgia (country)", repo.countryPlace(georgia).getOrThrow().title)
        val before = api.summaryCalls
        repo.countryPlace(georgia)
        assertEquals(before, api.summaryCalls)

        api.fail = IOException("no network")
        val failed = repo.place("Galle Fort")
        assertTrue(failed.isFailure)
        assertEquals("You seem to be offline. Check your connection and try again.", friendlyError(failed.exceptionOrNull()!!))
        assertEquals("Something went wrong. Please try again.", friendlyError(IllegalStateException()))
    }
}
