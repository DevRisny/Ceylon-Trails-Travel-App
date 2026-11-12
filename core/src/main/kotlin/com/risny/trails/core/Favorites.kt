package com.risny.trails.core

/** Saved places as a set of keys ("dest:ella", "country:JP"), stored as one line of text. */
data class Favorites(val keys: Set<String> = emptySet()) {
    operator fun contains(key: String) = key in keys

    fun toggle(key: String): Favorites = Favorites(if (key in keys) keys - key else keys + key)

    fun destinations(all: List<Destination> = Destinations.ALL): List<Destination> = all.filter { it.favoriteKey in keys }

    fun countryCodes(): List<String> = keys.filter { it.startsWith("country:") }.map { it.removePrefix("country:") }.sorted()

    fun encode(): String = keys.sorted().joinToString("|")

    companion object {
        fun decode(raw: String?): Favorites =
            Favorites(raw.orEmpty().split('|').map { it.trim() }.filter { it.matches(Regex("(dest|country):[A-Za-z0-9-]{1,40}")) }.toSet())
    }
}
