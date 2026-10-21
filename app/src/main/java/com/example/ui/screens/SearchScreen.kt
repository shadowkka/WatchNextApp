package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PackKind
import com.example.model.TorrentSearchResult
import com.example.ui.components.QualityChip
import com.example.ui.components.TrackerBadge
import com.example.ui.components.VoiceChip
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearch: suspend (query: String, kind: PackKind, minSeeders: Int, voice: String?, quality: String?) -> List<TorrentSearchResult>,
    onAddToQueue: (TorrentSearchResult, season: Int, episode: Int, kind: PackKind) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("Дом Дракона") }
    var selectedKind by remember { mutableStateOf(PackKind.SERIES) }
    var selectedQuality by remember { mutableStateOf<String?>(null) }
    var selectedVoice by remember { mutableStateOf<String?>(null) }
    var minSeeders by remember { mutableIntStateOf(0) }

    var results by remember { mutableStateOf<List<TorrentSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var selectedForAdd by remember { mutableStateOf<TorrentSearchResult?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun triggerSearch() {
        if (searchQuery.isBlank()) return
        isSearching = true
        kotlinx.coroutines.CoroutineScope(coroutineScope.coroutineContext).run {
            coroutineScope.run {
                // launch search
            }
        }
    }

    LaunchedEffect(searchQuery, selectedKind, selectedQuality, selectedVoice, minSeeders) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            results = onSearch(searchQuery, selectedKind, minSeeders, selectedVoice, selectedQuality)
            isSearching = false
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Поиск торрентов (Jackett)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Название фильма или сериала...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_torrent_search"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kind selector & Filter chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedKind == PackKind.SERIES,
                        onClick = { selectedKind = PackKind.SERIES },
                        label = { Text("Сериалы") },
                        leadingIcon = if (selectedKind == PackKind.SERIES) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = selectedKind == PackKind.MOVIE,
                        onClick = { selectedKind = PackKind.MOVIE },
                        label = { Text("Фильмы") },
                        leadingIcon = if (selectedKind == PackKind.MOVIE) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )

                    // Quality filters
                    listOf("1080p", "2160p", "720p").forEach { q ->
                        FilterChip(
                            selected = selectedQuality == q,
                            onClick = { selectedQuality = if (selectedQuality == q) null else q },
                            label = { Text(q) }
                        )
                    }

                    // Studio filters
                    listOf("LostFilm", "HDRezka", "Кубик в кубе", "Дубляж").forEach { v ->
                        FilterChip(
                            selected = selectedVoice == v,
                            onClick = { selectedVoice = if (selectedVoice == v) null else v },
                            label = { Text(v) }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (results.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ничего не найдено",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Попробуй изменить запрос или фильтры качества",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                ) {
                    items(results) { item ->
                        SearchResultCard(
                            result = item,
                            onAdd = { selectedForAdd = item }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog before adding to WatchNext queue
    selectedForAdd?.let { item ->
        var confirmSeason by remember { mutableIntStateOf(item.detectedSeason) }
        var confirmEpisode by remember { mutableIntStateOf(item.detectedStartEpisode) }
        var confirmKind by remember { mutableStateOf(item.detectedKind) }

        AlertDialog(
            onDismissRequest = { selectedForAdd = null },
            title = { Text("Добавить в очередь?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Размер: ${item.sizeFormatted} · Сиды: ${item.seeders}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = confirmKind == PackKind.SERIES,
                            onClick = { confirmKind = PackKind.SERIES },
                            label = { Text("Сериал") }
                        )
                        FilterChip(
                            selected = confirmKind == PackKind.MOVIE,
                            onClick = { confirmKind = PackKind.MOVIE },
                            label = { Text("Фильм") }
                        )
                    }

                    if (confirmKind == PackKind.SERIES) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Начать с: Сезон $confirmSeason, Серия $confirmEpisode")
                            Row {
                                FilledTonalIconButton(
                                    onClick = { if (confirmEpisode > 1) confirmEpisode-- },
                                    enabled = confirmEpisode > 1
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                }
                                FilledTonalIconButton(
                                    onClick = { confirmEpisode++ }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddToQueue(item, confirmSeason, confirmEpisode, confirmKind)
                        selectedForAdd = null
                    },
                    modifier = Modifier.testTag("btn_confirm_add_search")
                ) {
                    Text("Добавить в очередь")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForAdd = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SearchResultCard(
    result: TorrentSearchResult,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_card_${result.tracker}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TrackerBadge(tracker = result.tracker)
                    QualityChip(quality = result.quality)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Seeders",
                        tint = StatusGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${result.seeders}",
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen,
                        fontSize = 12.sp
                    )
                    if (result.peers != null) {
                        Text(
                            text = "/ ${result.peers}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Studio badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                result.voices.forEach { voice ->
                    VoiceChip(voice = voice)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Size & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.sizeFormatted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onAdd,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_add_to_queue")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("В очередь", fontSize = 12.sp)
                }
            }
        }
    }
}
