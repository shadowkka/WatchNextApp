package com.example.ui.screens

import androidx.compose.foundation.background
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
        var preferredQuality by remember { mutableStateOf("1080p") }
        var preferredVoice by remember { mutableStateOf("LostFilm") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Подписка на сериал") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = showName,
                        onValueChange = { showName = it },
                        label = { Text("Название сериала") },
                        placeholder = { Text("Разделение / Severance") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sub_name")
                    )

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
