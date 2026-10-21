package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiQueueStatus(
    @Json(name = "episodeCountLimit") val episodeCountLimit: Int? = 5,
    @Json(name = "capGb") val capGb: Float? = null,
    @Json(name = "reserveGb") val reserveGb: Float = 5.0f,
    @Json(name = "isPaused") val isPaused: Boolean = false,
    @Json(name = "diskTotalGb") val diskTotalGb: Float = 0.0f,
    @Json(name = "diskFreeGb") val diskFreeGb: Float = 0.0f,
    @Json(name = "diskUsedGb") val diskUsedGb: Float = 0.0f,
    @Json(name = "activeDownloadsCount") val activeDownloadsCount: Int = 0,
    @Json(name = "totalPacksCount") val totalPacksCount: Int = 0,
    @Json(name = "serviceAlert") val serviceAlert: String? = null,
    @Json(name = "limitAlert") val limitAlert: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiTogglePauseResponse(
    @Json(name = "isPaused") val isPaused: Boolean
)

@JsonClass(generateAdapter = true)
data class ApiUpdateLimitsRequest(
    @Json(name = "episodeCountLimit") val episodeCountLimit: Int?,
    @Json(name = "capGb") val capGb: Float?,
    @Json(name = "reserveGb") val reserveGb: Float?
)

@JsonClass(generateAdapter = true)
data class ApiEpisodeItem(
    @Json(name = "index") val index: Int,
    @Json(name = "name") val name: String,
    @Json(name = "season") val season: Int,
    @Json(name = "episode") val episode: Int,
    @Json(name = "label") val label: String,
    @Json(name = "size") val size: Long,
    @Json(name = "progress") val progress: Float,
    @Json(name = "status") val status: String,
    @Json(name = "isPublished") val isPublished: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ApiPackItem(
    @Json(name = "hash") val hash: String,
    @Json(name = "title") val title: String,
    @Json(name = "kind") val kind: String,
    @Json(name = "season") val season: Int = 1,
    @Json(name = "startEpisode") val startEpisode: Int = 1,
    @Json(name = "state") val state: String,
    @Json(name = "progress") val progress: Float = 0f,
    @Json(name = "totalSize") val totalSize: Long = 0L,
    @Json(name = "downloadedSize") val downloadedSize: Long = 0L,
    @Json(name = "downloadSpeed") val downloadSpeed: Long = 0L,
    @Json(name = "readyEpisodes") val readyEpisodes: Int = 0,
    @Json(name = "totalEpisodes") val totalEpisodes: Int = 0,
    @Json(name = "noNextSeason") val noNextSeason: Boolean = false,
    @Json(name = "episodes") val episodes: List<ApiEpisodeItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiAddTorrentRequest(
    @Json(name = "magnet") val magnet: String,
    @Json(name = "title") val title: String? = "",
    @Json(name = "kind") val kind: String = "series",
    @Json(name = "season") val season: Int = 1,
    @Json(name = "startEpisode") val startEpisode: Int = 1,
    @Json(name = "autoSeason") val autoSeason: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ApiSetStartRequest(
    @Json(name = "season") val season: Int,
    @Json(name = "startEpisode") val startEpisode: Int
)

@JsonClass(generateAdapter = true)
data class ApiTorrentSearchResult(
    @Json(name = "title") val title: String,
    @Json(name = "tracker") val tracker: String,
    @Json(name = "size") val size: Long,
    @Json(name = "seeders") val seeders: Int,
    @Json(name = "peers") val peers: Int? = null,
    @Json(name = "quality") val quality: String,
    @Json(name = "voices") val voices: List<String> = emptyList(),
    @Json(name = "magnet") val magnet: String,
    @Json(name = "link") val link: String = "",
    @Json(name = "detectedKind") val detectedKind: String = "SERIES",
    @Json(name = "detectedSeason") val detectedSeason: Int = 1,
    @Json(name = "detectedStartEpisode") val detectedStartEpisode: Int = 1
)

@JsonClass(generateAdapter = true)
data class ApiSubscriptionItem(
    @Json(name = "id") val id: String,
    @Json(name = "showTitle") val showTitle: String,
    @Json(name = "preferredQuality") val preferredQuality: String = "1080p",
    @Json(name = "preferredVoice") val preferredVoice: String = "Любая",
    @Json(name = "status") val status: String = "ACTIVE",
    @Json(name = "lastSeason") val lastSeason: Int = 1,
    @Json(name = "lastEpisode") val lastEpisode: Int = 1,
    @Json(name = "nextEpisodeDate") val nextEpisodeDate: String? = null,
    @Json(name = "nextEpisodeLabel") val nextEpisodeLabel: String? = null,
    @Json(name = "candidateReleaseTitle") val candidateReleaseTitle: String? = null,
    @Json(name = "candidateReleaseMagnet") val candidateReleaseMagnet: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiAddSubscriptionRequest(
    @Json(name = "title") val title: String,
    @Json(name = "preferredQuality") val preferredQuality: String = "1080p",
    @Json(name = "preferredVoice") val preferredVoice: String = "Любая"
)
