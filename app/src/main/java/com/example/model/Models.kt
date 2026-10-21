package com.example.model

/**
 * Data models for WatchNext Media Queue application.
 * All labels are streamlined to avoid awkward word breaks in UI components.
 */

data class QueueStatus(
    val episodeCountLimit: Int = 5,
    val capGb: Float? = null,
    val reserveGb: Float = 5f,
    val isPaused: Boolean = false,
    val diskTotalGb: Float = 512f,
    val diskFreeGb: Float = 184.2f,
    val diskUsedGb: Float = 327.8f,
    val activeDownloadsCount: Int = 2,
    val totalPacksCount: Int = 4,
    val serviceAlert: String? = null,
    val limitAlert: String? = null
)

enum class PackKind(val label: String) {
    SERIES("Сериал"),
    MOVIE("Фильм")
}

enum class PackState(val label: String) {
    METADATA("Инфо"),
    READY("Готов"),
    DOWNLOADING("Загрузка"),
    CHECKING("Проверка"),
    STOPPED("Пауза"),
    COMPLETE("Скачан")
}

data class PackItem(
    val hash: String,
    val title: String,
    val kind: PackKind = PackKind.SERIES,
    val season: Int = 1,
    val startEpisode: Int = 1,
    val state: PackState = PackState.DOWNLOADING,
    val progress: Float = 0f,
    val totalSize: Long = 0L,
    val downloadedSize: Long = 0L,
    val downloadSpeed: Long = 0L,
    val readyEpisodes: Int = 0,
    val totalEpisodes: Int = 0,
    val noNextSeason: Boolean = false,
    val episodes: List<EpisodeItem> = emptyList()
) {
    val progressPercent: Int
        get() = (progress * 100).toInt()

    val totalSizeFormatted: String
        get() = formatBytes(totalSize)

    val downloadedSizeFormatted: String
        get() = formatBytes(downloadedSize)

    val speedFormatted: String
        get() = if (downloadSpeed > 0) "${formatBytes(downloadSpeed)}/с" else ""
}

enum class EpisodeStatus(val label: String) {
    COMPLETED("Готово"),
    DOWNLOADING("Загрузка"),
    WAITING_LIMIT("В очереди"),
    PLAYED("Просмотрено"),
    SKIPPED("Пропущено"),
    REMOVED("Удалено")
}

data class EpisodeItem(
    val index: Int,
    val name: String,
    val season: Int,
    val episode: Int,
    val label: String,
    val size: Long,
    val progress: Float,
    val status: EpisodeStatus,
    val isPublished: Boolean = false
) {
    val sizeFormatted: String
        get() = formatBytes(size)
}

data class TorrentSearchResult(
    val title: String,
    val tracker: String,
    val size: Long,
    val seeders: Int,
    val peers: Int?,
    val quality: String,
    val voices: List<String>,
    val magnet: String,
    val link: String = "",
    val detectedKind: PackKind = PackKind.SERIES,
    val detectedSeason: Int = 1,
    val detectedStartEpisode: Int = 1
) {
    val sizeFormatted: String
        get() = formatBytes(size)
}

enum class SubscriptionStatus(val label: String) {
    ACTIVE("Отслеживается"),
    EPISODE_OUT("Ждёт озвучку"),
    RELEASE_FOUND("Релиз найден"),
    UP_TO_DATE("Актуально")
}

data class SubscriptionItem(
    val id: String,
    val showTitle: String,
    val preferredQuality: String = "1080p",
    val preferredVoice: String = "Любая",
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val lastSeason: Int = 1,
    val lastEpisode: Int = 8,
    val nextEpisodeDate: String? = null,
    val nextEpisodeLabel: String? = null,
    val candidateReleaseTitle: String? = null,
    val candidateReleaseMagnet: String? = null
)

data class ShowSearchResult(
    val id: Int,
    val name: String,
    val premiered: String = "",
    val status: String = "",
    val language: String = ""
)

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 Б"
    val units = arrayOf("Б", "КБ", "МБ", "ГБ", "ТБ")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val group = digitGroups.coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, group.toDouble())
    return if (group == 0) "$bytes ${units[group]}" else String.format(java.util.Locale.US, "%.1f %s", value, units[group])
}
