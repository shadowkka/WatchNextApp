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
import androidx.compose.ui.text.style.TextOverflow
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

    var rawResults by remember { mutableStateOf<List<TorrentSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var selectedForAdd by remember { mutableStateOf<TorrentSearchResult?>(null) }

    // Search on query or kind change with debounce
    LaunchedEffect(searchQuery, selectedKind) {
        val q = searchQuery.trim()
        selectedQuality = null
        selectedVoice = null
        if (q.isNotBlank()) {
            kotlinx.coroutines.delay(400)
            isSearching = true
            try {
                rawResults = onSearch(q, selectedKind, 0, null, null)
            } catch (_: Exception) {
                rawResults = emptyList()
            } finally {
                isSearching = false
            }
        } else {
            rawResults = emptyList()
            isSearching = false
        }
    }

    // Dynamic filters extracted directly from real search results
    val availableVoices = remember(rawResults) {
        rawResults.flatMap { it.voices }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.contains("не указан", ignoreCase = true) }
            .distinct()
            .sorted()
    }

    val availableQualities = remember(rawResults) {
        val standard = listOf("2160p", "1080p", "720p", "4K")
        standard.filter { q ->
            rawResults.any { it.quality.contains(q, ignoreCase = true) }
        }
    }

    // Fast, instant client-side filtering without network lag
    val displayResults = remember(rawResults, selectedVoice, selectedQuality) {
        rawResults.filter { item ->
            val matchesVoice = selectedVoice == null || item.voices.any { it.contains(selectedVoice!!, ignoreCase = true) }
            val matchesQuality = selectedQuality == null || item.quality.contains(selectedQuality!!, ignoreCase = true)
            matchesVoice && matchesQuality
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
                    text = "Поиск торрентов",
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
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
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

                // Kind selector & Dynamic filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedKind == PackKind.SERIES,
                        onClick = { selectedKind = PackKind.SERIES },
                        label = { Text("Сериалы", fontSize = 12.sp) }
                    )

                    FilterChip(
                        selected = selectedKind == PackKind.MOVIE,
                        onClick = { selectedKind = PackKind.MOVIE },
                        label = { Text("Фильмы", fontSize = 12.sp) }
                    )

                    if (selectedQuality != null || selectedVoice != null) {
                        SuggestionChip(
                            onClick = {
                                selectedQuality = null
                                selectedVoice = null
                            },
                            label = { Text("Сбросить", fontSize = 11.sp, color = CyanPrimary) }
                        )
                    }

                    // Dynamically extracted qualities from actual results
                    availableQualities.forEach { q ->
                        FilterChip(
                            selected = selectedQuality == q,
                            onClick = { selectedQuality = if (selectedQuality == q) null else q },
                            label = { Text(q, fontSize = 12.sp) }
                        )
                    }

                    // Dynamically extracted studios/voices from actual results
                    availableVoices.forEach { v ->
                        FilterChip(
                            selected = selectedVoice == v,
                            onClick = { selectedVoice = if (selectedVoice == v) null else v },
                            label = { Text(v, fontSize = 12.sp) }
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
            } else if (displayResults.isEmpty()) {
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (rawResults.isNotEmpty()) "Нет раздач с выбранными фильтрами" else "Ничего не найдено",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
                ) {
                    items(displayResults) { item ->
                        CleanSearchResultCard(
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.sizeFormatted} · ${item.seeders} сидов",
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
                            Text("Сезон $confirmSeason, с серии $confirmEpisode")
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
                    Text("Добавить")
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
fun CleanSearchResultCard(
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TrackerBadge(tracker = result.tracker)
                    QualityChip(quality = result.quality)
                    result.voices.take(1).forEach { voice ->
                        VoiceChip(voice = voice)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Сиды",
                        tint = StatusGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${result.seeders}",
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Size & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.sizeFormatted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onAdd,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_add_to_queue")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("В очередь", fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}
