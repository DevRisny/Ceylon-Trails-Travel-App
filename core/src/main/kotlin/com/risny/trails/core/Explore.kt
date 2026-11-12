package com.risny.trails.core

import java.text.Normalizer
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class SortOrder(val label: String) { RECOMMENDED("Recommended"), NAME("A to Z"), NEAREST("Nearest to Colombo"), NORTH_TO_SOUTH("North to south") }

/** Everything the Explore screen can filter by. */
data class ExploreQuery(
    val text: String = "",
    val category: Category? = null,
    val province: String? = null,
    val inSeasonOnly: Boolean = false,
    val sort: SortOrder = SortOrder.RECOMMENDED,
)

object Explore {
    const val COLOMBO_LAT = 6.9271
    const val COLOMBO_LON = 79.8612

    /** Lower-case and strip accents, so "galle" finds "Galle" and "kandy" finds "Kandy". */
    fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase().trim()

    fun matches(d: Destination, text: String): Boolean {
        val q = normalize(text)
        if (q.isEmpty()) return true
        val hay = normalize(listOf(d.name, d.province, d.tagline, d.categories.joinToString(" ") { it.label }, d.highlights.joinToString(" ")).joinToString(" "))
        return q.split(Regex("\\s+")).all { hay.contains(it) }
    }

    /** Filter and sort. RECOMMENDED puts places that are in season this month first. */
    fun apply(all: List<Destination>, q: ExploreQuery, month: Int): List<Destination> {
        val filtered = all.filter { d ->
            (q.category == null || q.category in d.categories) &&
                (q.province == null || d.province == q.province) &&
                (!q.inSeasonOnly || d.inSeason(month)) &&
                matches(d, q.text)
        }
        return when (q.sort) {
            SortOrder.RECOMMENDED -> filtered.sortedWith(compareByDescending<Destination> { it.inSeason(month) }.thenBy { it.name })
            SortOrder.NAME -> filtered.sortedBy { it.name }
            SortOrder.NEAREST -> filtered.sortedBy { kmFromColombo(it) }
            SortOrder.NORTH_TO_SOUTH -> filtered.sortedByDescending { it.lat }
        }
    }

    /** Straight-line (great-circle) distance in km. */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * asin(sqrt(a))
    }

    fun kmFromColombo(d: Destination): Int = distanceKm(COLOMBO_LAT, COLOMBO_LON, d.lat, d.lon).roundToInt()

    /** "~120 km from Colombo" (straight line); Colombo itself gets "You're here". */
    fun distanceLabel(d: Destination): String = kmFromColombo(d).let { if (it < 5) "In Colombo" else "~$it km from Colombo" }
}

object Seasons {
    private val SHORT = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun short(month: Int): String = SHORT[month - 1]

    /**
     * Turn a set of months into readable ranges, wrapping around the new year:
     * {12,1,2,3,4} → "Dec – Apr", {1,2,3,4,7,8} → "Jan – Apr, Jul – Aug", all → "All year".
     */
    fun label(months: Set<Int>): String {
        if (months.isEmpty()) return "Any time"
        if (months.size == 12) return "All year"
        // start from a month whose previous month is not included, so runs never get split at December
        val start = (1..12).first { it in months && prev(it) !in months }
        val ranges = mutableListOf<Pair<Int, Int>>()
        var runStart: Int? = null
        var last = start
        for (i in 0 until 12) {
            val m = ((start - 1 + i) % 12) + 1
            if (m in months) {
                if (runStart == null) runStart = m
                last = m
            } else if (runStart != null) {
                ranges += runStart to last
                runStart = null
            }
        }
        if (runStart != null) ranges += runStart to last
        return ranges.distinct().joinToString(", ") { (a, b) -> if (a == b) short(a) else "${short(a)} – ${short(b)}" }
    }

    /** Months until the season starts again (0 = in season now). */
    fun monthsUntil(months: Set<Int>, now: Int): Int {
        if (months.isEmpty()) return 0
        for (i in 0..11) if (((now - 1 + i) % 12) + 1 in months) return i
        return 0
    }

    fun status(d: Destination, now: Int): String {
        val wait = monthsUntil(d.bestMonths, now)
        return when {
            d.bestMonths.size == 12 -> "Good all year"
            wait == 0 -> "In season now"
            wait == 1 -> "Season starts next month"
            else -> "Best from ${short(((now - 1 + wait) % 12) + 1)}"
        }
    }

    private fun prev(m: Int) = if (m == 1) 12 else m - 1
}
