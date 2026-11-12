package com.risny.trails.core

/** What kind of trip a place is good for. */
enum class Category(val label: String, val emoji: String) {
    BEACH("Beaches", "🏖️"),
    HERITAGE("Heritage", "🏛️"),
    HILLS("Hill country", "⛰️"),
    WILDLIFE("Wildlife", "🐘"),
    ADVENTURE("Adventure", "🧗"),
    CITY("Cities", "🌆"),
}

/**
 * A curated Sri Lankan destination. The description and photo come live from Wikipedia
 * ([wikiTitle]); the weather comes live from Open-Meteo ([lat], [lon]).
 *
 * @param bestMonths typical best months to visit (1 = January), from the monsoon pattern of the region
 */
data class Destination(
    val id: String,
    val name: String,
    val wikiTitle: String,
    val province: String,
    val categories: Set<Category>,
    val lat: Double,
    val lon: Double,
    val bestMonths: Set<Int>,
    val tagline: String,
    val highlights: List<String>,
) {
    init {
        require(categories.isNotEmpty()) { "$id needs a category" }
        require(bestMonths.all { it in 1..12 }) { "$id has an invalid month" }
        require(lat in 5.8..10.0 && lon in 79.5..82.0) { "$id is not in Sri Lanka" }
    }

    fun inSeason(month: Int): Boolean = month in bestMonths

    val favoriteKey: String get() = "dest:$id"
}
