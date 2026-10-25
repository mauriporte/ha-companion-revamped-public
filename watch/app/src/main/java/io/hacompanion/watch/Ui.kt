package io.hacompanion.watch

import android.content.Context
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import java.io.File
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.wear.compose.material.CompactChip
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

val HOME_SECTIONS = listOf("fav", "menus", "areas", "res")

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
                    "menus" -> Section("Menús", Icons.Filled.Apps, HaBlue, d?.let { "${it.menus.size} servidores" })
                    "areas" -> Section("Áreas", Icons.Filled.Home, Ok, d?.let { "${it.areas.size} áreas" })
                    else -> Section("Resúmenes", Icons.Filled.DonutLarge, Violet, "Casa · ${SERVERS.size} servidores")
                }
                val route = when (key) { "fav" -> "fav"; "menus" -> "list/menus"; "areas" -> "list/areas"; else -> "res" }
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
fun guarded(id: String) = "kill_switch" in id || id.startsWith("lock.")

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

/** Common MDI icons used on dashboards; anything unmapped falls back to a domain icon. */
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
