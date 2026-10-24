package io.hacompanion.watch

import org.json.JSONArray
import org.json.JSONObject

class Group(val title: String, val ids: List<String>, val guarded: Boolean = false, val subs: List<Group> = emptyList())

/** How the dashboard card presents an entity: its overrides win over the entity's own name/icon. */
class Card(val name: String?, val icon: String?, val color: String?, val image: String?)

/** The watch layout, read from your dashboard (config "dashboard") so editing it in HA updates the watch. */
class Dash(val fav: Group, val menus: List<Group>, val areas: List<Group>, val cards: Map<String, Card> = emptyMap()) {
    val all get() = (listOf(fav) + menus + areas).flatMap { it.ids }.toSet()

    companion object {
        private val ACTIONABLE = setOf(
            "light", "switch", "scene", "script", "button", "input_button", "input_boolean", "fan",
            "cover", "lock", "media_player", "camera", "alarm_control_panel", "automation"
        )
        // entity refs under these keys are conditions/actions, not things to show
        private val SKIP = setOf("visibility", "conditions", "card_mod", "tap_action", "hold_action", "double_tap_action")

        fun parse(cfg: JSONObject): Dash {
            val views = cfg.getJSONArray("views").objects()
            val overview = views.first { it.optString("path") == "overview" }

            // flatten all section cards; each heading card starts a new group
            val byHeading = LinkedHashMap<String, MutableList<JSONObject>>()
            var heading = ""
            for (s in overview.optJSONArray("sections").objects()) for (c in s.optJSONArray("cards").objects()) {
                if (c.optString("type") == "heading") {
                    heading = c.optString("heading")
                    byHeading.getOrPut(heading) { mutableListOf() }
                } else byHeading.getOrPut(heading) { mutableListOf() } += c
            }

            val fav = Group("Favoritos", keep(entities(byHeading["Favoritos"])))

            val areas = navPaths(byHeading["Áreas"]).mapNotNull { path ->
                views.firstOrNull { it.optString("path") == path }?.let { v ->
                    Group(v.optString("title", path), keep(entities(v.opt("sections") ?: v.opt("cards"))), subs = subsOf(v))
                }
            }.filter { it.ids.isNotEmpty() }

            // every heading after "Apps" is one server's container menu
            val names = byHeading.keys.toList()
            val menus = names.drop(names.indexOf("Apps") + 1).takeIf { "Apps" in names }.orEmpty()
                .map { Group(it, keep(entities(byHeading[it])), guarded = true) }
                .filter { it.ids.isNotEmpty() }

            return Dash(fav, menus, areas, cards(cfg))
        }

        /** First card per entity that sets a name, icon, color or image. */
        fun cards(x: Any?, out: MutableMap<String, Card> = mutableMapOf()): Map<String, Card> {
            when (x) {
                is JSONObject -> {
                    val e = x.opt("entity") as? String
                    if (e != null && e !in out && listOf("name", "icon", "color", "image").any { x.has(it) && x.opt(it) is String }) {
                        out[e] = Card(x.str("name"), x.str("icon"), x.str("color"), x.str("image"))
                    }
                    for (k in x.keys()) if (k !in SKIP) cards(x.get(k), out)
                }
                is JSONArray -> for (i in 0 until x.length()) cards(x.get(i), out)
            }
            return out
        }

        private fun JSONObject.str(k: String) = (opt(k) as? String)?.takeIf { it.isNotBlank() }

        /**
         * An area's sub-menus from its subview's headed sections. Sections with 2+ controls get their own
         * sub-menu; single-control sections (one per container on server areas) are pooled into "Más".
         * Empty when that leaves a single group, so the area opens straight into its buttons.
         */
        private fun subsOf(view: JSONObject): List<Group> {
            val named = mutableListOf<Group>()
            val loose = mutableListOf<String>()
            for (s in view.optJSONArray("sections").objects()) {
                val cards = s.optJSONArray("cards")
                val title = cards.objects().firstOrNull { it.optString("type") == "heading" }?.optString("heading").orEmpty()
                val ids = keep(entities(cards))
                if (ids.size >= 2 && title.isNotBlank()) named += Group(title, ids) else loose += ids
            }
            val all = named + listOfNotNull(loose.distinct().takeIf { it.isNotEmpty() }?.let { Group("Más", it) })
            return if (all.size > 1) all else emptyList()
        }

        private fun keep(ids: List<String>) =
            ids.filter { it.substringBefore('.') in ACTIONABLE }.distinct()

        fun entities(x: Any?, out: MutableList<String> = mutableListOf()): List<String> {
            when (x) {
                is JSONObject -> {
                    (x.opt("entity") as? String)?.let { out += it }
                    x.optJSONArray("entities")?.let { arr ->
                        for (i in 0 until arr.length()) when (val e = arr.get(i)) {
                            is String -> out += e
                            is JSONObject -> e.optString("entity").takeIf { it.isNotEmpty() }?.let { out += it }
                        }
                    }
                    for (k in x.keys()) if (k !in SKIP && k != "entities" && k != "entity") entities(x.get(k), out)
                }
                is JSONArray -> for (i in 0 until x.length()) entities(x.get(i), out)
                is List<*> -> x.forEach { entities(it, out) }
            }
            return out
        }

        /** navigation_path values ("/dashboard-home/areas-living" or "areas-living") → view paths, in order. */
        private fun navPaths(x: Any?, out: MutableList<String> = mutableListOf()): List<String> {
            when (x) {
                is JSONObject -> for (k in x.keys()) {
                    val v = x.get(k)
                    if (k == "navigation_path" && v is String) out += v.trimEnd('/').substringAfterLast('/')
                    else navPaths(v, out)
                }
                is JSONArray -> for (i in 0 until x.length()) navPaths(x.get(i), out)
                is List<*> -> x.forEach { navPaths(it, out) }
            }
            return out.distinct()
        }

        private fun JSONArray?.objects(): List<JSONObject> =
            if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
    }
}
