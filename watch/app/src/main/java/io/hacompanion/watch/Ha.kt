package io.hacompanion.watch

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class Ent(val id: String, val state: String, val attrs: JSONObject) {
    val domain get() = id.substringBefore('.')
    val name: String get() = attrs.optString("friendly_name").ifEmpty { id.substringAfter('.') }
    val unit: String get() = attrs.optString("unit_of_measurement")
    val on get() = state in ON_STATES

    companion object {
        val ON_STATES = setOf("on", "open", "unlocked", "playing", "heat", "cool")
    }
}

/** One WebSocket to HA while the app is visible, subscribed only to the entities the watch shows. */
/**
 * [urls]: home address first, then the remote address (e.g. a tunnel) for when the watch is out. A failed connection
 * moves to the next one; reconnects keep alternating, so it lands back on the home address at home.
 */
class Ha(
    private val urls: List<String>,
    private val token: String,
    startIndex: Int = 0,
    /** Called with the address index that just authenticated, so the next start tries it first (Bluetooth is slow). */
    private val onWorking: (Int) -> Unit = {},
) {
    private var urlIndex = startIndex.coerceIn(0, urls.lastIndex)
    val url get() = urls[urlIndex]
    private val http = OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build()
    private val main = Handler(Looper.getMainLooper())
    private var ws: WebSocket? = null
    private var nextId = 1
    private val pending = HashMap<Int, (JSONObject) -> Unit>()
    private var subId = -1
    private var wanted = false
    private var authFailed = false

    val states = mutableStateMapOf<String, Ent>()
    var status by mutableStateOf("Conectando…")
        private set
    var dash by mutableStateOf<Dash?>(null)
        private set

    /** Entity waiting for ✓/✗; the app draws the prompt over whatever screen is showing. */
    var confirm by mutableStateOf<String?>(null)

    /** ALARMA prompt (from the tile or the Alarma tile in the app) and its progress/result text. */
    var alarmPrompt by mutableStateOf(false)
    var alarmBusy by mutableStateOf(false)
        private set
    var alarmStatus by mutableStateOf<String?>(null)

    private val slow by lazy { http.newBuilder().readTimeout(130, TimeUnit.SECONDS).callTimeout(140, TimeUnit.SECONDS).build() }
    private var lastError: String? = null

    /** REST call on the address currently in use; the arm service blocks until the panel answers (tens of seconds). */
    private suspend fun rest(path: String, body: JSONObject? = null): JSONObject? = withContext(Dispatchers.IO) {
        runCatching {
            val b = Request.Builder().url(url + path).header("Authorization", "Bearer $token")
            if (body != null) b.post(body.toString().toRequestBody("application/json".toMediaType()))
            slow.newCall(b.build()).execute().use { r ->
                val text = r.body?.string().orEmpty()
                if (!r.isSuccessful) throw IOException("HTTP ${r.code} ${text.take(100)}")
                if (text.trimStart().startsWith("{")) JSONObject(text) else JSONObject()
            }
        }.getOrElse { lastError = it.message; null }
    }

    /** Waits out the transient "arming" state. */
    private suspend fun alarmState(): JSONObject? {
        repeat(10) {
            val s = rest("/api/states/$ALARM") ?: return null
            if (s.optString("state") != "arming") return s
            delay(3000)
        }
        return rest("/api/states/$ALARM")
    }

    /**
     * Full arm (alarm_arm_away = "total"). If the panel rejects it because zones are open, force-arm (Verisure OWA's
     * verisure_owa.force_arm), which arms total skipping those zones; the zones come from the rejected attempt's attributes.
     */
    suspend fun armAlarm() {
        alarmBusy = true
        alarmStatus = "Armando total…"
        try {
            val s0 = alarmState()?.optString("state") ?: run { alarmStatus = "Sin conexión con HA"; return }
            if (s0.startsWith("armed")) { alarmStatus = "Ya está armada"; return }
            rest("/api/services/alarm_control_panel/alarm_arm_away", JSONObject().put("entity_id", ALARM))
                ?: run { alarmStatus = "Error: $lastError"; return }
            val after = alarmState() ?: run { alarmStatus = "Sin conexión con HA"; return }
            val attrs = after.optJSONObject("attributes") ?: JSONObject()
            when {
                after.optString("state") == "armed_away" -> alarmStatus = "Alarma TOTAL armada"
                attrs.optBoolean("arm_exception_active") -> {
                    val zones = attrs.optJSONArray("arm_exceptions")?.let { a -> (0 until a.length()).map(a::optString) }.orEmpty().joinToString(", ")
                    if (!attrs.optBoolean("force_arm_available")) { alarmStatus = "No se puede forzar.\nAbiertas: $zones"; return }
                    alarmStatus = "Abiertas: $zones\nArmando parcial…"
                    rest("/api/services/verisure_owa/force_arm", JSONObject().put("entity_id", ALARM))
                        ?: run { alarmStatus = "Error: $lastError"; return }
                    val fin = alarmState()?.optString("state")
                    alarmStatus = if (fin == "armed_away") "Alarma PARCIAL armada\nSin: $zones" else "Estado: ${fin ?: "?"}"
                }
                else -> alarmStatus = "Estado: ${after.optString("state")}"
            }
        } finally {
            alarmBusy = false
        }
    }

    fun connect() {
        wanted = true
        if (ws != null || authFailed) return
        val req = Request.Builder().url(wsUrl(url)).build()
        ws = http.newWebSocket(req, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val m = JSONObject(text)
                main.post { if (webSocket === ws) onMsg(m) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                main.post { dropped(webSocket, "Sin conexión") }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                main.post { dropped(webSocket, "Desconectado") }
            }
        })
    }

    fun close() {
        wanted = false
        ws?.close(1000, null)
        ws = null
    }

    private fun dropped(sock: WebSocket, why: String) {
        if (sock !== ws) return
        ws = null
        pending.clear()
        if (authFailed) return
        status = why
        // try the other address right away; once both have failed, back off
        urlIndex = (urlIndex + 1) % urls.size
        val delay = if (urlIndex == 0) 5000L else 500L
        if (wanted) main.postDelayed({ if (wanted) connect() }, delay)
    }

    private fun send(o: JSONObject, cb: ((JSONObject) -> Unit)? = null): Int {
        val id = nextId++
        o.put("id", id)
        cb?.let { pending[id] = it }
        ws?.send(o.toString())
        return id
    }

    private fun onMsg(m: JSONObject) {
        when (m.optString("type")) {
            "auth_required" -> ws?.send(JSONObject().put("type", "auth").put("access_token", token).toString())
            // never retry a rejected token: repeated bad logins get the watch's IP banned by http.ip_ban
            "auth_invalid" -> {
                authFailed = true
                status = "Token inválido"
                ws?.close(1000, null)
            }
            "auth_ok" -> {
                status = ""
                onWorking(urlIndex)
                send(JSONObject().put("type", "lovelace/config").put("url_path", DASHBOARD)) { r ->
                    r.optJSONObject("result")?.let { dash = Dash.parse(it) } ?: run { status = "Sin panel" }
                    subscribe()
                }
            }
            "result" -> pending.remove(m.optInt("id"))?.invoke(m)
            "event" -> if (m.optInt("id") == subId) apply(m.getJSONObject("event"))
        }
    }

    private fun subscribe() {
        val ids = (dash?.all.orEmpty() + SERVERS.flatMap { it.ids } + EXTRA).toMutableSet()
        ids += ids.filter { it.startsWith("camera.") }.map(::captureButton)
        subId = send(JSONObject().put("type", "subscribe_entities").put("entity_ids", JSONArray(ids.toList())))
    }

    private fun apply(e: JSONObject) {
        e.optJSONObject("a")?.let { a ->
            for (k in a.keys()) {
                val o = a.getJSONObject(k)
                states[k] = Ent(k, o.optString("s"), o.optJSONObject("a") ?: JSONObject())
            }
        }
        e.optJSONObject("c")?.let { c ->
            for (k in c.keys()) {
                val old = states[k] ?: continue
                states[k] = merge(old, c.getJSONObject(k))
            }
        }
        e.optJSONArray("r")?.let { r -> for (i in 0 until r.length()) states.remove(r.getString(i)) }
    }

    fun call(domain: String, service: String, id: String) {
        send(
            JSONObject().put("type", "call_service").put("domain", domain).put("service", service)
                .put("target", JSONObject().put("entity_id", id))
        )
    }

    suspend fun camera(id: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder().url("$url/api/camera_proxy/$id")
                .header("Authorization", "Bearer $token").build()
            http.newCall(req).execute().use { if (it.isSuccessful) BitmapFactory.decodeStream(it.body!!.byteStream()) else null }
        }.getOrNull()
    }

    private val images = HashMap<String, Bitmap?>()

    /** Card logo (app icons from the CDN, or /local/... on HA). SVGs aren't decodable here, so they fall back to the icon. */
    suspend fun image(src: String): Bitmap? {
        if (".svg" in src) return null
        val full = if (src.startsWith("/")) url + src else src
        images[full]?.let { return it }
        val bmp = withContext(Dispatchers.IO) {
            runCatching {
                val req = Request.Builder().url(full).apply { if (full.startsWith(url)) header("Authorization", "Bearer $token") }.build()
                http.newCall(req).execute().use { if (it.isSuccessful) BitmapFactory.decodeStream(it.body!!.byteStream()) else null }
            }.getOrNull()
        }
        images[full] = bmp
        return bmp
    }

    companion object {
        val DASHBOARD get() = Config.dashboard

        fun captureButton(camera: String) = "button." + camera.substringAfter('.') + "_capture"

        /** http → ws, https → wss. */
        fun wsUrl(base: String) = base.trimEnd('/').replaceFirst("http", "ws") + "/api/websocket"

        /** Applies a subscribe_entities "c" diff: {"+": {s, a}, "-": {a: [keys]}}. */
        fun merge(old: Ent, d: JSONObject): Ent {
            val attrs = JSONObject(old.attrs.toString())
            val plus = d.optJSONObject("+")
            plus?.optJSONObject("a")?.let { p -> for (k in p.keys()) attrs.put(k, p.get(k)) }
            d.optJSONObject("-")?.optJSONArray("a")?.let { r -> for (i in 0 until r.length()) attrs.remove(r.getString(i)) }
            val s = plus?.optString("s").orEmpty()
            return Ent(old.id, s.ifEmpty { old.state }, attrs)
        }
    }
}
