package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PackKind
import com.example.ui.theme.CyanPrimary
import com.example.util.TorrentParser

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
    var isSubmitting by remember { mutableStateOf(false) }

    // Parse whenever magnet link changes
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Добавить магнет-ссылку",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Вставь ссылку формата magnet:?xt=urn:btih:... WatchNext автоматически остановит сидирование после загрузки и создаст hardlink для Jellyfin.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Magnet Link Input
        OutlinedTextField(
            value = magnetUri,
            onValueChange = {
                magnetUri = it
                errorMessage = null
            },
            label = { Text("Magnet URI") },
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
                    Icon(Icons.Default.ContentPaste, contentDescription = "Вставить из буфера")
                }
            },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_magnet_uri"),
            shape = RoundedCornerShape(12.dp)
        )

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Название фильма или сериала") },
            placeholder = { Text("Например: Дом Дракона") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_magnet_title"),
            shape = RoundedCornerShape(12.dp)
        )

        // Kind selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = kind == PackKind.SERIES,
                onClick = { kind = PackKind.SERIES },
                label = { Text("Сериал (по сериям)") },
                leadingIcon = if (kind == PackKind.SERIES) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = kind == PackKind.MOVIE,
                onClick = { kind = PackKind.MOVIE },
                label = { Text("Фильм (целиком)") },
                leadingIcon = if (kind == PackKind.MOVIE) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                modifier = Modifier.weight(1f)
            )
        }

        // Stepper for Series Season & Start Episode
        if (kind == PackKind.SERIES) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Точка старта просмотра (/from)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Сезон: $season", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (season > 1) season-- },
                                enabled = season > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            FilledTonalIconButton(
                                onClick = { season++ }
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
                        Text("Серия: $startEpisode", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { if (startEpisode > 1) startEpisode-- },
                                enabled = startEpisode > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            FilledTonalIconButton(
                                onClick = { startEpisode++ }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // Error message if any
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button
        Button(
            onClick = {
                if (magnetUri.isBlank() || !magnetUri.startsWith("magnet:")) {
                    errorMessage = "Пожалуйста, укажи валидную ссылку формата magnet:?xt=urn:btih:..."
                    return@Button
                }
                isSubmitting = true
                onAdd(magnetUri, title, kind, season, startEpisode)
                isSubmitting = false
                onSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_submit_magnet"),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Добавить в очередь",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
