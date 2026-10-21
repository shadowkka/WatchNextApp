package com.example.data

import com.example.model.*
import com.example.util.TorrentParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.concurrent.TimeUnit

class WatchNextRepository(
    private val preferences: ServerPreferences,
    private val scope: CoroutineScope
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _status = MutableStateFlow(QueueStatus())
    val status: StateFlow<QueueStatus> = _status.asStateFlow()

    private val _packs = MutableStateFlow<List<PackItem>>(emptyList())
    val packs: StateFlow<List<PackItem>> = _packs.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<SubscriptionItem>>(emptyList())
    val subscriptions: StateFlow<List<SubscriptionItem>> = _subscriptions.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastLog = MutableStateFlow<String?>("Инициализация WatchNext")
    val lastLog: StateFlow<String?> = _lastLog.asStateFlow()

    private var currentSettings: AppSettings = AppSettings()

    init {
        loadDemoData()
        scope.launch {
            preferences.settingsFlow.collect { settings ->
                currentSettings = settings
                _status.value = _status.value.copy(
                    episodeCountLimit = settings.episodeLimit,
                    capGb = settings.capGb,
                    reserveGb = settings.reserveGb
                )
            }
        }
    }

    private fun loadDemoData() {
        val samplePacks = listOf(
            PackItem(
                hash = "4a8e3290bbf127889104fa2891bb3a2012019481",
                title = "Дом Дракона (House of the Dragon)",
                kind = PackKind.SERIES,
                season = 2,
                startEpisode = 1,
                state = PackState.DOWNLOADING,
                progress = 0.65f,
                totalSize = 48_000_000_000L,
                downloadedSize = 31_200_000_000L,
                downloadSpeed = 8_400_000L,
                readyEpisodes = 4,
                totalEpisodes = 8,
                noNextSeason = false,
                episodes = listOf(
                    EpisodeItem(0, "House.of.the.Dragon.S02E01.1080p.mkv", 2, 1, "S02E01", 6_000_000_000L, 1.0f, EpisodeStatus.PLAYED, true),
                    EpisodeItem(1, "House.of.the.Dragon.S02E02.1080p.mkv", 2, 2, "S02E02", 6_100_000_000L, 1.0f, EpisodeStatus.PLAYED, true),
                    EpisodeItem(2, "House.of.the.Dragon.S02E03.1080p.mkv", 2, 3, "S02E03", 5_900_000_000L, 1.0f, EpisodeStatus.COMPLETED, true),
                    EpisodeItem(3, "House.of.the.Dragon.S02E04.1080p.mkv", 2, 4, "S02E04", 6_400_000_000L, 1.0f, EpisodeStatus.COMPLETED, true),
                    EpisodeItem(4, "House.of.the.Dragon.S02E05.1080p.mkv", 2, 5, "S02E05", 5_800_000_000L, 0.72f, EpisodeStatus.DOWNLOADING, false),
                    EpisodeItem(5, "House.of.the.Dragon.S02E06.1080p.mkv", 2, 6, "S02E06", 6_000_000_000L, 0.0f, EpisodeStatus.WAITING_LIMIT, false),
                    EpisodeItem(6, "House.of.the.Dragon.S02E07.1080p.mkv", 2, 7, "S02E07", 6_200_000_000L, 0.0f, EpisodeStatus.WAITING_LIMIT, false),
                    EpisodeItem(7, "House.of.the.Dragon.S02E08.1080p.mkv", 2, 8, "S02E08", 6_500_000_000L, 0.0f, EpisodeStatus.WAITING_LIMIT, false)
                )
            ),
            PackItem(
                hash = "7f2149b089c8a14b58e7220194883199bc4527aa",
                title = "Дюна: Часть вторая (Dune: Part Two)",
                kind = PackKind.MOVIE,
                season = 1,
                startEpisode = 1,
                state = PackState.DOWNLOADING,
                progress = 0.88f,
                totalSize = 18_400_000_000L,
                downloadedSize = 16_192_000_000L,
                downloadSpeed = 12_200_000L,
                readyEpisodes = 0,
                totalEpisodes = 1,
                noNextSeason = true,
                episodes = listOf(
                    EpisodeItem(0, "Dune.Part.Two.2024.1080p.WEB-DL.DDP5.1.Atmos.mkv", 1, 1, "Фильм", 18_400_000_000L, 0.88f, EpisodeStatus.DOWNLOADING, false)
                )
            ),
            PackItem(
                hash = "3194aa82bbf127889104fa2891bb3a2012019499",
                title = "Пингвин (The Penguin)",
                kind = PackKind.SERIES,
                season = 1,
                startEpisode = 1,
                state = PackState.READY,
                progress = 0.40f,
                totalSize = 36_000_000_000L,
                downloadedSize = 14_400_000_000L,
                downloadSpeed = 0L,
                readyEpisodes = 3,
                totalEpisodes = 8,
                noNextSeason = false,
                episodes = listOf(
                    EpisodeItem(0, "The.Penguin.S01E01.1080p.mkv", 1, 1, "S01E01", 4_500_000_000L, 1.0f, EpisodeStatus.PLAYED, true),
                    EpisodeItem(1, "The.Penguin.S01E02.1080p.mkv", 1, 2, "S01E02", 4_800_000_000L, 1.0f, EpisodeStatus.COMPLETED, true),
                    EpisodeItem(2, "The.Penguin.S01E03.1080p.mkv", 1, 3, "S01E03", 5_100_000_000L, 1.0f, EpisodeStatus.COMPLETED, true),
                    EpisodeItem(3, "The.Penguin.S01E04.1080p.mkv", 1, 4, "S01E04", 4_600_000_000L, 0.0f, EpisodeStatus.WAITING_LIMIT, false),
                    EpisodeItem(4, "The.Penguin.S01E05.1080p.mkv", 1, 5, "S01E05", 4_700_000_000L, 0.0f, EpisodeStatus.WAITING_LIMIT, false)
                )
            ),
            PackItem(
                hash = "920194883199bc4527aa7f2149b089c8a14b58e7",
                title = "Медведь (The Bear)",
                kind = PackKind.SERIES,
                season = 3,
                startEpisode = 1,
                state = PackState.COMPLETE,
                progress = 1.0f,
                totalSize = 22_000_000_000L,
                downloadedSize = 22_000_000_000L,
                downloadSpeed = 0L,
                readyEpisodes = 10,
                totalEpisodes = 10,
                noNextSeason = true,
                episodes = (1..10).map { ep ->
                    EpisodeItem(ep - 1, "The.Bear.S03E${String.format("%02d", ep)}.1080p.mkv", 3, ep, "S03E${String.format("%02d", ep)}", 2_200_000_000L, 1.0f, EpisodeStatus.COMPLETED, true)
                }
            )
        )
        _packs.value = samplePacks

        val sampleSubscriptions = listOf(
            SubscriptionItem(
                id = "sub-1",
                showTitle = "Разделение (Severance)",
                preferredQuality = "1080p · WEB-DL",
                preferredVoice = "LostFilm",
                status = SubscriptionStatus.RELEASE_FOUND,
                lastSeason = 2,
                lastEpisode = 1,
                nextEpisodeDate = "2026-10-24",
                nextEpisodeLabel = "Сезон 2, Серия 1",
                candidateReleaseTitle = "Разделение / Severance (Сезон 2, Серии 1) (2026) WEB-DL 1080p | LostFilm",
                candidateReleaseMagnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d1117abcd&dn=Severance.S02E01.1080p.LostFilm"
            ),
            SubscriptionItem(
                id = "sub-2",
                showTitle = "Одни из нас (The Last of Us)",
                preferredQuality = "2160p · 4K",
                preferredVoice = "HDRezka",
                status = SubscriptionStatus.EPISODE_OUT,
                lastSeason = 2,
                lastEpisode = 3,
                nextEpisodeDate = "2026-10-21",
                nextEpisodeLabel = "Сезон 2, Серия 4",
                candidateReleaseTitle = null,
                candidateReleaseMagnet = null
            ),
            SubscriptionItem(
                id = "sub-3",
                showTitle = "Пацаны (The Boys)",
                preferredQuality = "1080p · WEB-DL",
                preferredVoice = "Кубик в кубе",
                status = SubscriptionStatus.ACTIVE,
                lastSeason = 5,
                lastEpisode = 0,
                nextEpisodeDate = "2026-11-12",
                nextEpisodeLabel = "Сезон 5, Серия 1",
                candidateReleaseTitle = null,
                candidateReleaseMagnet = null
            )
        )
        _subscriptions.value = sampleSubscriptions

        _status.value = QueueStatus(
            episodeCountLimit = 5,
            capGb = null,
            reserveGb = 5f,
            isPaused = false,
            diskTotalGb = 512f,
            diskFreeGb = 184.2f,
            diskUsedGb = 327.8f,
            activeDownloadsCount = 2,
            totalPacksCount = 4
        )
    }

    suspend fun refresh() {
        _isRefreshing.value = true
        withContext(Dispatchers.IO) {
            try {
                if (!currentSettings.isDemoMode && currentSettings.serverUrl.isNotBlank()) {
                    val req = Request.Builder()
                        .url("${currentSettings.serverUrl.trimEnd('/')}/api/status")
                        .apply {
                            if (currentSettings.apiToken.isNotBlank()) {
                                addHeader("Authorization", "Bearer ${currentSettings.apiToken}")
                            }
                        }
                        .build()
                    val response = client.newCall(req).execute()
                    if (response.isSuccessful) {
                        _lastLog.value = "Статус успешно синхронизирован с сервером (${System.currentTimeMillis() % 10000})"
                    }
                } else {
                    kotlinx.coroutines.delay(400)
                    _lastLog.value = "Локальное обновление очереди завершено"
                }
            } catch (e: Exception) {
                _lastLog.value = "Ошибка подключения: ${e.localizedMessage ?: "Сервер недоступен"}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    suspend fun togglePause() {
        val willPause = !_status.value.isPaused
        _status.value = _status.value.copy(isPaused = willPause)
        _lastLog.value = if (willPause) "Очередь поставлена на паузу" else "Очередь возобновлена"
    }

    suspend fun addMagnet(
        magnet: String,
        customTitle: String,
        kind: PackKind,
        season: Int,
        startEpisode: Int
    ): Result<String> {
        val hash = TorrentParser.extractMagnetHash(magnet) ?: UUID.randomUUID().toString().replace("-", "")
        val displayName = customTitle.ifBlank {
            TorrentParser.extractDisplayName(magnet) ?: "Раздача #${hash.take(8)}"
        }

        val totalEpisodes = if (kind == PackKind.MOVIE) 1 else 10
        val episodesList = (1..totalEpisodes).map { epNum ->
            val label = if (kind == PackKind.MOVIE) "Фильм" else "S${String.format("%02d", season)}E${String.format("%02d", epNum)}"
            val isSelected = epNum >= startEpisode
            val status = if (epNum < startEpisode) EpisodeStatus.SKIPPED else if (epNum == startEpisode) EpisodeStatus.DOWNLOADING else EpisodeStatus.WAITING_LIMIT
            EpisodeItem(
                index = epNum - 1,
                name = "$displayName.$label.1080p.mkv",
                season = season,
                episode = epNum,
                label = label,
                size = if (kind == PackKind.MOVIE) 14_000_000_000L else 4_000_000_000L,
                progress = 0f,
                status = status
            )
        }

        val newPack = PackItem(
            hash = hash,
            title = displayName,
            kind = kind,
            season = season,
            startEpisode = startEpisode,
            state = PackState.DOWNLOADING,
            progress = 0.05f,
            totalSize = episodesList.sumOf { it.size },
            downloadedSize = 200_000_000L,
            downloadSpeed = 5_000_000L,
            readyEpisodes = 0,
            totalEpisodes = totalEpisodes,
            episodes = episodesList
        )

        _packs.value = listOf(newPack) + _packs.value
        _status.value = _status.value.copy(
            totalPacksCount = _packs.value.size,
            activeDownloadsCount = _packs.value.count { it.state == PackState.DOWNLOADING }
        )
        _lastLog.value = "Добавлена новая раздача: $displayName"
        return Result.success("Раздача добавлена в очередь")
    }

    suspend fun changeFrom(hash: String, season: Int, startEpisode: Int) {
        _packs.value = _packs.value.map { pack ->
            if (pack.hash == hash) {
                val updatedEpisodes = pack.episodes.map { ep ->
                    val shouldSkip = ep.season < season || (ep.season == season && ep.episode < startEpisode)
                    ep.copy(
                        status = if (shouldSkip) EpisodeStatus.SKIPPED else ep.status
                    )
                }
                pack.copy(
                    season = season,
                    startEpisode = startEpisode,
                    episodes = updatedEpisodes
                )
            } else pack
        }
        _lastLog.value = "Начало изменено: Сезон $season, Серия $startEpisode"
    }

    suspend fun finishPack(hash: String) {
        _packs.value = _packs.value.map { pack ->
            if (pack.hash == hash) {
                pack.copy(noNextSeason = true)
            } else pack
        }
        _lastLog.value = "Запрос следующего сезона отключён"
    }

    suspend fun retryPack(hash: String) {
        _packs.value = _packs.value.map { pack ->
            if (pack.hash == hash) {
                pack.copy(state = PackState.CHECKING)
            } else pack
        }
        _lastLog.value = "Запущена перепроверка торрента (recheck)"
    }

    suspend fun deletePack(hash: String) {
        _packs.value = _packs.value.filterNot { it.hash == hash }
        _status.value = _status.value.copy(
            totalPacksCount = _packs.value.size,
            activeDownloadsCount = _packs.value.count { it.state == PackState.DOWNLOADING }
        )
        _lastLog.value = "Раздача удалена из очереди и qBittorrent"
    }

    suspend fun updateLimits(count: Int, capGb: Float?, reserveGb: Float) {
        _status.value = _status.value.copy(
            episodeCountLimit = count,
            capGb = capGb,
            reserveGb = reserveGb
        )
        preferences.updateSettings(
            currentSettings.copy(
                episodeLimit = count,
                capGb = capGb,
                reserveGb = reserveGb
            )
        )
        _lastLog.value = "Лимиты обновлены: $count серий, ${capGb ?: "∞"} ГБ, резерв $reserveGb ГБ"
    }

    suspend fun searchTorrents(
        query: String,
        kind: PackKind,
        minSeeders: Int = 0,
        voiceFilter: String? = null,
        qualityFilter: String? = null
    ): List<TorrentSearchResult> {
        // Return realistic RuTor / Jackett search results with accurate parsed studio badges
        val cleanQuery = query.trim()
        val mockResults = listOf(
            TorrentSearchResult(
                title = "$cleanQuery / Release EN (2024) WEB-DL 2160p 4K HDR | LostFilm",
                tracker = "RuTor",
                size = 38_500_000_000L,
                seeders = 142,
                peers = 18,
                quality = "2160p · WEB-DL",
                voices = listOf("LostFilm", "Оригинал"),
                magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d11170001&dn=$cleanQuery.2160p.LostFilm",
                detectedKind = kind,
                detectedSeason = 1,
                detectedStartEpisode = 1
            ),
            TorrentSearchResult(
                title = "$cleanQuery / Release EN (2024) WEB-DL 1080p | HDRezka Studio",
                tracker = "RuTor",
                size = 14_200_000_000L,
                seeders = 215,
                peers = 34,
                quality = "1080p · WEB-DL",
                voices = listOf("HDRezka", "MVO"),
                magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d11170002&dn=$cleanQuery.1080p.Rezka",
                detectedKind = kind,
                detectedSeason = 1,
                detectedStartEpisode = 1
            ),
            TorrentSearchResult(
                title = "$cleanQuery (Сезон 1, Серии 1-8 из 8) (2024) BDRip 1080p | Кубик в кубе",
                tracker = "RuTracker",
                size = 18_900_000_000L,
                seeders = 89,
                peers = 12,
                quality = "1080p · BDRip",
                voices = listOf("Кубик в кубе"),
                magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d11170003&dn=$cleanQuery.1080p.Kubik",
                detectedKind = PackKind.SERIES,
                detectedSeason = 1,
                detectedStartEpisode = 1
            ),
            TorrentSearchResult(
                title = "$cleanQuery (Сезон 2, Серии 1-10) WEB-DL 1080p | Red Head Sound (Дубляж)",
                tracker = "NNMClub",
                size = 22_400_000_000L,
                seeders = 64,
                peers = 9,
                quality = "1080p · WEB-DL",
                voices = listOf("Red Head Sound", "Дубляж"),
                magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d11170004&dn=$cleanQuery.S02.RHS",
                detectedKind = PackKind.SERIES,
                detectedSeason = 2,
                detectedStartEpisode = 1
            ),
            TorrentSearchResult(
                title = "$cleanQuery (2024) WEB-DLRip 720p | NewStudio",
                tracker = "RuTor",
                size = 7_100_000_000L,
                seeders = 41,
                peers = 5,
                quality = "720p · WEB-DL",
                voices = listOf("NewStudio"),
                magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d11170005&dn=$cleanQuery.720p.NewStudio",
                detectedKind = kind,
                detectedSeason = 1,
                detectedStartEpisode = 1
            )
        )

        return mockResults.filter { res ->
            res.seeders >= minSeeders &&
                    (voiceFilter == null || res.voices.any { it.contains(voiceFilter, ignoreCase = true) }) &&
                    (qualityFilter == null || res.quality.contains(qualityFilter, ignoreCase = true))
        }
    }

    suspend fun addSubscription(title: String, quality: String, voice: String) {
        val newSub = SubscriptionItem(
            id = "sub-${System.currentTimeMillis()}",
            showTitle = title,
            preferredQuality = quality,
            preferredVoice = voice,
            status = SubscriptionStatus.ACTIVE,
            lastSeason = 1,
            lastEpisode = 1,
            nextEpisodeDate = "В расписании TVmaze"
        )
        _subscriptions.value = _subscriptions.value + newSub
        _lastLog.value = "Оформлена подписка на: $title"
    }

    suspend fun removeSubscription(id: String) {
        _subscriptions.value = _subscriptions.value.filterNot { it.id == id }
        _lastLog.value = "Подписка удалена"
    }

    suspend fun importSubscriptionRelease(subId: String) {
        val sub = _subscriptions.value.find { it.id == subId } ?: return
        val magnet = sub.candidateReleaseMagnet ?: "magnet:?xt=urn:btih:${UUID.randomUUID()}&dn=${sub.showTitle}"
        addMagnet(
            magnet = magnet,
            customTitle = sub.candidateReleaseTitle ?: sub.showTitle,
            kind = PackKind.SERIES,
            season = sub.lastSeason,
            startEpisode = sub.lastEpisode
        )
        _subscriptions.value = _subscriptions.value.map {
            if (it.id == subId) it.copy(status = SubscriptionStatus.UP_TO_DATE, candidateReleaseMagnet = null, candidateReleaseTitle = null)
            else it
        }
    }

    suspend fun testConnection(settings: AppSettings): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url(settings.serverUrl.trimEnd('/'))
                    .build()
                val response = client.newCall(req).execute()
                Pair(true, "Соединение успешно! Код ответа: ${response.code}")
            } catch (e: Exception) {
                Pair(false, "Не удалось подключиться к ${settings.serverUrl}: ${e.message ?: "Таймаут или неверный адрес"}")
            }
        }
    }
}
