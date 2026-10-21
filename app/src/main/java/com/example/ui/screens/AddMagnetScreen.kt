package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PackKind
import com.example.ui.theme.CyanPrimary
import com.example.util.TorrentParser
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AddMagnetScreen(
    initialMagnet: String = "",
    onAdd: (magnet: String, title: String, kind: PackKind, season: Int, startEpisode: Int) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var magnetUri by remember { mutableStateOf(initialMagnet) }
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(PackKind.SERIES) }
    var season by remember { mutableIntStateOf(1) }
    var startEpisode by remember { mutableIntStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(magnetUri) {
        if (magnetUri.isNotBlank()) {
            val extractedName = TorrentParser.extractDisplayName(magnetUri)
            if (extractedName != null && title.isBlank()) {
                title = extractedName
                kind = TorrentParser.detectKind(extractedName)
                season = TorrentParser.detectSeason(extractedName)
                startEpisode = TorrentParser.detectStartEpisode(extractedName)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Добавить ссылку",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Magnet Link Input
        OutlinedTextField(
            value = magnetUri,
            onValueChange = {
                magnetUri = it
                errorMessage = null
            },
            label = { Text("Ссылка magnet") },
            placeholder = { Text("magnet:?xt=urn:btih:...") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = clipboard.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            val text = clip.getItemAt(0).text?.toString() ?: ""
                            if (text.startsWith("magnet:")) {
                                magnetUri = text
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_paste_clipboard")
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = "Вставить")
                }
            },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_magnet_uri"),
            shape = RoundedCornerShape(12.dp)
        )

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название (необязательно)") },
            placeholder = { Text("Например: Дом Дракона") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_magnet_title"),
            shape = RoundedCornerShape(12.dp)
        )

        // Compact Type selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = kind == PackKind.SERIES,
                onClick = { kind = PackKind.SERIES },
                label = { Text("Сериал", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = kind == PackKind.MOVIE,
                onClick = { kind = PackKind.MOVIE },
                label = { Text("Фильм", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        // Stepper for Series
        if (kind == PackKind.SERIES) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Точка старта",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = season.toString(),
                            onValueChange = { str ->
                                val n = str.filter { it.isDigit() }.toIntOrNull()
                                if (n != null && n in 1..999) season = n
                            },
                            label = { Text("Сезон") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_start_season")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (season > 1) season-- },
                                enabled = season > 1,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Уменьшить сезон", modifier = Modifier.size(18.dp))
                            }
                            FilledTonalIconButton(
                                onClick = { season++ },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Увеличить сезон", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = startEpisode.toString(),
                            onValueChange = { str ->
                                val n = str.filter { it.isDigit() }.toIntOrNull()
                                if (n != null && n in 1..9999) startEpisode = n
                            },
                            label = { Text("Начальная серия") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_start_episode")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (startEpisode > 1) startEpisode-- },
                                enabled = startEpisode > 1,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Уменьшить серию", modifier = Modifier.size(18.dp))
                            }
                            FilledTonalIconButton(
                                onClick = { startEpisode++ },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Увеличить серию", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Submit Button
        Button(
            onClick = {
                if (magnetUri.isBlank() || !magnetUri.startsWith("magnet:")) {
                    errorMessage = "Укажите ссылку формата magnet:?xt=urn:btih:..."
                    return@Button
                }
                onAdd(magnetUri, title, kind, season, startEpisode)
                onSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_submit_magnet"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Добавить в очередь",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
