package com.example.util

import com.example.model.PackKind
import java.util.regex.Pattern

object TorrentParser {

    private val MAGNET_BTIH_REGEX = Pattern.compile("urn:btih:([0-9a-fA-F]{40}|[2-7a-zA-Z]{32})", Pattern.CASE_INSENSITIVE)
    private val MAGNET_NAME_REGEX = Pattern.compile("[?&]dn=([^&]+)", Pattern.CASE_INSENSITIVE)

    private val QUALITY_REGEX = Pattern.compile("\\b(2160p|4k|1080p|1080i|720p|576p|480p)\\b", Pattern.CASE_INSENSITIVE)
    private val RELEASE_TYPE_REGEX = Pattern.compile("\\b(web-dl|web-dlrip|bluray|bdrip|hdrip|hdtv|tvrip|dvdrip)\\b", Pattern.CASE_INSENSITIVE)

    private val VOICE_STUDIOS = listOf(
        "LostFilm", "HDRezka", "Кубик в кубе", "NewStudio", "AlexFilm",
        "Red Head Sound", "RuDub", "TVShows", "Jaskier", "BaibaKo",
        "Пифагор", "Невафильм", "Дубляж", "MVO", "DVO", "AVO", "VO",
        "Субтитры", "Оригинал"
    )

    private val SEASON_EPISODE_PATTERNS = listOf(
        Pattern.compile("[Ss]([0-9]{1,2})[Ee]([0-9]{1,2})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("сезон\\s*([0-9]{1,2})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("серии?\\s*([0-9]{1,3})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("([0-9]{1,2})x([0-9]{1,2})", Pattern.CASE_INSENSITIVE)
    )

    fun extractMagnetHash(magnet: String): String? {
        val matcher = MAGNET_BTIH_REGEX.matcher(magnet)
        return if (matcher.find()) matcher.group(1)?.lowercase() else null
    }

    fun extractDisplayName(magnet: String): String? {
        val matcher = MAGNET_NAME_REGEX.matcher(magnet)
        return if (matcher.find()) {
            try {
                java.net.URLDecoder.decode(matcher.group(1) ?: "", "UTF-8")
            } catch (e: Exception) {
                matcher.group(1)
            }
        } else null
    }

    fun extractQuality(title: String): String {
        val qMatcher = QUALITY_REGEX.matcher(title)
        val qual = if (qMatcher.find()) qMatcher.group(1)?.uppercase() else "1080P"
        val rMatcher = RELEASE_TYPE_REGEX.matcher(title)
        val release = if (rMatcher.find()) rMatcher.group(1)?.uppercase() else "WEB-DL"
        return "$qual · $release"
    }

    fun extractVoices(title: String): List<String> {
        val found = mutableListOf<String>()
        val lower = title.lowercase()
        for (v in VOICE_STUDIOS) {
            if (lower.contains(v.lowercase())) {
                found.add(v)
            }
        }
        return if (found.isNotEmpty()) found else listOf("Многоголосый")
    }

    fun detectKind(title: String): PackKind {
        val lower = title.lowercase()
        val isSeries = lower.contains("сезон") ||
                lower.contains("серии") ||
                lower.contains("серия") ||
                Pattern.compile("[Ss][0-9]{1,2}").matcher(title).find() ||
                Pattern.compile("[0-9]{1,2}\\s*сезон").matcher(lower).find()
        return if (isSeries) PackKind.SERIES else PackKind.MOVIE
    }

    fun detectSeason(title: String): Int {
        val lower = title.lowercase()
        val matcher1 = Pattern.compile("(?:сезон|s)\\s*([0-9]{1,2})").matcher(lower)
        if (matcher1.find()) {
            return matcher1.group(1)?.toIntOrNull() ?: 1
        }
        val matcher2 = Pattern.compile("([0-9]{1,2})\\s*сезон").matcher(lower)
        if (matcher2.find()) {
            return matcher2.group(1)?.toIntOrNull() ?: 1
        }
        return 1
    }

    fun detectStartEpisode(title: String): Int {
        val lower = title.lowercase()
        val matcher1 = Pattern.compile("(?:сери[яи]|e|ep)\\s*([0-9]{1,3})").matcher(lower)
        if (matcher1.find()) {
            return matcher1.group(1)?.toIntOrNull() ?: 1
        }
        val matcher2 = Pattern.compile("([0-9]{1,3})\\s*сери[яи]").matcher(lower)
        if (matcher2.find()) {
            return matcher2.group(1)?.toIntOrNull() ?: 1
        }
        return 1
    }
}
