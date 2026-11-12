package com.risny.trails.core

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/** Open-Meteo forecast response (https://open-meteo.com, free, no API key). */
data class ForecastDto(
    val current: Current? = null,
    val daily: Daily? = null,
) {
    data class Current(
        val time: String? = null,
        @SerializedName("temperature_2m") val temperature: Double? = null,
        @SerializedName("apparent_temperature") val feelsLike: Double? = null,
        @SerializedName("relative_humidity_2m") val humidity: Int? = null,
        @SerializedName("weather_code") val code: Int? = null,
        @SerializedName("wind_speed_10m") val wind: Double? = null,
    )

    data class Daily(
        val time: List<String> = emptyList(),
        @SerializedName("weather_code") val code: List<Int?> = emptyList(),
        @SerializedName("temperature_2m_max") val max: List<Double?> = emptyList(),
        @SerializedName("temperature_2m_min") val min: List<Double?> = emptyList(),
        @SerializedName("precipitation_probability_max") val rainChance: List<Int?> = emptyList(),
    )
}

data class DayForecast(val date: LocalDate, val dayLabel: String, val code: Int, val max: Int, val min: Int, val rainChance: Int?)

data class Weather(val temperature: Int, val feelsLike: Int?, val humidity: Int?, val windKmh: Int?, val code: Int, val days: List<DayForecast>) {
    val summary: String get() = WeatherCodes.label(code)
    val emoji: String get() = WeatherCodes.emoji(code)
}

/** WMO weather interpretation codes used by Open-Meteo. */
object WeatherCodes {
    fun label(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mostly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61 -> "Light rain"
        63 -> "Rain"
        65 -> "Heavy rain"
        66, 67 -> "Freezing rain"
        71, 73, 75, 77 -> "Snow"
        80 -> "Light showers"
        81 -> "Showers"
        82 -> "Heavy showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Unknown"
    }

    fun emoji(code: Int): String = when (code) {
        0 -> "☀️"
        1 -> "🌤️"
        2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..57, 61, 80 -> "🌦️"
        63, 65, 66, 67, 81, 82 -> "🌧️"
        in 71..77, 85, 86 -> "❄️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }

    /** A short travel tip for the current conditions. */
    fun tip(code: Int, rainChance: Int?): String = when {
        code >= 95 -> "Thunderstorms likely: avoid exposed hikes and boat trips."
        code in 61..67 || code in 80..82 || (rainChance ?: 0) >= 70 -> "Pack a rain jacket; mornings are usually drier."
        code in 45..48 -> "Misty: views may be hidden until late morning."
        else -> "Good conditions for sightseeing. Carry water and sunscreen."
    }

    fun toWeather(dto: ForecastDto): Weather? {
        val c = dto.current ?: return null
        val t = c.temperature ?: return null
        val d = dto.daily
        val days = if (d == null) emptyList() else d.time.indices.mapNotNull { i ->
            val date = runCatching { LocalDate.parse(d.time[i]) }.getOrNull() ?: return@mapNotNull null
            val max = d.max.getOrNull(i) ?: return@mapNotNull null
            val min = d.min.getOrNull(i) ?: return@mapNotNull null
            DayForecast(date, if (i == 0) "Today" else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                d.code.getOrNull(i) ?: 0, max.roundToInt(), min.roundToInt(), d.rainChance.getOrNull(i))
        }
        return Weather(t.roundToInt(), c.feelsLike?.roundToInt(), c.humidity, c.wind?.roundToInt(), c.code ?: 0, days)
    }
}
