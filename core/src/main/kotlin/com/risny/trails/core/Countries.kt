package com.risny.trails.core

import java.util.Locale
import kotlin.math.roundToLong

/**
 * REST Countries v3.1 (https://restcountries.com). The /all endpoint allows at most 10 fields:
 * name, cca2, capital, region, subregion, population, currencies, languages, flags, timezones.
 */
data class CountryDto(
    val name: Name? = null,
    val cca2: String? = null,
    val capital: List<String>? = null,
    val region: String? = null,
    val subregion: String? = null,
    val population: Long? = null,
    val currencies: Map<String, Currency>? = null,
    val languages: Map<String, String>? = null,
    val flags: Flags? = null,
    val timezones: List<String>? = null,
) {
    data class Name(val common: String? = null, val official: String? = null)
    data class Currency(val name: String? = null, val symbol: String? = null)
    data class Flags(val png: String? = null, val svg: String? = null, val alt: String? = null)
}

data class Country(
    val code: String,
    val name: String,
    val officialName: String,
    val capital: String?,
    val region: String,
    val subregion: String?,
    val population: Long,
    val currencies: List<String>,
    val languages: List<String>,
    val flagUrl: String?,
    val flagAlt: String?,
    val timezones: List<String>,
) {
    val favoriteKey: String get() = "country:$code"
}

enum class CountrySort(val label: String) { NAME("A to Z"), POPULATION("Population") }

object Countries {
    const val FIELDS = "name,cca2,capital,region,subregion,population,currencies,languages,flags,timezones"

    fun toCountry(d: CountryDto): Country? {
        val code = d.cca2?.uppercase() ?: return null
        val name = d.name?.common ?: return null
        return Country(
            code = code,
            name = name,
            officialName = d.name.official ?: name,
            capital = d.capital?.firstOrNull(),
            region = d.region?.takeIf { it.isNotBlank() } ?: "Other",
            subregion = d.subregion?.takeIf { it.isNotBlank() },
            population = d.population ?: 0,
            currencies = d.currencies.orEmpty().map { (code, c) -> listOfNotNull(c.name, c.symbol?.let { "($it)" }, "· $code").joinToString(" ") }.sorted(),
            languages = d.languages.orEmpty().values.sorted(),
            flagUrl = d.flags?.png,
            flagAlt = d.flags?.alt,
            timezones = d.timezones.orEmpty(),
        )
    }

    fun fromDtos(list: List<CountryDto>): List<Country> = list.mapNotNull(::toCountry).distinctBy { it.code }.sortedBy { it.name }

    fun regions(all: List<Country>): List<String> = all.map { it.region }.distinct().sorted()

    fun filter(all: List<Country>, text: String, region: String?, sort: CountrySort): List<Country> {
        val q = Explore.normalize(text)
        val out = all.filter { c ->
            (region == null || c.region == region) &&
                (q.isEmpty() || Explore.normalize("${c.name} ${c.officialName} ${c.capital.orEmpty()} ${c.code}").contains(q))
        }
        return when (sort) {
            CountrySort.NAME -> out.sortedBy { it.name }
            CountrySort.POPULATION -> out.sortedByDescending { it.population }
        }
    }

    /** 21_916_000 → "21.9 million", 341_250 → "341,250". */
    fun population(n: Long): String = when {
        n >= 1_000_000_000 -> String.format(Locale.ENGLISH, "%.2f billion", n / 1e9)
        n >= 1_000_000 -> String.format(Locale.ENGLISH, "%.1f million", n / 1e6)
        else -> String.format(Locale.ENGLISH, "%,d", n)
    }

    /** Compact form for list rows: "21.9M", "1.43B", "341K". */
    fun populationShort(n: Long): String = when {
        n >= 1_000_000_000 -> String.format(Locale.ENGLISH, "%.2fB", n / 1e9)
        n >= 1_000_000 -> String.format(Locale.ENGLISH, "%.1fM", n / 1e6)
        n >= 1_000 -> "${(n / 1e3).roundToLong()}K"
        else -> n.toString()
    }

    /** "🇱🇰" from "LK" (regional indicator symbols). */
    fun flagEmoji(code: String): String =
        if (code.length != 2 || !code.all { it in 'A'..'Z' }) "🏳️"
        else String(Character.toChars(0x1F1E6 + (code[0] - 'A'))) + String(Character.toChars(0x1F1E6 + (code[1] - 'A')))
}
