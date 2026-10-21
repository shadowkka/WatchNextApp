package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val (dotColor, textColor, bgColor) = when (state) {
        PackState.DOWNLOADING -> Triple(CyanPrimary, CyanPrimary, CyanPrimary.copy(alpha = 0.12f))
        PackState.READY -> Triple(StatusGreen, StatusGreen, StatusGreen.copy(alpha = 0.12f))
        PackState.CHECKING -> Triple(StatusAmber, StatusAmber, StatusAmber.copy(alpha = 0.12f))
        PackState.STOPPED -> Triple(StatusAmber, StatusAmber, StatusAmber.copy(alpha = 0.12f))
        PackState.METADATA -> Triple(IndigoSecondary, IndigoSecondary, IndigoSecondary.copy(alpha = 0.12f))
        PackState.COMPLETE -> Triple(StatusGreen, StatusGreen, StatusGreen.copy(alpha = 0.15f))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = state.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun EpisodeStatusBadge(
    status: EpisodeStatus,
    modifier: Modifier = Modifier
) {
    val (textColor, bgColor) = when (status) {
        EpisodeStatus.COMPLETED -> Pair(StatusGreen, StatusGreen.copy(alpha = 0.12f))
        EpisodeStatus.DOWNLOADING -> Pair(CyanPrimary, CyanPrimary.copy(alpha = 0.12f))
        EpisodeStatus.WAITING_LIMIT -> Pair(StatusAmber, StatusAmber.copy(alpha = 0.12f))
        EpisodeStatus.PLAYED -> Pair(IndigoSecondary, IndigoSecondary.copy(alpha = 0.12f))
        EpisodeStatus.SKIPPED -> Pair(Color.Gray, Color.Gray.copy(alpha = 0.12f))
        EpisodeStatus.REMOVED -> Pair(StatusRose, StatusRose.copy(alpha = 0.12f))
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
            fontWeight = FontWeight.Medium,
            maxLines = 1
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
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = tracker,
            color = CyanPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
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
            .background(IndigoSecondary.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = quality,
            color = IndigoSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
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
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
