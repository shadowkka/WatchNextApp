package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.AppSettings
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose

@Composable
fun SettingsScreen(
    currentSettings: AppSettings,
    lastLog: String?,
    onSaveSettings: (AppSettings) -> Unit,
    onTestConnection: suspend (AppSettings) -> Pair<Boolean, String>,
    modifier: Modifier = Modifier
) {
    var serverUrl by remember(currentSettings) { mutableStateOf(currentSettings.serverUrl) }
    var apiToken by remember(currentSettings) { mutableStateOf(currentSettings.apiToken) }
    var qbitUrl by remember(currentSettings) { mutableStateOf(currentSettings.qbitUrl) }
    var qbitUser by remember(currentSettings) { mutableStateOf(currentSettings.qbitUsername) }
    var qbitPass by remember(currentSettings) { mutableStateOf(currentSettings.qbitPassword) }
    var jackettUrl by remember(currentSettings) { mutableStateOf(currentSettings.jackettUrl) }
    var jackettKey by remember(currentSettings) { mutableStateOf(currentSettings.jackettApiKey) }
    var jellyfinUrl by remember(currentSettings) { mutableStateOf(currentSettings.jellyfinUrl) }
    var isDemoMode by remember(currentSettings) { mutableStateOf(currentSettings.isDemoMode) }

    var episodeLimit by remember(currentSettings) { mutableIntStateOf(currentSettings.episodeLimit) }
    var hasCapGb by remember(currentSettings) { mutableStateOf(currentSettings.capGb != null) }
    var capGb by remember(currentSettings) { mutableFloatStateOf(currentSettings.capGb ?: 50f) }
    var reserveGb by remember(currentSettings) { mutableFloatStateOf(currentSettings.reserveGb) }

    var testStatusText by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testSucceeded by remember { mutableStateOf<Boolean?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Параметры и сервер",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Queue Limits Section (/limit, /reserve)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Лимиты очереди (/limit, /reserve)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                // Episode count limit
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Лимит активных серий вперед:")
                        Text("$episodeLimit серий", fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                    Slider(
                        value = episodeLimit.toFloat(),
                        onValueChange = { episodeLimit = it.toInt() },
                        valueRange = 1f..20f,
                        steps = 18,
                        modifier = Modifier.testTag("slider_episode_limit")
                    )
                }

                // Reserve GB
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Резерв свободного места на диске:")
                        Text("${reserveGb.toInt()} ГБ", fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                    Slider(
                        value = reserveGb,
                        onValueChange = { reserveGb = it },
                        valueRange = 1f..50f,
                        steps = 48,
                        modifier = Modifier.testTag("slider_reserve_gb")
                    )
                }

                // Cap GB
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ограничение общего объёма (ГБ)")
                        Text(
                            if (hasCapGb) "${capGb.toInt()} ГБ максимум" else "Без ограничений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasCapGb,
                        onCheckedChange = { hasCapGb = it },
                        modifier = Modifier.testTag("switch_cap_gb")
                    )
                }

                if (hasCapGb) {
                    Slider(
                        value = capGb,
                        onValueChange = { capGb = it },
                        valueRange = 10f..200f,
                        steps = 18
                    )
                }
            }
        }

        // Demo Mode vs Real Server Toggle
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Демо-режим песочницы",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Позволяет исследовать и тестировать приложение с имитацией домашнего сервера WatchNext, qBittorrent и Jackett без поднятого ПК",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDemoMode,
                        onCheckedChange = { isDemoMode = it },
                        modifier = Modifier.testTag("switch_demo_mode")
                    )
                }
            }
        }

        // Server Connection Section
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Подключение к домашнему серверу",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("WatchNext Server URL") },
                    placeholder = { Text("http://192.168.1.100:8000") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_server_url")
                )

                OutlinedTextField(
                    value = apiToken,
                    onValueChange = { apiToken = it },
                    label = { Text("API токен (опционально)") },
                    placeholder = { Text("Секретный токен авторизации") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Прямой доступ к службам (при необходимости):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = qbitUrl,
                    onValueChange = { qbitUrl = it },
                    label = { Text("qBittorrent Web UI") },
                    placeholder = { Text("http://192.168.1.100:8080") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qbitUser,
                        onValueChange = { qbitUser = it },
                        label = { Text("qBit Логин") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = qbitPass,
                        onValueChange = { qbitPass = it },
                        label = { Text("qBit Пароль") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = jackettUrl,
                    onValueChange = { jackettUrl = it },
                    label = { Text("Jackett Torznab URL") },
                    placeholder = { Text("http://192.168.1.100:9117") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = jellyfinUrl,
                    onValueChange = { jellyfinUrl = it },
                    label = { Text("Jellyfin Server URL") },
                    placeholder = { Text("http://192.168.1.100:8096") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Test Connection Button
                OutlinedButton(
                    onClick = {
                        isTestingConnection = true
                        testStatusText = null
                        kotlinx.coroutines.CoroutineScope(coroutineScope.coroutineContext).run {
                            // Launch test connection
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_test_connection")
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Проверить доступность сервера")
                }

                if (testStatusText != null) {
                    Text(
                        text = testStatusText ?: "",
                        color = if (testSucceeded == true) StatusGreen else StatusRose,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val updated = currentSettings.copy(
                    serverUrl = serverUrl,
                    apiToken = apiToken,
                    qbitUrl = qbitUrl,
                    qbitUsername = qbitUser,
                    qbitPassword = qbitPass,
                    jackettUrl = jackettUrl,
                    jackettApiKey = jackettKey,
                    jellyfinUrl = jellyfinUrl,
                    episodeLimit = episodeLimit,
                    capGb = if (hasCapGb) capGb else null,
                    reserveGb = reserveGb,
                    isDemoMode = isDemoMode
                )
                onSaveSettings(updated)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_settings")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Сохранить настройки")
        }

        // Diagnostic Log card
        if (lastLog != null) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Лог: $lastLog",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
