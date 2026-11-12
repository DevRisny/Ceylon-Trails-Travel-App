package com.risny.trails.core

import com.google.gson.annotations.SerializedName

/** Wikipedia REST page summary (https://en.wikipedia.org/api/rest_v1/page/summary/{title}). */
data class WikiSummaryDto(
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val extract: String? = null,
    val thumbnail: Image? = null,
    @SerializedName("originalimage") val originalImage: Image? = null,
    @SerializedName("content_urls") val contentUrls: ContentUrls? = null,
) {
    data class Image(val source: String? = null, val width: Int? = null, val height: Int? = null)
    data class ContentUrls(val desktop: Page? = null, val mobile: Page? = null)
    data class Page(val page: String? = null)
}

data class PlaceInfo(val title: String, val description: String?, val extract: String, val thumbnailUrl: String?, val imageUrl: String?, val pageUrl: String?)

object Wiki {
    /** Wikipedia titles in URLs use underscores for spaces. */
    fun pathTitle(title: String): String = title.trim().replace(' ', '_')

    /**
     * Ask Wikimedia's thumbnail service for a sensible width instead of the multi-megabyte original:
     * ".../thumb/a/ab/File.jpg/320px-File.jpg" → same path with 960px.
     */
    fun resize(url: String?, width: Int): String? {
        if (url == null) return null
        val m = Regex("/(\\d+)px-([^/]+)$").find(url) ?: return url
        return url.replaceRange(m.range, "/${width}px-${m.groupValues[2]}")
    }

    // Wikimedia serves standard thumbnail widths fastest (and never upscales past the original)
    const val CARD_WIDTH = 500
    const val HERO_WIDTH = 1280

    fun sized(dto: WikiSummaryDto, width: Int): String? {
        val original = dto.originalImage
        if (original?.source != null && (original.width ?: Int.MAX_VALUE) <= width) return original.source
        return resize(dto.thumbnail?.source, width) ?: original?.source
    }

    fun toPlace(dto: WikiSummaryDto, fallbackTitle: String): PlaceInfo = PlaceInfo(
        title = dto.title ?: fallbackTitle,
        description = dto.description?.replaceFirstChar { it.uppercase() },
        extract = dto.extract?.trim().takeUnless { it.isNullOrEmpty() } ?: "No description available.",
        thumbnailUrl = sized(dto, CARD_WIDTH),
        imageUrl = sized(dto, HERO_WIDTH),
        pageUrl = dto.contentUrls?.mobile?.page ?: dto.contentUrls?.desktop?.page,
    )
}
