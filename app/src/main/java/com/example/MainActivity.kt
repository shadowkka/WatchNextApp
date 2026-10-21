package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.AppSettings
import com.example.data.ServerPreferences
import com.example.data.WatchNextRepository
import com.example.ui.WatchNextApp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferences: ServerPreferences
    private lateinit var repository: WatchNextRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = ServerPreferences(applicationContext)
        repository = WatchNextRepository(preferences, lifecycleScope)

        val incomingMagnet = extractMagnetFromIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val settings by preferences.settingsFlow.collectAsState(initial = AppSettings())
                    val coroutineScope = rememberCoroutineScope()

                    WatchNextApp(
                        repository = repository,
                        currentSettings = settings,
                        initialMagnet = incomingMagnet,
                        onSaveSettings = { updated ->
                            coroutineScope.launch {
                                preferences.updateSettings(updated)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val magnet = extractMagnetFromIntent(intent)
        if (magnet != null) {
            lifecycleScope.launch {
                repository.addMagnet(
                    magnet = magnet,
                    customTitle = "",
                    kind = com.example.model.PackKind.SERIES,
                    season = 1,
                    startEpisode = 1
                )
            }
        }
    }

    private fun extractMagnetFromIntent(intent: Intent?): String? {
        val data = intent?.dataString ?: return null
        return if (data.startsWith("magnet:", ignoreCase = true)) data else null
    }
}
