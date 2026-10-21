package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppSettings
import com.example.data.WatchNextRepository
import com.example.model.PackItem
import com.example.ui.screens.*
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Очередь", Icons.Default.ViewAgenda),
    SEARCH("Поиск", Icons.Default.Search),
    ADD("Магнет", Icons.Default.Link),
    SUBSCRIPTIONS("Подписки", Icons.Default.Subscriptions),
    SETTINGS("Настройки", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchNextApp(
    repository: WatchNextRepository,
    currentSettings: AppSettings,
    initialMagnet: String? = null,
    onSaveSettings: (AppSettings) -> Unit
) {
    var currentTab by remember { mutableStateOf(if (!initialMagnet.isNullOrBlank()) ScreenTab.ADD else ScreenTab.DASHBOARD) }
    var magnetInput by remember { mutableStateOf(initialMagnet ?: "") }
    var selectedPackForDetail by remember { mutableStateOf<PackItem?>(null) }

    val status by repository.status.collectAsStateWithLifecycle()
    val packs by repository.packs.collectAsStateWithLifecycle()
    val subscriptions by repository.subscriptions.collectAsStateWithLifecycle()
    val isRefreshing by repository.isRefreshing.collectAsStateWithLifecycle()
    val lastLog by repository.lastLog.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = currentTab != ScreenTab.DASHBOARD) {
        currentTab = ScreenTab.DASHBOARD
    }

    LaunchedEffect(lastLog) {
        lastLog?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                ScreenTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp, maxLines = 1) },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.DASHBOARD -> {
                    DashboardScreen(
                        status = status,
                        packs = packs,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            coroutineScope.launch { repository.refresh() }
                        },
                        onTogglePause = {
                            coroutineScope.launch { repository.togglePause() }
                        },
                        onSelectPack = { pack ->
                            selectedPackForDetail = pack
                        },
                        onNavigateToAdd = { currentTab = ScreenTab.ADD },
                        onNavigateToSearch = { currentTab = ScreenTab.SEARCH }
                    )
                }
                ScreenTab.SEARCH -> {
                    SearchScreen(
                        onSearch = { query, kind, minSeeders, voice, quality ->
                            repository.searchTorrents(query, kind, minSeeders, voice, quality)
                        },
                        onAddToQueue = { result, season, episode, kind ->
                            coroutineScope.launch {
                                repository.addMagnet(
                                    magnet = result.magnet,
                                    customTitle = result.title,
                                    kind = kind,
                                    season = season,
                                    startEpisode = episode
                                )
                                currentTab = ScreenTab.DASHBOARD
                            }
                        }
                    )
                }
                ScreenTab.ADD -> {
                    AddMagnetScreen(
                        initialMagnet = magnetInput,
                        onAdd = { magnet, title, kind, season, episode ->
                            coroutineScope.launch {
                                repository.addMagnet(magnet, title, kind, season, episode)
                            }
                        },
                        onSuccess = {
                            magnetInput = ""
                            currentTab = ScreenTab.DASHBOARD
                        }
                    )
                }
                ScreenTab.SUBSCRIPTIONS -> {
                    SubscriptionsScreen(
                        subscriptions = subscriptions,
                        onAddSubscription = { title, quality, voice ->
                            coroutineScope.launch {
                                repository.addSubscription(title, quality, voice)
                            }
                        },
                        onRemoveSubscription = { id ->
                            coroutineScope.launch {
                                repository.removeSubscription(id)
                            }
                        },
                        onImportRelease = { id ->
                            coroutineScope.launch {
                                repository.importSubscriptionRelease(id)
                                currentTab = ScreenTab.DASHBOARD
                            }
                        }
                    )
                }
                ScreenTab.SETTINGS -> {
                    SettingsScreen(
                        currentSettings = currentSettings,
                        lastLog = lastLog,
                        onSaveSettings = { updated ->
                            onSaveSettings(updated)
                            coroutineScope.launch {
                                repository.updateLimits(updated.episodeLimit, updated.capGb, updated.reserveGb)
                            }
                        },
                        onTestConnection = { settings ->
                            repository.testConnection(settings)
                        }
                    )
                }
            }

            // Pack details bottom sheet
            selectedPackForDetail?.let { pack ->
                PackDetailSheet(
                    pack = pack,
                    onDismiss = { selectedPackForDetail = null },
                    onChangeFrom = { season, episode ->
                        coroutineScope.launch {
                            repository.changeFrom(pack.hash, season, episode)
                            selectedPackForDetail = null
                        }
                    },
                    onFinishNextSeason = {
                        coroutineScope.launch {
                            repository.finishPack(pack.hash)
                            selectedPackForDetail = null
                        }
                    },
                    onRetry = {
                        coroutineScope.launch {
                            repository.retryPack(pack.hash)
                            selectedPackForDetail = null
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            repository.deletePack(pack.hash)
                            selectedPackForDetail = null
                        }
                    },
                    onDeleteEpisode = { episodeIndex ->
                        coroutineScope.launch {
                            repository.deleteEpisode(pack.hash, episodeIndex)
                            selectedPackForDetail = null
                        }
                    }
                )
            }
        }
    }
}
