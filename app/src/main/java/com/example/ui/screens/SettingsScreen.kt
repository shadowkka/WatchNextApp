package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.ui.components.SettingSliderCard
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRose
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentSettings: AppSettings,
    lastLog: String?,
    onSaveSettings: (AppSettings) -> Unit,
    onTestConnection: suspend (AppSettings) -> Pair<Boolean, String>,
    modifier: Modifier = Modifier
) {
    // Preserve local user input across recompositions and background syncs
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    var serverUrl by rememberSaveable { mutableStateOf(currentSettings.serverUrl) }
    var apiToken by rememberSaveable { mutableStateOf(currentSettings.apiToken) }
    var isDemoMode by rememberSaveable { mutableStateOf(currentSettings.isDemoMode) }

    var episodeLimit by rememberSaveable { mutableIntStateOf(currentSettings.episodeLimit) }
    var hasCapGb by rememberSaveable { mutableStateOf(currentSettings.capGb != null) }
    var capGb by rememberSaveable { mutableFloatStateOf(currentSettings.capGb ?: 50f) }
    var reserveGb by rememberSaveable { mutableFloatStateOf(currentSettings.reserveGb) }
    var pollIntervalSeconds by rememberSaveable { mutableIntStateOf(currentSettings.pollIntervalSeconds) }

    var testStatusText by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testSucceeded by remember { mutableStateOf<Boolean?>(null) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Sync only on first arrival of persisted settings from DataStore
    LaunchedEffect(currentSettings) {
        if (!isInitialized) {
            serverUrl = currentSettings.serverUrl
            apiToken = currentSettings.apiToken
            isDemoMode = currentSettings.isDemoMode
            episodeLimit = currentSettings.episodeLimit
            hasCapGb = currentSettings.capGb != null
            capGb = currentSettings.capGb ?: 50f
            reserveGb = currentSettings.reserveGb
            pollIntervalSeconds = currentSettings.pollIntervalSeconds
            isInitialized = true
        }
    }

    // High contrast switch colors with visible thumb and border in both on and off states
    val modernSwitchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = CyanPrimary,
        uncheckedThumbColor = Color(0xFFCBD5E1),
        uncheckedTrackColor = Color(0xFF334155),
        uncheckedBorderColor = Color(0xFF64748B)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Queue Limits
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Лимиты очереди",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                // Episode limit
                SettingSliderCard(
                    title = "Серий вперед",
                    subtitle = "Количество серий для одновременного скачивания",
                    value = episodeLimit.toFloat(),
                    valueRange = 1f..30f,
                    step = 1f,
                    unitLabel = "серий",
                    presets = listOf("3" to 3f, "5" to 5f, "10" to 10f, "15" to 15f, "20" to 20f),
                    onValueChange = { episodeLimit = it.toInt() },
                    testTagInput = "input_episode_limit",
                    testTagSlider = "slider_episode_limit"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Reserve GB
                SettingSliderCard(
                    title = "Резерв места на диске",
                    subtitle = "Минимальный свободный объём на диске",
                    value = reserveGb,
                    valueRange = 0f..100f,
                    step = 1f,
                    unitLabel = "ГБ",
                    presets = listOf("2 ГБ" to 2f, "5 ГБ" to 5f, "10 ГБ" to 10f, "20 ГБ" to 20f, "50 ГБ" to 50f),
                    onValueChange = { reserveGb = it },
                    testTagInput = "input_reserve_gb",
                    testTagSlider = "slider_reserve_gb"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Cap GB Toggle & Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasCapGb) "Лимит объёма хранилища" else "Лимит объёма: без ограничений",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (hasCapGb) "Останавливать скачивание при превышении" else "Скачивать без ограничения по общему весу",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasCapGb,
                        onCheckedChange = { hasCapGb = it },
                        colors = modernSwitchColors,
                        modifier = Modifier.testTag("switch_cap_gb")
                    )
                }

                if (hasCapGb) {
                    SettingSliderCard(
                        title = "Максимальный объём",
                        value = capGb,
                        valueRange = 10f..500f,
                        step = 10f,
                        unitLabel = "ГБ",
                        presets = listOf("25 ГБ" to 25f, "50 ГБ" to 50f, "100 ГБ" to 100f, "200 ГБ" to 200f, "500 ГБ" to 500f),
                        onValueChange = { capGb = it },
                        testTagInput = "input_cap_gb",
                        testTagSlider = "slider_cap_gb"
                    )
                }
            }
        }

        // Server Connection
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Сервер WatchNext",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Адрес сервера") },
                    placeholder = { Text("http://192.168.1.100:8000") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_server_url"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = apiToken,
                    onValueChange = { apiToken = it },
                    label = { Text("API токен") },
                    placeholder = { Text("Необязательно") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedButton(
                    onClick = {
                        isTestingConnection = true
                        testStatusText = null
                        coroutineScope.launch {
                            val (success, message) = onTestConnection(
                                currentSettings.copy(
                                    serverUrl = serverUrl.trim(),
                                    apiToken = apiToken.trim()
                                )
                            )
                            testSucceeded = success
                            testStatusText = message
                            isTestingConnection = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_test_connection"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Icon(Icons.Outlined.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Проверить связь", fontSize = 13.sp)
                }

                if (testStatusText != null) {
                    Text(
                        text = testStatusText ?: "",
                        color = if (testSucceeded == true) StatusGreen else StatusRose,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Background Polling Interval
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Автообновление очереди",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
                SettingSliderCard(
                    title = "Период опроса сервера",
                    subtitle = "Частота синхронизации статуса и загрузок (1–30 сек)",
                    value = pollIntervalSeconds.toFloat(),
                    valueRange = 1f..30f,
                    step = 1f,
                    unitLabel = "сек",
                    presets = listOf("1с" to 1f, "2с" to 2f, "3с" to 3f, "5с" to 5f, "10с" to 10f),
                    onValueChange = { pollIntervalSeconds = it.toInt() },
                    testTagInput = "input_poll_interval",
                    testTagSlider = "slider_poll_interval"
                )
            }
        }

        // Demo Mode Toggle
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Демо-режим", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Имитация очередей без сервера", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isDemoMode,
                    onCheckedChange = { isDemoMode = it },
                    colors = modernSwitchColors,
                    modifier = Modifier.testTag("switch_demo_mode")
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val updated = currentSettings.copy(
                    serverUrl = serverUrl.trim(),
                    apiToken = apiToken.trim(),
                    episodeLimit = episodeLimit,
                    capGb = if (hasCapGb) capGb else null,
                    reserveGb = reserveGb,
                    pollIntervalSeconds = pollIntervalSeconds,
                    isDemoMode = isDemoMode
                )
                onSaveSettings(updated)
                showSaveSuccess = true
                coroutineScope.launch {
                    delay(2500)
                    showSaveSuccess = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_settings"),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (showSaveSuccess) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Настройки сохранены!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
