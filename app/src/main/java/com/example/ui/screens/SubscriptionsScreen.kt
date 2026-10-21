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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubscriptionItem
import com.example.model.SubscriptionStatus
import com.example.ui.components.QualityChip
import com.example.ui.components.VoiceChip
import com.example.ui.theme.*

@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionItem>,
    onAddSubscription: (title: String, quality: String, voice: String) -> Unit,
    onRemoveSubscription: (id: String) -> Unit,
    onImportRelease: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IndigoSecondary,
                contentColor = DarkBackground,
                modifier = Modifier.testTag("fab_add_subscription")
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "Добавить подписку")
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
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Подписки на релизы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Раз в 2 часа WatchNext проверяет даты TVmaze и торренты Jackett. Когда серия выходит в нужной озвучке, раздачу можно импортировать в один тап.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (subscriptions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Subscriptions,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Нет активных подписок",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Нажми кнопку ниже, чтобы отслеживать новые сезоны и серии",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(subscriptions, key = { it.id }) { sub ->
                        SubscriptionCard(
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
        var preferredQuality by remember { mutableStateOf("1080p · WEB-DL") }
        var preferredVoice by remember { mutableStateOf("LostFilm") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Новая подписка на сериал") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = showName,
                        onValueChange = { showName = it },
                        label = { Text("Название сериала") },
                        placeholder = { Text("Например: Разделение (Severance)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sub_name")
                    )

                    Text("Желаемое качество:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1080p · WEB-DL", "2160p 4K", "720p").forEach { q ->
                            FilterChip(
                                selected = preferredQuality == q,
                                onClick = { preferredQuality = q },
                                label = { Text(q, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Озвучка / студия:", style = MaterialTheme.typography.labelMedium)
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
                            onAddSubscription(showName, preferredQuality, preferredVoice)
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
fun SubscriptionCard(
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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sub.showTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Остановлен на S${String.format("%02d", sub.lastSeason)}E${String.format("%02d", sub.lastEpisode)} · След: ${sub.nextEpisodeLabel ?: "Ожидается дата"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QualityChip(quality = sub.preferredQuality)
                VoiceChip(voice = sub.preferredVoice)

                val (statusColor, statusText) = when (sub.status) {
                    SubscriptionStatus.ACTIVE -> Pair(StatusBlue, "Отслеживание")
                    SubscriptionStatus.EPISODE_OUT -> Pair(StatusAmber, "Серия вышла — ждём озвучку")
                    SubscriptionStatus.RELEASE_FOUND -> Pair(StatusGreen, "Раздача найдена!")
                    SubscriptionStatus.UP_TO_DATE -> Pair(StatusGreen, "Актуально")
                }

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            // Release Found prompt
            if (sub.status == SubscriptionStatus.RELEASE_FOUND && sub.candidateReleaseTitle != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = sub.candidateReleaseTitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onImport,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_import_release_${sub.id}")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Добавить в очередь", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
