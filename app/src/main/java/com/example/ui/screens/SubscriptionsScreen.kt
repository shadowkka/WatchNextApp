package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShowSearchResult
import com.example.model.SubscriptionItem
import com.example.model.SubscriptionStatus
import com.example.ui.components.QualityChip
import com.example.ui.components.VoiceChip
import com.example.ui.theme.*

@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionItem>,
    onAddSubscription: (title: String, quality: String, voice: String, tvmazeId: Int?) -> Unit,
    onRemoveSubscription: (id: String) -> Unit,
    onImportRelease: (id: String) -> Unit,
    onSearchShows: suspend (query: String) -> List<ShowSearchResult> = { emptyList() },
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IndigoSecondary,
                contentColor = DarkBackground,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_subscription")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить подписку")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Подписки",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${subscriptions.size} сериалов",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (subscriptions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Subscriptions,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нет подписок",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(subscriptions, key = { it.id }) { sub ->
                        CleanSubscriptionCard(
                            sub = sub,
                            onImport = { onImportRelease(sub.id) },
                            onDelete = { onRemoveSubscription(sub.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var showName by remember { mutableStateOf("") }
        var selectedTvmazeId by remember { mutableStateOf<Int?>(null) }
        var preferredQuality by remember { mutableStateOf("1080p") }
        var preferredVoice by remember { mutableStateOf("LostFilm") }
        var showSuggestions by remember { mutableStateOf<List<ShowSearchResult>>(emptyList()) }
        var isSearchingShows by remember { mutableStateOf(false) }

        LaunchedEffect(showName) {
            val query = showName.trim()
            if (query.length >= 2 && selectedTvmazeId == null) {
                kotlinx.coroutines.delay(300)
                isSearchingShows = true
                try {
                    showSuggestions = onSearchShows(query)
                } catch (_: Exception) {
                    showSuggestions = emptyList()
                } finally {
                    isSearchingShows = false
                }
            } else {
                showSuggestions = emptyList()
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Подписка на сериал") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = showName,
                        onValueChange = {
                            showName = it
                            selectedTvmazeId = null
                        },
                        label = { Text("Название сериала") },
                        placeholder = { Text("Разделение / Severance") },
                        trailingIcon = {
                            if (isSearchingShows) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else if (selectedTvmazeId != null) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Привязан к TVmaze",
                                    tint = StatusGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else if (showName.isNotEmpty()) {
                                IconButton(onClick = {
                                    showName = ""
                                    selectedTvmazeId = null
                                    showSuggestions = emptyList()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sub_name")
                    )

                    if (selectedTvmazeId != null) {
                        Text(
                            text = "✓ Сериал найден в TVmaze (расписание активно)",
                            color = StatusGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (showSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Подсказки TVmaze:",
                                    fontSize = 11.sp,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                showSuggestions.take(4).forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                showName = item.name
                                                selectedTvmazeId = item.id
                                                showSuggestions = emptyList()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val extra = listOfNotNull(
                                                item.premiered.takeIf { it.isNotBlank() },
                                                item.status.takeIf { it.isNotBlank() }
                                            ).joinToString(" · ")
                                            if (extra.isNotBlank()) {
                                                Text(
                                                    text = extra,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Выбрать",
                                            fontSize = 11.sp,
                                            color = CyanPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Качество:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1080p", "4K", "720p").forEach { q ->
                            FilterChip(
                                selected = preferredQuality == q,
                                onClick = { preferredQuality = q },
                                label = { Text(q, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Озвучка:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("LostFilm", "HDRezka", "Кубик в кубе", "Любая").forEach { v ->
                            FilterChip(
                                selected = preferredVoice == v,
                                onClick = { preferredVoice = v },
                                label = { Text(v, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (showName.isNotBlank()) {
                            onAddSubscription(showName, preferredQuality, preferredVoice, selectedTvmazeId)
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_sub")
                ) {
                    Text("Подписаться")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun CleanSubscriptionCard(
    sub: SubscriptionItem,
    onImport: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sub_card_${sub.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sub.showTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status & Tags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QualityChip(quality = sub.preferredQuality)
                VoiceChip(voice = sub.preferredVoice)

                val (statusColor, statusBg) = when (sub.status) {
                    SubscriptionStatus.ACTIVE -> Pair(StatusBlue, StatusBlue.copy(alpha = 0.12f))
                    SubscriptionStatus.EPISODE_OUT -> Pair(StatusAmber, StatusAmber.copy(alpha = 0.12f))
                    SubscriptionStatus.RELEASE_FOUND -> Pair(StatusGreen, StatusGreen.copy(alpha = 0.12f))
                    SubscriptionStatus.UP_TO_DATE -> Pair(StatusGreen, StatusGreen.copy(alpha = 0.12f))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = sub.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
                        maxLines = 1
                    )
                }
            }

            // Quick download if release found
            if (sub.status == SubscriptionStatus.RELEASE_FOUND && sub.candidateReleaseTitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onImport,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_import_release_${sub.id}")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Скачать найденный релиз", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}
