package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.PackItem
import com.example.model.PackKind
import com.example.model.QueueStatus
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    status: QueueStatus,
    packs: List<PackItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onTogglePause: () -> Unit,
    onSelectPack: (PackItem) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = CyanPrimary,
                contentColor = DarkBackground,
                modifier = Modifier.testTag("fab_add_magnet")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить магнет")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            // Header with status indicator & Pause/Resume
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WatchNext",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (status.isPaused) "Очередь на паузе" else "Медиа-очередь активна",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status.isPaused) StatusAmber else StatusGreen
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("btn_refresh_dashboard")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                        }

                        Button(
                            onClick = onTogglePause,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status.isPaused) StatusGreen else StatusAmber
                            ),
                            modifier = Modifier.testTag("btn_toggle_pause")
                        ) {
                            Icon(
                                if (status.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (status.isPaused) "Продолжить" else "Пауза",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Alerts
            if (status.limitAlert != null || status.serviceAlert != null) {
                item {
                    val alertText = status.limitAlert ?: status.serviceAlert ?: ""
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusAmber.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAmber)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = alertText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Storage & Limit Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Диск (свободно)",
                        value = "${String.format(java.util.Locale.US, "%.1f", status.diskFreeGb)} ГБ",
                        subtitle = "Резерв: ${status.reserveGb.toInt()} ГБ",
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Лимит серий",
                        value = "${status.episodeCountLimit}",
                        subtitle = if (status.capGb != null) "Объём: ${status.capGb.toInt()} ГБ" else "Объём: без лимита",
                        icon = Icons.Default.ViewAgenda,
                        iconTint = IndigoSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_btn_search")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Поиск Jackett", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToAdd,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_btn_add")
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🧲 Вставить magnet", fontSize = 12.sp)
                    }
                }
            }

            // Queue List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Активная очередь (${packs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Всего скачивается: ${status.activeDownloadsCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Queue Pack Cards
            if (packs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Очередь пуста",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Добавь раздачу через поиск или magnet ссылку",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(packs, key = { it.hash }) { pack ->
                    PackCard(
                        pack = pack,
                        onClick = { onSelectPack(pack) }
                    )
                }
            }
        }
    }
}

@Composable
fun PackCard(
    pack: PackItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("pack_card_${pack.hash.take(8)}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (pack.kind == PackKind.SERIES)
                            "Сезон ${pack.season} (начало с ${pack.startEpisode} серии) · Готово: ${pack.readyEpisodes}/${pack.totalEpisodes}"
                        else
                            "Полнометражный фильм · ${pack.totalSizeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(state = pack.state)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { pack.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CyanPrimary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pack.progressPercent}% (${pack.downloadedSizeFormatted} из ${pack.totalSizeFormatted})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pack.speedFormatted.isNotBlank()) {
                    Text(
                        text = pack.speedFormatted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary
                    )
                }
            }
        }
    }
}
