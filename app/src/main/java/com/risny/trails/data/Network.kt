package com.risny.trails.data

import android.content.Context
import com.risny.trails.core.Countries
import com.risny.trails.core.CountryDto
import com.risny.trails.core.ForecastDto
import com.risny.trails.core.TravelApi
import com.risny.trails.core.WikiSummaryDto
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

/** Wikipedia REST API: page summaries with an extract and a photo. */
interface WikiService {
    @GET("page/summary/{title}")
    suspend fun summary(@Path("title") title: String): WikiSummaryDto
}

/** Open-Meteo: current weather and a 7-day forecast, no API key needed. */
interface WeatherService {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 7,
    ): ForecastDto
}

/** REST Countries v3.1. */
interface CountriesService {
    @GET("v3.1/all")
    suspend fun all(@Query("fields") fields: String = Countries.FIELDS): List<CountryDto>
}

object Network {
    /** Wikimedia asks every client to identify itself; generic agents can be blocked. */
    const val USER_AGENT = "CeylonTrails/1.0 (Android travel app; https://github.com/DevRisny/ceylon-trails)"

    private var client: OkHttpClient? = null

    fun client(context: Context): OkHttpClient = client ?: OkHttpClient.Builder()
        .cache(Cache(File(context.cacheDir, "http"), 20L * 1024 * 1024)) // responses are reused offline
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain -> chain.proceed(chain.request().newBuilder().header("User-Agent", USER_AGENT).build()) }
        .build().also { client = it }

    private fun retrofit(context: Context, baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client(context))
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun api(context: Context): TravelApi {
        val wiki = retrofit(context, "https://en.wikipedia.org/api/rest_v1/").create(WikiService::class.java)
        val weather = retrofit(context, "https://api.open-meteo.com/").create(WeatherService::class.java)
        val countries = retrofit(context, "https://restcountries.com/").create(CountriesService::class.java)
        return object : TravelApi {
            override suspend fun summary(title: String) = wiki.summary(title)
            override suspend fun forecast(lat: Double, lon: Double) = weather.forecast(lat, lon)
            override suspend fun countries() = countries.all()
        }
    }
}
