package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Save
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
import kotlinx.coroutines.launch

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Лимиты очереди",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )

                // Episode limit slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Серий вперед:", fontSize = 13.sp)
                        Text("$episodeLimit", fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                    Slider(
                        value = episodeLimit.toFloat(),
                        onValueChange = { episodeLimit = it.toInt() },
                        valueRange = 1f..20f,
                        steps = 18,
                        modifier = Modifier.testTag("slider_episode_limit")
                    )
                }

                // Reserve GB slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Резерв места на диске:", fontSize = 13.sp)
                        Text("${reserveGb.toInt()} ГБ", fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                    Slider(
                        value = reserveGb,
                        onValueChange = { reserveGb = it },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.testTag("slider_reserve_gb")
                    )
                }

                // Cap GB
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (hasCapGb) "Лимит объёма: ${capGb.toInt()} ГБ" else "Лимит объёма: без ограничений",
                        fontSize = 13.sp
                    )
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
                            val (success, message) = onTestConnection(currentSettings.copy(serverUrl = serverUrl))
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
                    modifier = Modifier.testTag("switch_demo_mode")
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val updated = currentSettings.copy(
                    serverUrl = serverUrl,
                    apiToken = apiToken,
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
                .testTag("btn_save_settings"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
