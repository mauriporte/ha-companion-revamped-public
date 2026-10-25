package io.hacompanion.watch

import android.content.Context
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import java.io.File
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.wear.compose.material.CompactChip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.net.Uri
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Mic
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.wear.input.RemoteInputIntentHelper
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.app.RemoteInput
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.SwipeToDismissBoxState
import androidx.wear.compose.foundation.edgeSwipeToDismiss
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavHostState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// HA dark theme palette
val HaBlue = Color(0xFF03A9F4)
val Amber = Color(0xFFFFC107)
val Violet = Color(0xFFAB47BC)
val CardBg = Color(0xFF1C1C1E)
val Muted = Color(0xFF9E9E9E)
val Ok = Color(0xFF4CAF50)
val Bad = Color(0xFFF44336)

private val ALARM_TEXT = mapOf(
    "disarmed" to "Desarmada", "armed_away" to "Armada (ausente)", "armed_home" to "Armada (casa)",
    "armed_night" to "Armada (noche)", "arming" to "Armando…", "pending" to "Pendiente", "triggered" to "¡Disparada!"
)
private val WEATHER_TEXT = mapOf(
    "sunny" to "Soleado", "clear-night" to "Despejado", "cloudy" to "Nublado", "partlycloudy" to "Parcialmente nublado",
    "rainy" to "Lluvia", "pouring" to "Lluvia fuerte", "fog" to "Niebla", "windy" to "Viento", "snowy" to "Nieve",
    "lightning" to "Tormenta", "lightning-rainy" to "Tormenta"
)
private val PRESS_DOMAINS = setOf("scene", "script", "button", "input_button", "camera")

@Composable
fun App(ha: Ha, route: String? = null, routeShown: () -> Unit = {}) {
    MaterialTheme(colors = Colors(primary = HaBlue, onPrimary = Color.Black, surface = CardBg, onSurface = Color.White)) {
        val nav = rememberSwipeDismissableNavController()
        val sdb = rememberSwipeToDismissBoxState()
        Box(Modifier.fillMaxSize()) {
        SwipeDismissableNavHost(nav, startDestination = "home", state = rememberSwipeDismissableNavHostState(sdb)) {
            composable("home") { Home(ha) { nav.navigate(it) } }
            composable("fav") { ha.dash?.let { Pages(ha, it.fav, Amber, sdb) } ?: Loading(ha) }
            composable("list/{kind}") { e ->
                val kind = e.arguments?.getString("kind")
                val d = ha.dash ?: return@composable Loading(ha)
                if (kind == "menus") GroupList(ha, "Menús", d.menus, HaBlue, { Icons.Filled.Dns }) { nav.navigate("group/menus/$it") }
                else GroupList(ha, "Áreas", d.areas, Ok, { Icons.Filled.Home }) {
                    nav.navigate(if (d.areas[it].subs.isEmpty()) "group/areas/$it" else "subs/$it")
                }
            }
            composable("subs/{i}") { e ->
                val area = ha.dash?.areas?.getOrNull(e.arguments?.getString("i")?.toIntOrNull() ?: -1) ?: return@composable Loading(ha)
                GroupList(ha, area.title, area.subs, Ok, { g -> groupIcon(ha, g) }) { j -> nav.navigate("sub/${e.arguments?.getString("i")}/$j") }
            }
            composable("sub/{i}/{j}") { e ->
                val area = ha.dash?.areas?.getOrNull(e.arguments?.getString("i")?.toIntOrNull() ?: -1)
                val g = area?.subs?.getOrNull(e.arguments?.getString("j")?.toIntOrNull() ?: -1) ?: return@composable Loading(ha)
                Pages(ha, g, Ok, sdb)
            }
            composable("group/{kind}/{i}") { e ->
                val d = ha.dash ?: return@composable Loading(ha)
                val menus = e.arguments?.getString("kind") == "menus"
                val g = (if (menus) d.menus else d.areas).getOrNull(e.arguments?.getString("i")?.toIntOrNull() ?: -1)
                    ?: return@composable Loading(ha)
                Pages(ha, g, if (menus) HaBlue else Ok, sdb)
            }
            composable("res") { Summaries(ha, sdb) }
            // every camera on the dashboard, one page each (opened from Joint Launcher's shortcuts tile)
            composable("cams") { ha.dash?.let { d -> Pages(ha, Group("Cámaras", d.all.filter { it.startsWith("camera.") }.distinct()), Violet, sdb) } ?: Loading(ha) }
            composable("chats") { ChatsList(ha) { nav.navigate("chat/$it") } }
            composable("chat/{i}") { e ->
                val chat = ha.beeperChats.getOrNull(e.arguments?.getString("i")?.toIntOrNull() ?: -1) ?: return@composable Loading(ha)
                ChatView(ha, chat)
            }
        }
        ha.confirm?.let { ConfirmOverlay(ha, it) }
        if (ha.alarmPrompt) AlarmOverlay(ha)
        // opened from the Favoritos tile: go there on top of home, so back returns to home
        LaunchedEffect(route) {
            when (route) {
                null -> {}
                "alarm" -> { ha.alarmPrompt = true; routeShown() }
                else -> {
                    nav.popBackStack("home", inclusive = false)
                    nav.navigate(route)
                    routeShown()
                }
            }
        }
        }
    }
}

@Composable
fun Loading(ha: Ha) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(ha.status.ifEmpty { "Cargando…" }, color = Muted)
}

@Composable
fun NoConfig() = Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
    Text("Falta config.json.\nCopia config.example.json a app/src/main/assets/config.json.", color = Muted, fontSize = 12.sp)
}

@Composable
fun NoToken() = Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
    Text("Falta el token de HA.\nEjecuta set-token.ps1 en el PC.", color = Muted, fontSize = 13.sp)
}

val HOME_SECTIONS = listOf("fav", "chats", "menus", "areas", "res")

/** Saved order first (unknown keys dropped), then any section missing from it, so new sections still show. */
fun homeOrder(saved: String?): List<String> {
    val kept = saved.orEmpty().split(',').filter { it in HOME_SECTIONS }.distinct()
    return kept + HOME_SECTIONS.filter { it !in kept }
}

fun moved(order: List<String>, i: Int, delta: Int): List<String> {
    val j = i + delta
    if (i !in order.indices || j !in order.indices) return order
    return order.toMutableList().apply { add(j, removeAt(i)) }
}

/** Home sections; press and hold any of them to reorder with ▲ ▼ (saved on the watch). */
@Composable
fun Home(ha: Ha, go: (String) -> Unit) {
    val d = ha.dash
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val file = remember { File(ctx.filesDir, "home_order") }
    var order by remember { mutableStateOf(homeOrder(file.takeIf { it.exists() }?.readText())) }
    var sorting by remember { mutableStateOf(false) }
    val done: () -> Unit = { sorting = false; runCatching { file.writeText(order.joinToString(",")) } }
    BackHandler(enabled = sorting) { done() }
    val list = rememberScalingLazyListState()
    Scaffold(timeText = { TimeText() }, positionIndicator = { PositionIndicator(list) }) {
        ScalingLazyColumn(Modifier.fillMaxSize(), state = list) {
            item { ListHeader { Text(if (sorting) "Ordenar" else stringResource(R.string.app_name), color = HaBlue) } }
            if (ha.status.isNotEmpty() && !sorting) item { Text(ha.status, color = Muted, fontSize = 12.sp) }
            items(order.size) { i ->
                val key = order[i]
                val (label, icon, tint, sub) = when (key) {
                    "fav" -> Section("Favoritos", Icons.Filled.Star, Amber, d?.let { "${shown(it.fav.ids).size} entidades" })
                    "chats" -> Section("Chats", Icons.AutoMirrored.Filled.Chat, Color(0xFF25D366), "Beeper")
                    "menus" -> Section("Menús", Icons.Filled.Apps, HaBlue, d?.let { "${it.menus.size} servidores" })
                    "areas" -> Section("Áreas", Icons.Filled.Home, Ok, d?.let { "${it.areas.size} áreas" })
                    else -> Section("Resúmenes", Icons.Filled.DonutLarge, Violet, "Casa · ${SERVERS.size} servidores")
                }
                val route = when (key) { "fav" -> "fav"; "chats" -> "chats"; "menus" -> "list/menus"; "areas" -> "list/areas"; else -> "res" }
                if (sorting) SortRow(label, icon, tint, up = { order = moved(order, i, -1) }, down = { order = moved(order, i, 1) })
                else SectionChip(label, icon, tint, sub, onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); sorting = true }) { go(route) }
            }
            if (sorting) item {
                CompactChip(onClick = { done() }, label = { Text("Listo") }, colors = ChipDefaults.chipColors(backgroundColor = HaBlue, contentColor = Color.Black))
            }
        }
    }
}

private data class Section(val label: String, val icon: ImageVector, val tint: Color, val sub: String?)

@Composable
fun SortRow(label: String, icon: ImageVector, tint: Color, up: () -> Unit, down: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).background(CardBg).padding(start = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color.White, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Subir", tint = HaBlue, modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = up).padding(6.dp))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Bajar", tint = HaBlue, modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = down).padding(6.dp))
    }
}

/** Chip look (Wear's Chip has no long press), so the home sections can be held to reorder. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SectionChip(label: String, icon: ImageVector, tint: Color, sub: String?, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).background(CardBg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (sub != null) Text(sub, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun GroupList(ha: Ha, title: String, groups: List<Group>, tint: Color, icon: (Group) -> ImageVector, open: (Int) -> Unit) {
    val list = rememberScalingLazyListState()
    Scaffold(positionIndicator = { PositionIndicator(list) }) {
        ScalingLazyColumn(Modifier.fillMaxSize(), state = list) {
            item { ListHeader { Text(title, color = tint) } }
            itemsIndexed(groups) { i, g ->
                val on = g.ids.count { ha.states[it]?.on == true }
                val sub = if (g.subs.isEmpty()) "${shown(g.ids).size} · $on encendidos" else "${g.subs.size} grupos · $on encendidos"
                SectionChip(g.title, icon(g), tint, sub) { open(i) }
            }
        }
    }
}

sealed interface Page
class TilesPage(val ids: List<String>) : Page
class CameraPage(val id: String) : Page

/** What a section shows: a camera's capture button is dropped when the camera itself is there (tapping its picture captures). */
fun shown(ids: List<String>): List<String> {
    val captures = ids.filter { it.startsWith("camera.") }.map(Ha::captureButton).toSet()
    return ids.filter { it !in captures }
}

/** Three tiles per page fit the round 416px screen; each camera gets a full page. */
fun pagesOf(ids: List<String>): List<Page> {
    // everyday controls, then cameras, then kill switches / server power (confirmation-protected); dashboard order within each
    val (cams, rest) = shown(ids).partition { it.startsWith("camera.") }
    val (power, everyday) = rest.partition(::guarded)
    return everyday.chunked(3).map(::TilesPage) + cams.map(::CameraPage) + power.chunked(3).map(::TilesPage)
}

@Composable
fun Pages(ha: Ha, g: Group, tint: Color, sdb: SwipeToDismissBoxState) {
    val pages = remember(g) { pagesOf(g.ids) }
    Paged(pages.size, tint, sdb) { i ->
        when (val p = pages[i]) {
            is TilesPage -> Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(g.title, color = tint, fontSize = 12.sp, maxLines = 1)
                p.ids.forEach { EntityTile(ha, it, g.guarded || guarded(it)) }
            }
            is CameraPage -> CameraView(ha, p.id)
        }
    }
}

@Composable
fun Paged(count: Int, tint: Color, sdb: SwipeToDismissBoxState, content: @Composable (Int) -> Unit) {
    val state = rememberPagerState { count }
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state,
            Modifier.fillMaxSize()
                .edgeSwipeToDismiss(sdb)
                .onRotaryScrollEvent { ev ->
                    if (!state.isScrollInProgress) {
                        val target = (state.currentPage + if (ev.verticalScrollPixels > 0) 1 else -1).coerceIn(0, count - 1)
                        scope.launch { state.animateScrollToPage(target) }
                    }
                    true
                }
                .focusRequester(focus)
                .focusable(),
        ) { content(it) }
        if (count in 2..12) Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(count) {
                val sel = it == state.currentPage
                Box(Modifier.size(if (sel) 6.dp else 5.dp).clip(CircleShape).background(if (sel) tint else Color(0xFF555555)))
            }
        } else if (count > 12) Text(
            "${state.currentPage + 1}/$count", color = Muted, fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
        )
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
}

/** Kill switches, server power and locks ask for confirmation, so a stray tap can't cut a machine off. */
fun guarded(id: String) = "kill_switch" in id || "smart_switch" in id || id.startsWith("lock.")

/** "¿Apagar Server 1?" — the question the confirmation asks for this entity's next action. */
fun confirmQuestion(e: Ent, name: String): String = when {
    e.domain == "lock" -> if (e.state == "locked") "¿Desbloquear $name?" else "¿Bloquear $name?"
    e.on -> "¿Apagar $name?"
    else -> "¿Encender $name?"
}

@Composable
fun EntityTile(ha: Ha, id: String, guard: Boolean) {
    val e = ha.states[id]
    val card = ha.dash?.cards?.get(id)
    val name = card?.name ?: e?.name ?: id.substringAfter('.')
    val base = colorOf(e)
    val active = e != null && base != Muted
    // the card's own color (red kill switches, purple Modo Película…) when it's on, like HA
    val c = if (active && e?.domain != "alarm_control_panel") haColor(card?.color) ?: base else base
    val haptic = LocalHapticFeedback.current
    val ctx = LocalContext.current
    val fire = { ent: Ent -> haptic.performHapticFeedback(HapticFeedbackType.LongPress); act(ha, ent) }
    Row(
        Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(23.dp))
            .background(if (active) c.copy(alpha = 0.16f) else CardBg)
            .clickable {
                when {
                    e == null -> {}
                    e.domain == "alarm_control_panel" -> ha.alarmPrompt = true
                    guard -> ha.confirm = id
                    else -> fire(e)
                }
            }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TileIcon(ha, id, card, e, c)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stateText(e), color = Muted, fontSize = 11.sp, maxLines = 1)
        }
        if (guard) Icon(Icons.Filled.Lock, contentDescription = "Protegido", tint = Muted, modifier = Modifier.size(13.dp))
    }
}

/**
 * Full-screen ✗ / ✓ prompt drawn over the current screen (a Wear Dialog window closed itself on the watch).
 * ✗ or back does nothing; ✓ acts on the entity's state at that moment.
 */
@Composable
fun ConfirmOverlay(ha: Ha, id: String) {
    val e = ha.states[id] ?: run { ha.confirm = null; return }
    val name = ha.dash?.cards?.get(id)?.name ?: e.name
    val haptic = LocalHapticFeedback.current
    BackHandler { ha.confirm = null }
    Column(
        // swallow touches so nothing underneath can be pressed while asking
        Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) { detectTapGestures { } },
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = Bad, modifier = Modifier.size(30.dp))
        Text(confirmQuestion(e, name), color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(onClick = { ha.confirm = null }, colors = ButtonDefaults.secondaryButtonColors()) {
                Icon(Icons.Filled.Close, contentDescription = "Cancelar")
            }
            Button(
                onClick = { ha.confirm = null; haptic.performHapticFeedback(HapticFeedbackType.LongPress); act(ha, e) },
                colors = ButtonDefaults.primaryButtonColors(backgroundColor = Bad),
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Confirmar")
            }
        }
    }
}

/** Full-screen ALARMA prompt: the red title makes it unmistakable before arming; ✓ arms total (partial if zones are open). */
@Composable
fun AlarmOverlay(ha: Ha) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val armed = ha.states[ALARM]?.state?.startsWith("armed") == true
    val close = { ha.alarmPrompt = false; ha.alarmStatus = null }
    BackHandler { if (!ha.alarmBusy) close() }
    Column(
        Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) { detectTapGestures { } },
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Shield, contentDescription = null, tint = Bad, modifier = Modifier.size(30.dp))
        Text("ALARMA", color = Bad, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        val status = ha.alarmStatus
        when {
            ha.alarmBusy -> {
                CircularProgressIndicator(indicatorColor = Bad, modifier = Modifier.size(28.dp))
                Text(status ?: "Armando…", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 28.dp))
            }
            status != null || armed -> {
                Text(status ?: "Ya está armada", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 28.dp))
                Button(onClick = close, colors = ButtonDefaults.secondaryButtonColors()) { Icon(Icons.Filled.Check, contentDescription = "Cerrar") }
            }
            else -> {
                Text("¿Armar la alarma?", color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Button(onClick = close, colors = ButtonDefaults.secondaryButtonColors()) { Icon(Icons.Filled.Close, contentDescription = "Cancelar") }
                    Button(
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); scope.launch { ha.armAlarm() } },
                        colors = ButtonDefaults.primaryButtonColors(backgroundColor = Bad),
                    ) { Icon(Icons.Filled.Check, contentDescription = "Armar") }
                }
            }
        }
    }
}

private val NETWORK_COLORS = mapOf(
    "whatsapp" to Color(0xFF25D366), "telegram" to Color(0xFF29B6F6), "instagram" to Color(0xFFE1306C),
    "linkedin" to Color(0xFF0A66C2), "google messages" to Color(0xFF1A73E8), "beeper" to Color(0xFF9C27B0),
)

fun networkColor(network: String) = NETWORK_COLORS.entries.firstOrNull { network.lowercase().contains(it.key) }?.value ?: HaBlue

/** Recent Beeper chats (all networks), newest first. Needs the PC with Beeper Desktop on. */
@Composable
fun ChatsList(ha: Ha, open: (Int) -> Unit) {
    LaunchedEffect(Unit) { ha.loadChats() }
    val list = rememberScalingLazyListState()
    val chats = ha.beeperChats
    Scaffold(positionIndicator = { PositionIndicator(list) }) {
        ScalingLazyColumn(Modifier.fillMaxSize(), state = list) {
            item { ListHeader { Text("Chats", color = Color(0xFF25D366)) } }
            when {
                ha.beeperAvailable == false -> item {
                    Text("Beeper no disponible\n(¿PC o Beeper Desktop apagado?)", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                chats.isEmpty() -> item { Text(ha.status.ifEmpty { "Cargando…" }, color = Muted, fontSize = 12.sp) }
            }
            itemsIndexed(chats.take(30)) { i, c ->
                // the logo already says the network: show the last message, unread count first.
                // Beeper sends no text for voice notes, stickers and reactions, hence the placeholder.
                val sub = listOf(if (c.unread > 0) "${c.unread}" else "", c.preview.ifBlank { "📎 Multimedia" }).filter { it.isNotBlank() }.joinToString(" · ")
                Chip(
                    onClick = { open(i) },
                    label = { Text(c.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (c.muted) Muted else Color.White) },
                    secondaryLabel = { Text(sub, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (c.unread > 0) networkColor(c.network) else Muted) },
                    icon = { Icon(brandIcon(c.network), contentDescription = c.network, tint = networkColor(c.network)) },
                    colors = ChipDefaults.chipColors(backgroundColor = CardBg, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Latest messages of one chat and a Responder button (keyboard, dictation or quick replies), confirmed before sending. */
@Composable
fun ChatView(ha: Ha, chat: BeeperChat) {
    var msgs by remember { mutableStateOf<List<BeeperMsg>?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var pending by remember { mutableStateOf<String?>(null) }
    var viewing by remember { mutableStateOf<Media?>(null) }
    val ctx = LocalContext.current
    LaunchedEffect(reload) { ha.loadMessages(chat.id) { msgs = it } }
    val input = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        r.data?.let { RemoteInput.getResultsFromIntent(it)?.getCharSequence(REPLY_KEY)?.toString() }
            ?.takeIf { it.isNotBlank() }?.let { pending = it }
    }
    val recorder = remember { VoiceRecorder(ctx) }
    var recording by remember { mutableStateOf(false) }
    var recorded by remember { mutableStateOf<Double?>(null) } // seconds, waiting for ✓
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) { onDispose { recorder.discard() } } // never leave a recording behind
    val startRec = { if (recorder.start()) recording = true else toast(ctx, "No se pudo grabar") }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRec() else toast(ctx, "Sin permiso de micrófono")
    }
    val list = rememberScalingLazyListState()
    Box(Modifier.fillMaxSize()) {
        Scaffold(positionIndicator = { PositionIndicator(list) }) {
            ScalingLazyColumn(Modifier.fillMaxSize(), state = list) {
                item { ListHeader { Text(chat.title, color = networkColor(chat.network), maxLines = 1, overflow = TextOverflow.Ellipsis) } }
                item {
                    Chip(
                        onClick = {
                            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                            RemoteInputIntentHelper.putRemoteInputsExtra(
                                intent,
                                listOf(RemoteInput.Builder(REPLY_KEY).setLabel(chat.title).setChoices(QUICK_REPLIES).build()),
                            )
                            input.launch(intent)
                        },
                        label = { Text("Responder") },
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = networkColor(chat.network)) },
                        colors = ChipDefaults.chipColors(backgroundColor = CardBg, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Chip(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRec()
                            else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        label = { Text("Nota de voz") },
                        icon = { Icon(Icons.Filled.Mic, contentDescription = null, tint = networkColor(chat.network)) },
                        colors = ChipDefaults.chipColors(backgroundColor = CardBg, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                val m = msgs
                if (m == null) item { Text("Cargando…", color = Muted, fontSize = 12.sp) }
                else items(m.take(15).size) { i -> Bubble(ha, m[i], chat.network) { viewing = it } }
            }
        }
        viewing?.let { MediaViewer(ha, it) { viewing = null } }
        pending?.let { text ->
            SendConfirm(chat.title, text, onCancel = { pending = null }) {
                pending = null
                ha.sendText(chat.id, text) { ok ->
                    toast(ctx, if (ok) "Enviado" else "No se pudo enviar")
                    reload++
                }
            }
        }
        if (recording) Recording(onCancel = { recording = false; recorder.discard() }) {
            recording = false
            val secs = recorder.stop()
            if (secs < 0.5) { recorder.discard(); toast(ctx, "Demasiado corta") } else recorded = secs
        }
        recorded?.let { secs ->
            if (sending) Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(indicatorColor = networkColor(chat.network))
            } else SendConfirm(chat.title, "🎤 Nota de voz (${secs.roundToInt()} s)", onCancel = { recorded = null; recorder.discard() }) {
                val f = recorder.file
                if (f == null) { recorded = null; return@SendConfirm }
                sending = true
                scope.launch {
                    val err = ha.sendVoice(chat.id, f, recorder.mime, secs)
                    recorder.discard()
                    sending = false
                    recorded = null
                    toast(ctx, if (err == null) "Nota de voz enviada" else "No se pudo enviar ($err)")
                    reload++
                }
            }
        }
    }
}

/** Full-screen recording: red mic, running seconds, ■ to stop (then ✗/✓), back or ✗ to throw it away. */
@Composable
fun Recording(onCancel: () -> Unit, onStop: () -> Unit) {
    var secs by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); secs++ } }
    BackHandler { onCancel() }
    Column(
        Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) { detectTapGestures { } },
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Mic, contentDescription = null, tint = Bad, modifier = Modifier.size(34.dp))
        Text("Grabando  %d:%02d".format(secs / 60, secs % 60), color = Color.White, fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(onClick = onCancel, colors = ButtonDefaults.secondaryButtonColors()) { Icon(Icons.Filled.Close, contentDescription = "Descartar") }
            Button(onClick = onStop, colors = ButtonDefaults.primaryButtonColors(backgroundColor = Bad)) { Icon(Icons.Filled.Stop, contentDescription = "Parar") }
        }
    }
}

@Composable
fun Bubble(ha: Ha, m: BeeperMsg, network: String, open: (Media) -> Unit) {
    Column(
        Modifier.fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (m.mine) networkColor(network).copy(alpha = 0.22f) else CardBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = if (m.mine) Alignment.End else Alignment.Start,
    ) {
        if (!m.mine && m.sender.isNotBlank()) Text(m.sender, color = networkColor(network), fontSize = 10.sp, maxLines = 1)
        m.media?.let { MediaThumb(ha, it) { open(it) } }
        val (plain, hrefs) = remember(m.text) { stripHtml(m.text) }
        if (plain.isNotBlank()) Text(linkified(plain, LocalContext.current), color = Color.White, fontSize = 13.sp, maxLines = 6, overflow = TextOverflow.Ellipsis)
        (hrefs.firstOrNull() ?: findLinks(plain).firstOrNull()?.second)?.let { url ->
            val ctx = LocalContext.current
            CompactChip(
                onClick = { openOnPhone(ctx, url) },
                label = { Text("Abrir en el teléfono", fontSize = 11.sp, maxLines = 1) },
                icon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF2A2A2A), contentColor = HaBlue),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Message text with its links highlighted; tapping one opens it on the phone. */
fun linkified(text: String, ctx: Context): AnnotatedString = buildAnnotatedString {
    var at = 0
    for ((range, url) in findLinks(text)) {
        append(text.substring(at, range.first))
        withLink(LinkAnnotation.Clickable(url, TextLinkStyles(SpanStyle(color = HaBlue, textDecoration = TextDecoration.Underline))) { openOnPhone(ctx, url) }) {
            append(text.substring(range.first, range.last + 1))
        }
        at = range.last + 1
    }
    append(text.substring(at))
}

/** A photo, or a video's preview frame with ▶ (videos only play on tap). */
@Composable
fun MediaThumb(ha: Ha, media: Media, onTap: () -> Unit) {
    val src = if (media.video) media.poster else media.url
    val img by produceState<ImageBitmap?>(null, src) { value = src?.let { ha.image(ha.assetUrl(it))?.asImageBitmap() } }
    val ratio = img?.let { it.width.toFloat() / it.height }
        ?: if (media.width > 0 && media.height > 0) media.width.toFloat() / media.height else 1.4f
    Box(
        Modifier.fillMaxWidth().aspectRatio(ratio.coerceIn(0.6f, 2f)).padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2A)).clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        img?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            ?: Icon(if (media.video) Icons.Filled.Videocam else Icons.Filled.Image, contentDescription = null, tint = Muted)
        if (media.video) Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0x99000000)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir", tint = Color.White)
        }
    }
}

/** Full screen: a photo fitted to the screen, or the video streamed from HA (tap pauses/resumes). Back closes. */
@Composable
fun MediaViewer(ha: Ha, media: Media, onClose: () -> Unit) {
    BackHandler { onClose() }
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        if (media.video) {
            AndroidView(
                factory = { c ->
                    VideoView(c).apply {
                        setVideoURI(Uri.parse(ha.assetUrl(media.url)), ha.authHeaders())
                        setOnPreparedListener { start() }
                        setOnClickListener { if (isPlaying) pause() else start() }
                    }
                },
                onRelease = { it.stopPlayback() },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val img by produceState<ImageBitmap?>(null, media.url) { value = ha.image(ha.assetUrl(media.url))?.asImageBitmap() }
            img?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize().clickable(onClick = onClose), contentScale = ContentScale.Fit) }
                ?: CircularProgressIndicator(indicatorColor = HaBlue)
        }
    }
}

/** "¿Enviar a …?" with the text, so a wrist tap can't send by accident. */
@Composable
fun SendConfirm(to: String, text: String, onCancel: () -> Unit, onSend: () -> Unit) {
    BackHandler { onCancel() }
    Column(
        Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) { detectTapGestures { } },
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("¿Enviar a $to?", color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(horizontal = 28.dp))
        Text("“$text”", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 30.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(onClick = onCancel, colors = ButtonDefaults.secondaryButtonColors()) { Icon(Icons.Filled.Close, contentDescription = "Cancelar") }
            Button(onClick = onSend, colors = ButtonDefaults.primaryButtonColors(backgroundColor = Color(0xFF25D366))) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
            }
        }
    }
}

private const val REPLY_KEY = "reply"
private val QUICK_REPLIES = arrayOf<CharSequence>("Ok", "👍", "Ahora no puedo", "Te llamo luego", "Voy")

/** App logo from the card if it has one, else the card's or entity's MDI icon, else a domain icon. */
@Composable
fun TileIcon(ha: Ha, id: String, card: Card?, e: Ent?, c: Color) {
    val logo by produceState<ImageBitmap?>(null, card?.image) { value = card?.image?.let { ha.image(it)?.asImageBitmap() } }
    Box(Modifier.size(34.dp).clip(CircleShape).background(c.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
        logo?.let { Image(it, contentDescription = null, modifier = Modifier.size(22.dp)) }
            ?: Icon(mdi(card?.icon) ?: mdi(e?.attrs?.optString("icon")) ?: iconOf(id), contentDescription = null, tint = c, modifier = Modifier.size(19.dp))
    }
}

/** A sub-menu's icon: its first entity's card/entity icon. */
fun groupIcon(ha: Ha, g: Group): ImageVector {
    val id = g.ids.firstOrNull() ?: return Icons.Filled.Home
    return mdi(ha.dash?.cards?.get(id)?.icon) ?: mdi(ha.states[id]?.attrs?.optString("icon")) ?: iconOf(id)
}

/** HA's named theme colors as used in tile cards. */
fun haColor(name: String?): Color? = when (name) {
    "primary", "accent" -> HaBlue
    "red" -> Color(0xFFF44336)
    "pink" -> Color(0xFFE91E63)
    "purple" -> Color(0xFF926BC7)
    "deep-purple" -> Color(0xFF6E41AB)
    "indigo" -> Color(0xFF3F51B5)
    "blue" -> Color(0xFF2196F3)
    "light-blue" -> Color(0xFF03A9F4)
    "cyan" -> Color(0xFF00BCD4)
    "teal" -> Color(0xFF009688)
    "green" -> Color(0xFF4CAF50)
    "light-green" -> Color(0xFF8BC34A)
    "lime" -> Color(0xFFCDDC39)
    "yellow" -> Color(0xFFFFEB3B)
    "amber" -> Amber
    "orange" -> Color(0xFFFF9800)
    "deep-orange" -> Color(0xFFFF6F22)
    "brown" -> Color(0xFF795548)
    "grey", "blue-grey" -> Color(0xFF9E9E9E)
    else -> null
}

/** Bedside lamp (shade, stem, base): Material Icons has no nightstand lamp, so it's drawn here. */
val TableLamp: ImageVector by lazy {
    ImageVector.Builder("TableLamp", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(7f, 3f); horizontalLineTo(17f); lineTo(20f, 11f); horizontalLineTo(4f); close() // shade
            moveTo(11f, 11f); horizontalLineTo(13f); verticalLineTo(18f); horizontalLineTo(11f); close() // stem
            moveTo(7f, 18f); horizontalLineTo(17f); verticalLineTo(21f); horizontalLineTo(7f); close() // base
        }
    }.build()
}

/** The MDI icons used on the dashboard; anything unmapped falls back to a domain icon. */
fun mdi(name: String?): ImageVector? = when (name?.removePrefix("mdi:")) {
    "power", "power-standby" -> Icons.Filled.PowerSettingsNew
    "server", "server-network" -> Icons.Filled.Dns
    "raspberry-pi" -> Icons.Filled.Memory
    "desktop-tower", "desktop-classic" -> Icons.Filled.Computer
    "monitor", "monitor-multiple" -> Icons.Filled.Monitor
    "weather-night" -> Icons.Filled.Bedtime
    "movie-open", "movie" -> Icons.Filled.Movie
    "home-export-outline" -> Icons.AutoMirrored.Filled.ExitToApp
    "ceiling-light" -> Icons.Filled.Light
    "lamp-night" -> TableLamp
    "desk-lamp" -> Icons.Filled.Highlight
    "lightbulb", "lightbulb-group", "lamp", "floor-lamp" -> Icons.Filled.Lightbulb
    "music", "speaker" -> Icons.Filled.MusicNote
    "camera", "cctv" -> Icons.Filled.CameraAlt
    "television", "television-classic" -> Icons.Filled.Tv
    "gesture-tap-button" -> Icons.Filled.TouchApp
    "android" -> Icons.Filled.Android
    "docker" -> Icons.Filled.Inventory2
    "shield-home", "shield-lock" -> Icons.Filled.Shield
    else -> null
}

fun act(ha: Ha, e: Ent) {
    val d = e.domain
    when (d) {
        "light", "switch", "input_boolean", "fan", "cover", "automation" -> ha.call(d, "toggle", e.id)
        "scene", "script" -> ha.call(d, "turn_on", e.id)
        "button", "input_button" -> ha.call(d, "press", e.id)
        "lock" -> ha.call(d, if (e.state == "locked") "unlock" else "lock", e.id)
        "media_player" -> ha.call(d, "media_play_pause", e.id)
    }
}

fun colorOf(e: Ent?): Color = when {
    e == null || e.state == "unavailable" -> Muted
    e.domain == "alarm_control_panel" -> if (e.state == "disarmed") Ok else Bad
    e.domain in PRESS_DOMAINS -> HaBlue
    !e.on -> Muted
    e.domain == "light" -> Amber
    else -> HaBlue
}

fun stateText(e: Ent?): String = when {
    e == null -> "…"
    e.state == "unavailable" -> "No disponible"
    e.domain == "light" && e.on && e.attrs.has("brightness") -> "${(e.attrs.optDouble("brightness") / 2.55).roundToInt()}%"
    e.domain in setOf("scene", "script") -> "Activar"
    e.domain in setOf("button", "input_button") -> "Pulsar"
    e.domain == "camera" -> "Cámara"
    e.domain == "alarm_control_panel" -> ALARM_TEXT[e.state] ?: e.state
    e.state == "on" -> "Encendido"
    e.state == "off" -> "Apagado"
    else -> listOf(e.state, e.unit).filter { it.isNotEmpty() }.joinToString(" ")
}

fun iconOf(id: String): ImageVector = when {
    "kill_switch" in id -> Icons.Filled.PowerSettingsNew
    "smart_switch" in id -> Icons.Filled.Dns
    "noche" in id -> Icons.Filled.Bedtime
    "peli" in id -> Icons.Filled.Movie
    "ausente" in id -> Icons.AutoMirrored.Filled.DirectionsWalk
    "monitor" in id -> Icons.Filled.Monitor
    "contenedor" in id -> Icons.Filled.Inventory2
    "capture" in id -> Icons.Filled.CameraAlt
    id.startsWith("media_player.") || "tv" in id -> Icons.Filled.Tv
    else -> when (id.substringBefore('.')) {
        "light" -> Icons.Filled.Lightbulb
        "scene" -> Icons.Filled.Palette
        "script", "automation" -> Icons.Filled.PlayArrow
        "button", "input_button" -> Icons.Filled.RadioButtonChecked
        "alarm_control_panel" -> Icons.Filled.Shield
        "camera" -> Icons.Filled.Videocam
        "lock" -> Icons.Filled.Lock
        else -> Icons.Filled.ToggleOn
    }
}

private fun toast(ctx: Context, s: String) = Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show()

/** Verisure only sends a new picture after a capture, so tapping captures and re-fetches as it lands. */
@Composable
fun CameraView(ha: Ha, id: String) {
    var img by remember { mutableStateOf<ImageBitmap?>(null) }
    var busy by remember { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val btn = Ha.captureButton(id)
    LaunchedEffect(tick) { ha.camera(id)?.let { img = it.asImageBitmap() } }
    Box(
        Modifier.fillMaxSize().background(Color.Black).clickable {
            if (busy) return@clickable
            if (btn in ha.states) {
                busy = true
                ha.call("button", "press", btn)
                scope.launch {
                    repeat(4) { delay(6000); tick++ }
                    busy = false
                }
            } else tick++
        },
        contentAlignment = Alignment.Center,
    ) {
        img?.let { Image(it, contentDescription = ha.states[id]?.name, Modifier.fillMaxSize(0.94f).clip(CircleShape), contentScale = ContentScale.Crop) }
            ?: CircularProgressIndicator(indicatorColor = HaBlue)
        Text(
            ha.states[id]?.name ?: id, color = Color.White, fontSize = 12.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp).background(Color(0x99000000), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
        )
        Text(
            if (busy) "Capturando…" else "Toca para capturar", color = if (busy) Amber else Muted, fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp).background(Color(0x99000000), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun Summaries(ha: Ha, sdb: SwipeToDismissBoxState) {
    Paged(SERVERS.size + 1, Violet, sdb) { i -> if (i == 0) HomeSummary(ha) else GaugePage(ha, SERVERS[i - 1]) }
}

@Composable
fun HomeSummary(ha: Ha) {
    val lights = ha.states.values.filter { it.domain == "light" }
    val on = lights.count { it.on }
    val w = ha.states[WEATHER]
    val temp = w?.attrs?.optDouble("temperature")?.takeIf { !it.isNaN() }?.let { " ${it.roundToInt()}°" }.orEmpty()
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text("Casa", color = Violet, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        SummaryRow(Icons.Filled.Shield, colorOf(ha.states[ALARM]), "Alarma", stateText(ha.states[ALARM]))
        SummaryRow(Icons.Filled.Lightbulb, if (on > 0) Amber else Muted, "Luces", "$on/${lights.size} encendidas")
        SummaryRow(Icons.Filled.WbSunny, HaBlue, "Tiempo", w?.let { (WEATHER_TEXT[it.state] ?: it.state) + temp } ?: "…")
        SummaryRow(Icons.AutoMirrored.Filled.DirectionsWalk, colorOf(ha.states[AWAY]), "Modo ausente", stateText(ha.states[AWAY]))
    }
}

@Composable
fun SummaryRow(icon: ImageVector, c: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(c.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = c, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = Muted, fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

fun pct(e: Ent?): Float? = if (e?.unit == "%") e.state.toFloatOrNull()?.coerceIn(0f, 100f) else null

fun shortValue(e: Ent?): String =
    e?.state?.toDoubleOrNull()?.let { v -> v.roundToInt().toString() + if (e.unit == "%") "%" else " ${e.unit}" } ?: "—"

/** Three arcs hugging the bezel (cpu, ram, disk); red past 85%. Temperature in the middle. */
@Composable
fun GaugePage(ha: Ha, s: Server) {
    val rings = listOf(Triple("cpu", ha.states[s.cpu], HaBlue), Triple("ram", ha.states[s.ram], Violet), Triple("disco", ha.states[s.disk], Amber))
    val temp = s.temp?.let { ha.states[it]?.state?.toDoubleOrNull() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(6.dp)) {
            val sw = 9.dp.toPx()
            rings.forEachIndexed { i, (_, e, c) ->
                val inset = i * 14.dp.toPx() + sw / 2
                val tl = Offset(inset, inset)
                val sz = Size(size.width - 2 * inset, size.height - 2 * inset)
                drawArc(Color(0xFF2A2A2A), 120f, 300f, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round))
                pct(e)?.let { p -> drawArc(if (p >= 85f) Bad else c, 120f, 300f * p / 100f, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round)) }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(s.name, color = Muted, fontSize = 12.sp)
            Text(temp?.let { "${it.roundToInt()}°" } ?: "—", color = if ((temp ?: 0.0) >= 80) Bad else Color.White, fontSize = 34.sp)
            Row {
                rings.forEachIndexed { i, (_, e, c) ->
                    if (i > 0) Text(" · ", color = Muted, fontSize = 11.sp)
                    Text(shortValue(e), color = c, fontSize = 11.sp)
                }
            }
            Text("cpu · ram · disco", color = Muted, fontSize = 10.sp)
        }
    }
}
