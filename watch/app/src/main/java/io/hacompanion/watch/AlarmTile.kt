package io.hacompanion.watch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.ListenableFuture
import org.json.JSONObject
import java.util.concurrent.Executors

/** Alarm only: a big shield (red when armed) with the state under it; a tap opens the ALARMA prompt (✗ / ✓). */
class AlarmTile : TileService() {
    private val pool = Executors.newSingleThreadExecutor()

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> =
        CallbackToFutureAdapter.getFuture { done ->
            pool.execute {
                Config.load(this)
                val state = if (ALARM.isBlank()) null else TileApi.from(this)?.state(ALARM)
                done.set(tile(state))
            }
            "alarm-tile"
        }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
        CallbackToFutureAdapter.getFuture { done ->
            pool.execute {
                done.set(
                    ResourceBuilders.Resources.Builder().setVersion(RES_VERSION)
                        .addIdToImageMapping("on", RingTile.png(RingTile.button(Icons.Filled.Shield, Bad.copy(alpha = 0.35f).compositeOver(Color.Black), Bad, SHIELD_PX)))
                        .addIdToImageMapping("off", RingTile.png(RingTile.button(Icons.Filled.Shield, CardBg, Ok, SHIELD_PX)))
                        .addIdToImageMapping("na", RingTile.png(RingTile.button(Icons.Filled.Shield, CardBg, Color(0xFF4A4A4A), SHIELD_PX)))
                        .build()
                )
            }
            "alarm-tile-res"
        }

    override fun onDestroy() {
        pool.shutdown()
        super.onDestroy()
    }

    private fun tile(state: String?): TileBuilders.Tile {
        val variant = when (state) {
            null, "unavailable", "unknown" -> "na"
            "disarmed" -> "off"
            else -> "on" // armed / arming / triggered
        }
        val open = ModifiersBuilders.Clickable.Builder().setId("alarm").setOnClick(
            ActionBuilders.LaunchAction.Builder().setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder().setPackageName(packageName).setClassName(MainActivity::class.java.name)
                    .addKeyToExtraMapping(MainActivity.ROUTE, ActionBuilders.AndroidStringExtra.Builder().setValue("alarm").build())
                    .build()
            ).build()
        ).build()
        val text = if (state == null) "Sin conexión" else stateText(Ent(ALARM, state, JSONObject()))
        val column = LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setModifiers(ModifiersBuilders.Modifiers.Builder().setClickable(open).build())
            .addContent(LayoutElementBuilders.Image.Builder().setResourceId(variant).setWidth(dp(SHIELD_DP)).setHeight(dp(SHIELD_DP)).build())
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(8f)).build())
            .addContent(
                LayoutElementBuilders.Text.Builder().setText(text).setFontStyle(
                    LayoutElementBuilders.FontStyle.Builder().setSize(sp(16f))
                        .setColor(argb((if (variant == "on") Bad else Muted).toArgb())).build()
                ).build()
            )
            .build()
        val root = LayoutElementBuilders.Box.Builder().setWidth(expand()).setHeight(expand()).addContent(column).build()
        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RES_VERSION)
            .setFreshnessIntervalMillis(5 * 60_000L)
            .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(root))
            .build()
    }

    companion object {
        private const val RES_VERSION = "alarm-1"
        private const val SHIELD_DP = 110f
        private const val SHIELD_PX = 240
    }
}
