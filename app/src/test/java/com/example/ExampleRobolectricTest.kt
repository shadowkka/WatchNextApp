package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PackKind
import com.example.util.TorrentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WatchNext", appName)
    }

    @Test
    fun `extract magnet hash and display name`() {
        val magnet = "magnet:?xt=urn:btih:38bdf8818cf84f46e50284c7161b220d1117abcd&dn=House.of.the.Dragon.S02.1080p.LostFilm"
        val hash = TorrentParser.extractMagnetHash(magnet)
        val name = TorrentParser.extractDisplayName(magnet)

        assertEquals("38bdf8818cf84f46e50284c7161b220d1117abcd", hash)
        assertEquals("House.of.the.Dragon.S02.1080p.LostFilm", name)
    }

    @Test
    fun `detect series kind and season`() {
        val title = "House of the Dragon Сезон 2 (2024) WEB-DL 1080p | LostFilm"
        val kind = TorrentParser.detectKind(title)
        val season = TorrentParser.detectSeason(title)
        val voices = TorrentParser.extractVoices(title)

        assertEquals(PackKind.SERIES, kind)
        assertEquals(2, season)
        assertNotNull(voices.find { it == "LostFilm" })
    }

    @Test
    fun `app settings default poll interval is 3 seconds`() {
        val settings = com.example.data.AppSettings()
        assertEquals(3, settings.pollIntervalSeconds)
        assertEquals("http://192.168.2.11:8000", settings.serverUrl)
    }

    @Test
    fun `show search result model holds tvmaze show info`() {
        val show = com.example.model.ShowSearchResult(
            id = 44458,
            name = "Severance",
            premiered = "2022-02-18",
            status = "Running",
            language = "English"
        )
        assertEquals(44458, show.id)
        assertEquals("Severance", show.name)
        assertEquals("2022-02-18", show.premiered)
    }
}
