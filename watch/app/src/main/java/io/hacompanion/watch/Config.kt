package io.hacompanion.watch

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Everything specific to one home, read from assets/config.json (not in the public repo; see config.example.json):
 * addresses, dashboard, alarm/weather/away entities, server gauges and the tile's 8 buttons.
 */
object Config {
    var homeUrl = ""; private set
    var externalUrl = ""; private set
    var dashboard = ""; private set
    var alarm = ""; private set
    var weather = ""; private set
    var away = ""; private set
    var servers: List<Server> = emptyList(); private set
    var ring: List<RingButton> = emptyList(); private set
    var loaded = false; private set

    /** False when assets/config.json is missing or broken; the app then explains instead of crashing. */
    fun load(ctx: Context): Boolean {
        if (loaded) return true
        val j = runCatching { JSONObject(ctx.assets.open("config.json").bufferedReader().use { it.readText() }) }.getOrNull() ?: return false
        apply(j)
        return true
    }

    fun apply(j: JSONObject) {
        homeUrl = j.optString("homeUrl")
        externalUrl = j.optString("externalUrl")
        dashboard = j.optString("dashboard", "lovelace")
        alarm = j.optString("alarm")
        weather = j.optString("weather")
        away = j.optString("away")
        servers = j.optJSONArray("servers").objects().map {
            // optString turns JSON null into the text "null", which HA rejects as an entity id (and drops the whole subscription)
            Server(it.optString("name"), it.optString("cpu"), it.optString("ram"), it.optString("disk"), it.str("temp"))
        }
        ring = j.optJSONArray("ring").objects().map {
            RingButton(it.optString("id"), it.optString("icon"), it.optString("color"), it.str("route"), it.optBoolean("solidOn"))
        }
        loaded = true
    }

    /** Home address first (an override in files/url wins), then the remote one; blanks dropped. */
    fun urls(override: String?) = listOf(override ?: homeUrl, externalUrl).filter { it.isNotBlank() }

    /** Value or null for missing / JSON null / blank. */
    private fun JSONObject.str(k: String): String? = if (isNull(k)) null else optString(k).ifBlank { null }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
}
