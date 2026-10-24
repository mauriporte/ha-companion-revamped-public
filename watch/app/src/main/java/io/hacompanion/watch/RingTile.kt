package io.hacompanion.watch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.degrees
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** One button on the tile ring: entity, its dashboard icon and color. [route]: open the app there instead of toggling. */
class RingButton(
    val id: String, val icon: String, val color: String, val route: String? = null,
    /** On-state drawn as a solid disc in [color] with the icon in dark grey (instead of a tinted disc). */
    val solidOn: Boolean = false,
)

// the 8 buttons come from config.json ("ring"); keep kill switches and server plugs out: a tile can't ask ✓/✗
val RING get() = Config.ring

/**
 * Round tile: ★ in the center opens Favoritos; 8 toggles around it, one every 45°, lit when on.
 * Taps on the ring call HA from here (home address, then the tunnel) and redraw with fresh states.
 */
class RingTile : TileService() {
    private val pool = Executors.newSingleThreadExecutor()

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> =
        CallbackToFutureAdapter.getFuture { done ->
            pool.execute {
                Config.load(this)
                val api = TileApi.from(this)
                val clicked = requestParams.currentState.lastClickableId
                if (api != null && clicked.startsWith("t:")) {
                    RING.getOrNull(clicked.removePrefix("t:").toIntOrNull() ?: -1)?.let {
                        api.act(it.id)
                        Thread.sleep(700) // let HA report the new state before redrawing
                    }
                }
                done.set(tile(RING.map { api?.state(it.id) }))
            }
            "ring-tile"
        }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
        CallbackToFutureAdapter.getFuture { done ->
            pool.execute {
                Config.load(this)
                val res = ResourceBuilders.Resources.Builder().setVersion(RES_VERSION)
                RING.forEachIndexed { i, b ->
                    val icon = mdi(b.icon) ?: iconOf(b.id)
                    val c = haColor(b.color) ?: hexColor(b.color) ?: HaBlue
                    val on = if (b.solidOn) button(icon, c, Color(0xFF424242), BUTTON_PX)
                    else button(icon, c.copy(alpha = 0.35f).compositeOver(Color.Black), c, BUTTON_PX)
                    res.addIdToImageMapping("b${i}_on", png(on))
                    res.addIdToImageMapping("b${i}_off", png(button(icon, CardBg, Muted, BUTTON_PX)))
                    res.addIdToImageMapping("b${i}_na", png(button(icon, CardBg, Color(0xFF4A4A4A), BUTTON_PX)))
                }
                // optional personal logo (res/drawable-nodpi/m_logo.*, kept out of git); ★ otherwise
                val logoId = resources.getIdentifier("m_logo", "drawable", packageName)
                val center = if (logoId != 0) roundLogo(BitmapFactory.decodeResource(resources, logoId), CENTER_PX)
                else button(Icons.Filled.Star, CardBg, Amber, CENTER_PX)
                res.addIdToImageMapping("center", png(center))
                done.set(res.build())
            }
            "ring-tile-res"
        }

    override fun onDestroy() {
        pool.shutdown()
        super.onDestroy()
    }

    /** Opens the app on [route] ("fav", "alarm"). */
    private fun open(route: String, id: String) = ModifiersBuilders.Clickable.Builder().setId(id).setOnClick(
        ActionBuilders.LaunchAction.Builder().setAndroidActivity(
            ActionBuilders.AndroidActivity.Builder()
                .setPackageName(packageName)
                .setClassName(MainActivity::class.java.name)
                .addKeyToExtraMapping(MainActivity.ROUTE, ActionBuilders.AndroidStringExtra.Builder().setValue(route).build())
                .build()
        ).build()
    ).build()

    private fun tile(states: List<String?>): TileBuilders.Tile {
        val root = LayoutElementBuilders.Box.Builder().setWidth(expand()).setHeight(expand())
        RING.forEachIndexed { i, b ->
            val s = states[i]
            val variant = when {
                s == null || s == "unavailable" || s == "unknown" -> "na"
                b.id.startsWith("scene.") || s == "on" -> "on" // scenes are stateless: always shown lit
                b.id.startsWith("alarm_control_panel.") && s != "disarmed" -> "on" // armed / arming / triggered: red
                else -> "off"
            }
            val click = b.route?.let { open(it, "r:$i") }
                ?: ModifiersBuilders.Clickable.Builder().setId("t:$i").setOnClick(ActionBuilders.LoadAction.Builder().build()).build()
            val button = LayoutElementBuilders.Image.Builder()
                .setResourceId("b${i}_$variant").setWidth(dp(BUTTON_DP)).setHeight(dp(BUTTON_DP))
                .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(click).build())
                .build()
            val arc = LayoutElementBuilders.Arc.Builder()
                .setAnchorAngle(degrees(i * 45f))
                .setAnchorType(LayoutElementBuilders.ARC_ANCHOR_CENTER)
                .addContent(LayoutElementBuilders.ArcAdapter.Builder().setContent(button).setRotateContents(false).build())
                .build()
            // inset the ring from the bezel
            root.addContent(
                LayoutElementBuilders.Box.Builder().setWidth(expand()).setHeight(expand())
                    .setModifiers(ModifiersBuilders.Modifiers.Builder().setPadding(ModifiersBuilders.Padding.Builder().setAll(dp(RING_INSET_DP)).build()).build())
                    .addContent(arc).build()
            )
        }
        root.addContent(
            LayoutElementBuilders.Image.Builder().setResourceId("center").setWidth(dp(CENTER_DP)).setHeight(dp(CENTER_DP))
                .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(open("fav", "fav")).build()).build()
        )
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RES_VERSION)
            .setFreshnessIntervalMillis(5 * 60_000L)
            .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(root.build()))
            .build()
    }

    companion object {
        private const val RES_VERSION = "ring-10" // bump when icons/colors change so the watch re-fetches them
        private const val BUTTON_DP = 44f
        private const val CENTER_DP = 70f
        private const val RING_INSET_DP = 8f
        private const val BUTTON_PX = 96
        private const val CENTER_PX = 160

        /** A filled circle with the icon centered, as PNG (the tile renderer decodes it). */
        fun button(icon: ImageVector, bg: Color, fg: Color, px: Int): Bitmap {
            val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg.toArgb() }
            c.drawCircle(px / 2f, px / 2f, px / 2f, p)
            val iconPx = px * 0.52f
            c.translate((px - iconPx) / 2, (px - iconPx) / 2)
            c.scale(iconPx / icon.viewportWidth, iconPx / icon.viewportHeight)
            p.color = fg.toArgb()
            draw(icon.root, c, p)
            return bmp
        }

        /** "#RRGGBB" → Color, for ring colors that aren't HA theme names. */
        fun hexColor(s: String): Color? = s.takeIf { it.startsWith("#") && it.length == 7 }
            ?.substring(1)?.toLongOrNull(16)?.let { Color(0xFF000000 or it) }

        /** The M profile picture (from HA's main user) cropped to a circle with a thin dark ring, like the other buttons. */
        fun roundLogo(src: Bitmap, px: Int): Bitmap {
            val scaled = Bitmap.createScaledBitmap(src, px, px, true)
            val out = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val c = Canvas(out)
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
            c.drawCircle(px / 2f, px / 2f, px / 2f, p)
            val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = px * 0.03f; color = CardBg.toArgb() }
            c.drawCircle(px / 2f, px / 2f, px / 2f - ring.strokeWidth / 2, ring)
            return out
        }

        private fun draw(g: VectorGroup, c: Canvas, p: Paint) {
            for (n in g) when (n) {
                is VectorPath -> {
                    val path = PathParser().addPathNodes(n.pathData).toPath().asAndroidPath()
                    path.fillType = if (n.pathFillType == PathFillType.EvenOdd) android.graphics.Path.FillType.EVEN_ODD else android.graphics.Path.FillType.WINDING
                    c.drawPath(path, p)
                }
                is VectorGroup -> draw(n, c, p)
            }
        }

        private fun png(b: Bitmap): ResourceBuilders.ImageResource {
            val bytes = ByteArrayOutputStream().also { b.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
            return ResourceBuilders.ImageResource.Builder().setInlineResource(
                ResourceBuilders.InlineImageResource.Builder()
                    .setData(bytes).setWidthPx(b.width).setHeightPx(b.height)
                    .setFormat(ResourceBuilders.IMAGE_FORMAT_UNDEFINED) // undefined = encoded image, decoded by the renderer
                    .build()
            ).build()
        }
    }
}

/** Minimal REST client for the tile: same token and addresses as the app, home first then the tunnel. */
class TileApi(private val urls: List<String>, private val token: String, private val last: File) {
    // Bluetooth through the phone is slow: give HA time to answer
    private val http = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build()
    private var working = last.takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull()?.coerceIn(0, urls.lastIndex) ?: 0

    fun state(id: String): String? = request("/api/states/$id")?.let { JSONObject(it).optString("state") }

    fun act(id: String) {
        val domain = id.substringBefore('.')
        val service = when (domain) {
            "scene", "script" -> "turn_on"
            "button", "input_button" -> "press"
            else -> "toggle"
        }
        request("/api/services/$domain/$service", JSONObject().put("entity_id", id).toString())
    }

    private fun request(path: String, body: String? = null): String? {
        for (i in urls.indices) {
            val base = urls[(working + i) % urls.size]
            val ok = runCatching {
                val b = Request.Builder().url(base + path).header("Authorization", "Bearer $token")
                if (body != null) b.post(body.toRequestBody("application/json".toMediaType()))
                http.newCall(b.build()).execute().use { if (it.isSuccessful) it.body?.string() else null }
            }.getOrNull()
            if (ok != null) {
                val idx = (working + i) % urls.size
                if (idx != working) runCatching { last.writeText(idx.toString()) }
                working = idx
                return ok
            }
        }
        return null
    }

    companion object {
        fun from(ctx: Context): TileApi? {
            val token = File(ctx.filesDir, "token").takeIf { it.exists() }?.readText()?.trim().orEmpty()
            if (token.isEmpty()) return null
            val home = File(ctx.filesDir, "url").takeIf { it.exists() }?.readText()?.trim()
            return TileApi(Config.urls(home), token, File(ctx.filesDir, MainActivity.LAST_URL))
        }
    }
}
