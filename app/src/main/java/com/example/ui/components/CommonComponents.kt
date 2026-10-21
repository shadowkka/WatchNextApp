package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EpisodeStatus
import com.example.model.PackState
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    state: PackState,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (state) {
        PackState.DOWNLOADING -> Triple(StatusBlue.copy(alpha = 0.15f), StatusBlue, Icons.Default.Download)
        PackState.READY -> Triple(StatusGreen.copy(alpha = 0.15f), StatusGreen, Icons.Default.CheckCircle)
        PackState.CHECKING -> Triple(StatusAmber.copy(alpha = 0.15f), StatusAmber, Icons.Default.Sync)
        PackState.STOPPED -> Triple(StatusAmber.copy(alpha = 0.15f), StatusAmber, Icons.Default.Pause)
        PackState.METADATA -> Triple(CyanPrimary.copy(alpha = 0.15f), CyanPrimary, Icons.Default.Info)
        PackState.COMPLETE -> Triple(StatusGreen.copy(alpha = 0.2f), StatusGreen, Icons.Default.DoneAll)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
        Text(
            text = state.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EpisodeStatusBadge(
    status: EpisodeStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        EpisodeStatus.COMPLETED -> Pair(StatusGreen.copy(alpha = 0.15f), StatusGreen)
        EpisodeStatus.DOWNLOADING -> Pair(StatusBlue.copy(alpha = 0.15f), StatusBlue)
        EpisodeStatus.WAITING_LIMIT -> Pair(StatusAmber.copy(alpha = 0.15f), StatusAmber)
        EpisodeStatus.PLAYED -> Pair(IndigoSecondary.copy(alpha = 0.15f), IndigoSecondary)
        EpisodeStatus.SKIPPED -> Pair(Color.Gray.copy(alpha = 0.15f), Color.Gray)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TrackerBadge(
    tracker: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = tracker,
            color = CyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QualityChip(
    quality: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(IndigoSecondary.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = quality,
            color = IndigoSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VoiceChip(
    voice: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = voice,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = CyanPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_card_${title.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
