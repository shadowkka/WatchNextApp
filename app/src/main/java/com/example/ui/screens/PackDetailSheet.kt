package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EpisodeItem
import com.example.model.PackItem
import com.example.model.PackKind
import com.example.ui.components.EpisodeStatusBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackDetailSheet(
    pack: PackItem,
    onDismiss: () -> Unit,
    onChangeFrom: (season: Int, episode: Int) -> Unit,
    onFinishNextSeason: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    var showChangeFromDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${pack.kind.label} · Сезон ${pack.season}, с серии ${pack.startEpisode} · ${pack.totalSizeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(state = pack.state)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons matching Telegram bot commands
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pack.kind == PackKind.SERIES) {
                    FilledTonalButton(
                        onClick = { showChangeFromDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_change_from"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сезон/серия (/from)", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onFinishNextSeason,
                        enabled = !pack.noNextSeason,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_finish_season"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (pack.noNextSeason) "Завершён" else "Финал (/finish)", fontSize = 12.sp)
                    }
                }

                FilledTonalIconButton(
                    onClick = onRetry,
                    modifier = Modifier.testTag("action_retry_pack")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Recheck (/retry)")
                }

                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.testTag("action_delete_pack")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusRose)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Episodes / Files list
            Text(
                text = "Файлы раздачи (${pack.episodes.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pack.episodes) { episode ->
                    EpisodeRowItem(episode = episode)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showChangeFromDialog) {
        var inputSeason by remember { mutableIntStateOf(pack.season) }
        var inputEpisode by remember { mutableIntStateOf(pack.startEpisode) }

        AlertDialog(
            onDismissRequest = { showChangeFromDialog = false },
            title = { Text("Начать просмотр с:") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Сезон: $inputSeason", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (inputSeason > 1) inputSeason-- },
                                enabled = inputSeason > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            FilledTonalIconButton(
                                onClick = { inputSeason++ }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Серия: $inputEpisode", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (inputEpisode > 1) inputEpisode-- },
                                enabled = inputEpisode > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            FilledTonalIconButton(
                                onClick = { inputEpisode++ }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onChangeFrom(inputSeason, inputEpisode)
                        showChangeFromDialog = false
                    }
                ) {
                    Text("Применить (/from)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeFromDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Удалить раздачу?") },
            text = {
                Text("Раздача будет удалена из очереди и торрент-клиента qBittorrent. Hardlink в Jellyfin также будет очищен.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRose)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun EpisodeRowItem(episode: EpisodeItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = episode.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                EpisodeStatusBadge(status = episode.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = episode.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { episode.progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = CyanPrimary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(episode.progress * 100).toInt()}% · ${episode.sizeFormatted}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
