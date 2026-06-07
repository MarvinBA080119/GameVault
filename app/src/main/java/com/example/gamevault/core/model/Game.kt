package com.example.gamevault.core.model

import com.google.gson.annotations.SerializedName

data class GamesListResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("next") val next: String? = null,
    @SerializedName("results") val results: List<Game> = emptyList()
)

data class Game(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("rating") val rating: Double,
    @SerializedName("metacritic") val metacritic: Int?,
    @SerializedName("released") val released: String?,
    @SerializedName("genres") val genres: List<Genre> = emptyList(),
    @SerializedName("platforms") val platforms: List<PlatformWrapper> = emptyList()
) {

    fun genresText(max: Int = 2): String =
        genres.take(max).joinToString(" · ") { it.name }

    fun platformsText(max: Int = 3): String =
        platforms.take(max).joinToString(" · ") { it.platform.shortName() }
}

data class Genre(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = ""
)

data class PlatformWrapper(
    @SerializedName("platform") val platform: Platform
)

data class Platform(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("slug") val slug: String = ""
) {

    fun shortName(): String = when {
        slug.contains("playstation5") -> "PS5"
        slug.contains("playstation4") -> "PS4"
        slug.startsWith("xbox-one") -> "XB1"
        slug.contains("xbox-series") -> "XBX"
        slug.contains("xbox") -> "XBX"
        slug == "pc" -> "PC"
        slug.contains("nintendo-switch") -> "SW"
        slug.contains("ios") -> "iOS"
        slug.contains("android") -> "AND"
        slug.contains("mac") -> "Mac"
        slug.contains("linux") -> "Linux"
        else -> name.take(3).uppercase()
    }
}

data class GameDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description_raw") val descriptionRaw: String?,
    @SerializedName("released") val released: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("rating") val rating: Double,
    @SerializedName("ratings_count") val ratingsCount: Int = 0,
    @SerializedName("metacritic") val metacritic: Int?,
    @SerializedName("playtime") val playtime: Int = 0,
    @SerializedName("developers") val developers: List<Genre> = emptyList(),
    @SerializedName("genres") val genres: List<Genre> = emptyList(),
    @SerializedName("platforms") val platforms: List<PlatformWrapper> = emptyList(),
    @SerializedName("esrb_rating") val esrbRating: EsrbRating?
) {
    fun developerName(): String =
        developers.firstOrNull()?.name ?: "Desconocido"
}

data class EsrbRating(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = ""
)