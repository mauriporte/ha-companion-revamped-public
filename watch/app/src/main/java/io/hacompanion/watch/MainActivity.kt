package io.hacompanion.watch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

class MainActivity : ComponentActivity() {
    private var ha: Ha? = null

    /** Screen to jump to when opened from the Favoritos tile; cleared once shown. */
    private var route by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Config.load(this)) {
            setContent { NoConfig() }
            return
        }
        // token/url are pushed by set-token.ps1 via `run-as`, never typed on the watch
        val token = File(filesDir, "token").takeIf { it.exists() }?.readText()?.trim().orEmpty()
        val home = File(filesDir, "url").takeIf { it.exists() }?.readText()?.trim()
        val last = File(filesDir, LAST_URL)
        ha = if (token.isEmpty()) null else Ha(
            Config.urls(home), token,
            startIndex = last.takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull() ?: 0,
            onWorking = { last.writeText(it.toString()) },
        )
        route = intent.getStringExtra(ROUTE)
        setContent { ha?.let { App(it, route) { route = null } } ?: NoToken() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        route = intent.getStringExtra(ROUTE)
    }

    override fun onStart() {
        super.onStart()
        ha?.connect()
    }

    override fun onStop() {
        ha?.close()
        super.onStop()
    }

    companion object {
        const val ROUTE = "route"

        /** Index (0 = home, 1 = tunnel) of the address that worked last; shared with the tile. */
        const val LAST_URL = "last_url"
    }
}
