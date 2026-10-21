package com.example.data

import com.example.data.api.*
import com.example.model.*
import com.example.util.TorrentParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WatchNextRepository(
    private val preferences: ServerPreferences,
    private val scope: CoroutineScope
) {
    private val _status = MutableStateFlow(QueueStatus())
    val status: StateFlow<QueueStatus> = _status.asStateFlow()

    private val _packs = MutableStateFlow<List<PackItem>>(emptyList())
    val packs: StateFlow<List<PackItem>> = _packs.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<SubscriptionItem>>(emptyList())
    val subscriptions: StateFlow<List<SubscriptionItem>> = _subscriptions.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastLog = MutableStateFlow<String?>("WatchNext готов к работе")
    val lastLog: StateFlow<String?> = _lastLog.asStateFlow()

    private var currentSettings: AppSettings = AppSettings()
    private var apiService: WatchNextApiService? = null

    init {
        loadDemoData()
        scope.launch {
            preferences.settingsFlow.collect { settings ->
                currentSettings = settings
                updateApiService()
                if (!settings.isDemoMode) {
                    refresh(showIndicator = true)
                } else {
                    _status.value = _status.value.copy(
                        episodeCountLimit = settings.episodeLimit,
                        capGb = settings.capGb,
                        reserveGb = settings.reserveGb
                    )
                }
            }
        }
        // Periodic background polling based on user-configured interval
        scope.launch {
            while (isActive) {
                val interval = currentSettings.pollIntervalSeconds
                val delayMs = (if (interval > 0) interval else 3) * 1000L
                delay(delayMs)
                if (!currentSettings.isDemoMode && interval > 0 && apiService != null) {
                    try {
                        refresh(showIndicator = false)
                    } catch (_: Exception) {
                        // Background polling errors shouldn't disrupt UI
                    }
                }
            }
        }
    }

    private fun updateApiService() {
        try {
            if (currentSettings.serverUrl.isNotBlank() && currentSettings.serverUrl.startsWith("http")) {
                apiService = WatchNextApiService.create(
                    baseUrl = currentSettings.serverUrl,
                    apiToken = currentSettings.apiToken
                )
            }
        } catch (e: Exception) {
            apiService = null
        }
    }

    private fun loadDemoData() {
        val samplePacks = listOf(
            PackItem(
                hash = "4a8e3290bbf127889104fa2891bb3a2012019481",
                title = "Дом Дракона",
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
                title = "Дюна: Часть вторая",
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
                    EpisodeItem(0, "Dune.Part.Two.2024.1080p.mkv", 1, 1, "Фильм", 18_400_000_000L, 0.88f, EpisodeStatus.DOWNLOADING, false)
                )
            ),
            PackItem(
                hash = "3194aa82bbf127889104fa2891bb3a2012019499",
                title = "Пингвин",
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
                    EpisodeItem(0, "The.Penguin.S01E01.mkv", 1, 1, "S01E01", 4_500_000_000L, 1.0f, EpisodeStatus.PLAYED, true),
                    EpisodeItem(1, "The.Penguin.S01E02.mkv", 1, 2, "S01E02", 4_800_000_000L, 1.0f, EpisodeStatus.COMPLETED, true),
                    EpisodeItem(2, "The.Penguin.S01E03.mkv", 1, 3, "S01E03", 5_100_000_000L, 1.0f, EpisodeStatus.COMPLETED, true)
                )
            )
        )
        _packs.value = samplePacks

        _subscriptions.value = listOf(
            SubscriptionItem(
                id = "sub-1",
                showTitle = "Разделение (Severance)",
                preferredQuality = "1080p",
                preferredVoice = "LostFilm",
                status = SubscriptionStatus.RELEASE_FOUND,
                lastSeason = 2,
                lastEpisode = 1,
                nextEpisodeDate = "2026-10-24",
                nextEpisodeLabel = "Сезон 2, Серия 1",
                candidateReleaseTitle = "Разделение / Severance S02E01 1080p LostFilm",
                candidateReleaseMagnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d1117abcd"
            ),
            SubscriptionItem(
                id = "sub-2",
                showTitle = "Одни из нас (The Last of Us)",
                preferredQuality = "2160p",
                preferredVoice = "HDRezka",
                status = SubscriptionStatus.EPISODE_OUT,
                lastSeason = 2,
                lastEpisode = 3,
                nextEpisodeDate = "2026-10-21",
                nextEpisodeLabel = "Сезон 2, Серия 4"
            )
        )

        _status.value = QueueStatus(
            episodeCountLimit = 5,
            capGb = null,
            reserveGb = 5f,
            isPaused = false,
            diskTotalGb = 512f,
            diskFreeGb = 184.2f,
            diskUsedGb = 327.8f,
            activeDownloadsCount = 2,
            totalPacksCount = 3
        )
    }

    suspend fun refresh(showIndicator: Boolean = true) {
        if (showIndicator) _isRefreshing.value = true
        withContext(Dispatchers.IO) {
            val api = apiService
            if (!currentSettings.isDemoMode && api != null) {
                try {
                    val statusRes = api.getStatus()
                    _status.value = QueueStatus(
                        episodeCountLimit = statusRes.episodeCountLimit ?: 5,
                        capGb = statusRes.capGb,
                        reserveGb = statusRes.reserveGb,
                        isPaused = statusRes.isPaused,
                        diskTotalGb = statusRes.diskTotalGb,
                        diskFreeGb = statusRes.diskFreeGb,
                        diskUsedGb = statusRes.diskUsedGb,
                        activeDownloadsCount = statusRes.activeDownloadsCount,
                        totalPacksCount = statusRes.totalPacksCount,
                        serviceAlert = statusRes.serviceAlert,
                        limitAlert = statusRes.limitAlert
                    )

                    val packsRes = api.listPacks()
                    _packs.value = packsRes.map { p ->
                        PackItem(
                            hash = p.hash,
                            title = p.title,
                            kind = if (p.kind.equals("movie", ignoreCase = true)) PackKind.MOVIE else PackKind.SERIES,
                            season = p.season,
                            startEpisode = p.startEpisode,
                            state = mapPackState(p.state),
                            progress = p.progress,
                            totalSize = p.totalSize,
                            downloadedSize = p.downloadedSize,
                            downloadSpeed = p.downloadSpeed,
                            readyEpisodes = p.readyEpisodes,
                            totalEpisodes = p.totalEpisodes,
                            noNextSeason = p.noNextSeason,
                            episodes = p.episodes.map { e ->
                                EpisodeItem(
                                    index = e.index,
                                    name = e.name,
                                    season = e.season,
                                    episode = e.episode,
                                    label = e.label,
                                    size = e.size,
                                    progress = e.progress,
                                    status = mapEpisodeStatus(e.status),
                                    isPublished = e.isPublished
                                )
                            }
                        )
                    }

                    val subsRes = api.listSubscriptions()
                    _subscriptions.value = subsRes.map { s ->
                        SubscriptionItem(
                            id = s.id,
                            showTitle = s.showTitle,
                            preferredQuality = s.preferredQuality,
                            preferredVoice = s.preferredVoice,
                            status = mapSubscriptionStatus(s.status),
                            lastSeason = s.lastSeason,
                            lastEpisode = s.lastEpisode,
                            nextEpisodeDate = s.nextEpisodeDate,
                            nextEpisodeLabel = s.nextEpisodeLabel,
                            candidateReleaseTitle = s.candidateReleaseTitle,
                            candidateReleaseMagnet = s.candidateReleaseMagnet
                        )
                    }

                    if (showIndicator) {
                        _lastLog.value = "Синхронизировано с сервером WatchNext"
                    }
                } catch (e: Exception) {
                    if (showIndicator) {
                        _lastLog.value = "Ошибка связи с сервером: ${e.localizedMessage ?: "Недоступен"}"
                    }
                } finally {
                    if (showIndicator) {
                        _isRefreshing.value = false
                    }
                }
            } else {
                if (showIndicator) {
                    kotlinx.coroutines.delay(300)
                    _isRefreshing.value = false
                    _lastLog.value = "Локальные данные очереди обновлены"
                }
            }
        }
    }

    suspend fun togglePause() {
        val willPause = !_status.value.isPaused
        _status.value = _status.value.copy(isPaused = willPause)
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.togglePause()
                    refresh()
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка паузы: ${e.localizedMessage}"
                }
            }
        } else {
            _lastLog.value = if (willPause) "Очередь на паузе" else "Очередь продолжена"
        }
    }

    suspend fun addMagnet(
        magnet: String,
        customTitle: String,
        kind: PackKind,
        season: Int,
        startEpisode: Int
    ): Result<String> {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            return withContext(Dispatchers.IO) {
                try {
                    api.addPack(
                        ApiAddTorrentRequest(
                            magnet = magnet,
                            title = customTitle.ifBlank { null },
                            kind = if (kind == PackKind.MOVIE) "movie" else "series",
                            season = season,
                            startEpisode = startEpisode,
                            autoSeason = false
                        )
                    )
                    refresh()
                    Result.success("Раздача добавлена на сервер")
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

        // Demo fallback
        val hash = TorrentParser.extractMagnetHash(magnet) ?: UUID.randomUUID().toString().replace("-", "")
        val displayName = customTitle.ifBlank {
            TorrentParser.extractDisplayName(magnet) ?: "Раздача #${hash.take(8)}"
        }
        val totalEpisodes = if (kind == PackKind.MOVIE) 1 else 10
        val episodesList = (1..totalEpisodes).map { epNum ->
            val label = if (kind == PackKind.MOVIE) "Фильм" else "S${String.format("%02d", season)}E${String.format("%02d", epNum)}"
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
        _lastLog.value = "Добавлена раздача: $displayName"
        return Result.success("Раздача добавлена в очередь")
    }

    suspend fun changeFrom(hash: String, season: Int, startEpisode: Int) {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.setPackStart(hash, ApiSetStartRequest(season, startEpisode))
                    refresh()
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка /from: ${e.localizedMessage}"
                }
            }
            return
        }

        _packs.value = _packs.value.map { pack ->
            if (pack.hash == hash) {
                val updatedEpisodes = pack.episodes.map { ep ->
                    val shouldSkip = ep.season < season || (ep.season == season && ep.episode < startEpisode)
                    ep.copy(status = if (shouldSkip) EpisodeStatus.SKIPPED else ep.status)
                }
                pack.copy(season = season, startEpisode = startEpisode, episodes = updatedEpisodes)
            } else pack
        }
        _lastLog.value = "Старт изменён: Сезон $season, Серия $startEpisode"
    }

    suspend fun finishPack(hash: String) {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.finishPack(hash)
                    refresh()
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка /finish: ${e.localizedMessage}"
                }
            }
            return
        }

        _packs.value = _packs.value.map { pack ->
            if (pack.hash == hash) pack.copy(noNextSeason = true) else pack
        }
        _lastLog.value = "Раздача завершена"
    }

    suspend fun retryPack(hash: String) {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.retryPack(hash)
                    refresh()
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка /retry: ${e.localizedMessage}"
                }
            }
            return
        }

        _packs.value = _packs.value.map { pack ->
            if (pack.hash == hash) pack.copy(state = PackState.CHECKING) else pack
        }
        _lastLog.value = "Запущена проверка"
    }

    suspend fun deletePack(hash: String) {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.deletePack(hash)
                    refresh()
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка удаления: ${e.localizedMessage}"
                }
            }
            return
        }

        _packs.value = _packs.value.filterNot { it.hash == hash }
        _lastLog.value = "Раздача удалена"
    }

    suspend fun deleteEpisode(hash: String, episodeIndex: Int): Result<String> {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            return withContext(Dispatchers.IO) {
                try {
                    api.deleteEpisode(hash, episodeIndex)
                    refresh()
                    Result.success("Серия удалена")
                } catch (e: Exception) {
                    val msg = e.localizedMessage ?: "Ошибка удаления серии"
                    _lastLog.value = "Ошибка удаления серии: $msg"
                    Result.failure(e)
                }
            }
        }

        _packs.value = _packs.value.map { pack ->
            if (pack.hash == hash) {
                val updated = pack.episodes.map { ep ->
                    if (ep.index == episodeIndex) {
                        ep.copy(status = EpisodeStatus.REMOVED, isPublished = false)
                    } else ep
                }
                pack.copy(episodes = updated)
            } else pack
        }
        _lastLog.value = "Серия удалена"
        return Result.success("Серия удалена")
    }

    suspend fun updateLimits(count: Int, capGb: Float?, reserveGb: Float) {
        _status.value = _status.value.copy(
            episodeCountLimit = count,
            capGb = capGb,
            reserveGb = reserveGb
        )
        preferences.updateLimitsOnly(
            episodeLimit = count,
            capGb = capGb,
            reserveGb = reserveGb
        )

        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.updateLimits(
                        ApiUpdateLimitsRequest(
                            episodeCountLimit = count,
                            capGb = capGb,
                            reserveGb = reserveGb
                        )
                    )
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка обновления лимитов: ${e.localizedMessage}"
                }
            }
        }
    }

    suspend fun searchTorrents(
        query: String,
        kind: PackKind,
        minSeeders: Int = 0,
        voiceFilter: String? = null,
        qualityFilter: String? = null
    ): List<TorrentSearchResult> {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            return withContext(Dispatchers.IO) {
                try {
                    val apiResults = api.searchTorrents(
                        query = query,
                        kind = if (kind == PackKind.MOVIE) "movie" else "series",
                        minSeeders = minSeeders,
                        voiceFilter = voiceFilter,
                        qualityFilter = qualityFilter
                    )
                    apiResults.map { r ->
                        TorrentSearchResult(
                            title = r.title,
                            tracker = r.tracker,
                            size = r.size,
                            seeders = r.seeders,
                            peers = r.peers,
                            quality = r.quality,
                            voices = r.voices,
                            magnet = r.magnet,
                            link = r.link,
                            detectedKind = if (r.detectedKind.equals("movie", ignoreCase = true)) PackKind.MOVIE else PackKind.SERIES,
                            detectedSeason = r.detectedSeason,
                            detectedStartEpisode = r.detectedStartEpisode
                        )
                    }
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка поиска через сервер: ${e.localizedMessage}"
                    emptyList()
                }
            }
        }

        // Demo results fallback
        val cleanQuery = query.trim()
        val mockResults = listOf(
            TorrentSearchResult(
                title = "$cleanQuery / Release (2024) WEB-DL 2160p 4K | LostFilm",
                tracker = "RuTor",
                size = 38_500_000_000L,
                seeders = 142,
                peers = 18,
                quality = "2160p · 4K",
                voices = listOf("LostFilm"),
                magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d11170001&dn=$cleanQuery.2160p.LostFilm",
                detectedKind = kind,
                detectedSeason = 1,
                detectedStartEpisode = 1
            ),
            TorrentSearchResult(
                title = "$cleanQuery (2024) WEB-DL 1080p | HDRezka Studio",
                tracker = "RuTor",
                size = 14_200_000_000L,
                seeders = 215,
                peers = 34,
                quality = "1080p · WEB-DL",
                voices = listOf("HDRezka"),
                magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d11170002&dn=$cleanQuery.1080p.Rezka",
                detectedKind = kind,
                detectedSeason = 1,
                detectedStartEpisode = 1
            ),
            TorrentSearchResult(
                title = "$cleanQuery (Сезон 1, Серии 1-8) (2024) BDRip 1080p | Кубик в кубе",
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
            )
        )

        return mockResults.filter { res ->
            res.seeders >= minSeeders &&
                    (voiceFilter == null || res.voices.any { it.contains(voiceFilter, ignoreCase = true) }) &&
                    (qualityFilter == null || res.quality.contains(qualityFilter, ignoreCase = true))
        }
    }

    suspend fun searchShows(query: String): List<ShowSearchResult> {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            return withContext(Dispatchers.IO) {
                try {
                    val apiResults = api.searchShows(query)
                    apiResults.map { r ->
                        ShowSearchResult(
                            id = r.id,
                            name = r.name,
                            premiered = r.premiered ?: "",
                            status = r.status ?: "",
                            language = r.language ?: ""
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        return emptyList()
    }

    suspend fun addSubscription(title: String, quality: String, voice: String, tvmazeId: Int? = null) {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.addSubscription(ApiAddSubscriptionRequest(title, quality, voice, tvmazeId))
                    refresh()
                    _lastLog.value = "Подписка оформлена: $title"
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка создания подписки: ${e.localizedMessage}"
                }
            }
            return
        }

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
        _lastLog.value = "Оформлена подписка: $title"
    }

    suspend fun removeSubscription(id: String) {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.removeSubscription(id)
                    refresh()
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка удаления подписки: ${e.localizedMessage}"
                }
            }
            return
        }

        _subscriptions.value = _subscriptions.value.filterNot { it.id == id }
    }

    suspend fun importSubscriptionRelease(subId: String) {
        val api = apiService
        if (!currentSettings.isDemoMode && api != null) {
            withContext(Dispatchers.IO) {
                try {
                    api.importSubscriptionRelease(subId)
                    refresh()
                    _lastLog.value = "Релиз импортирован в очередь"
                } catch (e: Exception) {
                    _lastLog.value = "Ошибка импорта релиза: ${e.localizedMessage}"
                }
            }
            return
        }

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
                val testApi = WatchNextApiService.create(settings.serverUrl, settings.apiToken)
                val status = testApi.getStatus()
                Pair(
                    true,
                    "Успешно! Сервер WatchNext онлайн: ${status.totalPacksCount} раздач, ${status.diskFreeGb.toInt()} ГБ свободно."
                )
            } catch (e: Exception) {
                Pair(false, "Не удалось подключиться: ${e.localizedMessage ?: "Таймаут или неверный URL"}")
            }
        }
    }

    private fun mapPackState(state: String): PackState {
        return when (state.uppercase()) {
            "DOWNLOADING" -> PackState.DOWNLOADING
            "READY" -> PackState.READY
            "CHECKING" -> PackState.CHECKING
            "STOPPED" -> PackState.STOPPED
            "METADATA" -> PackState.METADATA
            "COMPLETE" -> PackState.COMPLETE
            else -> PackState.DOWNLOADING
        }
    }

    private fun mapEpisodeStatus(status: String): EpisodeStatus {
        return when (status.uppercase()) {
            "COMPLETED" -> EpisodeStatus.COMPLETED
            "DOWNLOADING" -> EpisodeStatus.DOWNLOADING
            "WAITING_LIMIT" -> EpisodeStatus.WAITING_LIMIT
            "PLAYED" -> EpisodeStatus.PLAYED
            "SKIPPED" -> EpisodeStatus.SKIPPED
            "REMOVED" -> EpisodeStatus.REMOVED
            else -> EpisodeStatus.WAITING_LIMIT
        }
    }

    private fun mapSubscriptionStatus(status: String): SubscriptionStatus {
        return when (status.uppercase()) {
            "ACTIVE" -> SubscriptionStatus.ACTIVE
            "EPISODE_OUT" -> SubscriptionStatus.EPISODE_OUT
            "RELEASE_FOUND" -> SubscriptionStatus.RELEASE_FOUND
            "UP_TO_DATE" -> SubscriptionStatus.UP_TO_DATE
            else -> SubscriptionStatus.ACTIVE
        }
    }
}
