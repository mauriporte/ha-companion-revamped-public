package io.hacompanion.watch

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class DashTest {
    private val cfg = JSONObject(
        """
        {"views":[
          {"path":"overview","sections":[
            {"cards":[
              {"type":"heading","heading":"Favoritos","visibility":[{"condition":"state","entity":"input_boolean.show_favorites"}]},
              {"type":"grid","cards":[{"type":"tile","entity":"light.dining","name":"Comedor","color":"amber","icon":"mdi:ceiling-light"},{"type":"tile","entity":"camera.entrance"},
                                      {"type":"tile","entity":"sensor.button_1_battery"},{"type":"tile","entity":"light.dining"}]},
              {"type":"heading","heading":"Áreas"},
              {"type":"area","area":"living","navigation_path":"/dashboard-home/areas-living"}
            ]},
            {"cards":[{"type":"heading","heading":"Apps"}]},
            {"cards":[{"type":"heading","heading":"Server1"},
                      {"type":"vertical-stack","cards":[{"type":"tile","entity":"switch.portainer_contenedor",
                        "tap_action":{"action":"toggle","entity":"input_boolean.show_server1_apps"}}]}]}
          ]},
          {"path":"areas-living","title":"Living","subview":true,"sections":[
            {"cards":[{"type":"heading","heading":"Luces"},{"type":"entities","entities":["light.living","light.dining"]}]},
            {"cards":[{"type":"heading","heading":"Escenas"},{"type":"tile","entity":"scene.audio"}]},
            {"cards":[{"type":"heading","heading":"Monitor1"},{"type":"tile","entity":"switch.monitor1"}]}]}
        ]}
        """
    )

    @Test
    fun parsesDashboardLayout() {
        val d = Dash.parse(cfg)
        assertEquals(listOf("light.dining", "camera.entrance"), d.fav.ids)
        assertEquals(listOf("Living"), d.areas.map { it.title })
        assertEquals(listOf("light.living", "light.dining", "scene.audio", "switch.monitor1"), d.areas[0].ids)
        // 2+ controls → own sub-menu; single-control sections pooled into "Más"
        assertEquals(listOf("Luces", "Más"), d.areas[0].subs.map { it.title })
        assertEquals(listOf("scene.audio", "switch.monitor1"), d.areas[0].subs[1].ids)
        assertEquals(listOf("Server1"), d.menus.map { it.title })
        assertEquals(listOf("switch.portainer_contenedor"), d.menus[0].ids)
        assertEquals(true, d.menus[0].guarded)
        val card = d.cards.getValue("light.dining")
        assertEquals(listOf("Comedor", "mdi:ceiling-light", "amber", null), listOf(card.name, card.icon, card.color, card.image))
        assertEquals(false, "camera.entrance" in d.cards)
    }

    @Test
    fun mergesStateDiff() {
        val old = Ent("light.x", "off", JSONObject("""{"friendly_name":"X","brightness":10}"""))
        val new = Ha.merge(old, JSONObject("""{"+":{"s":"on","a":{"brightness":200}},"-":{"a":["friendly_name"]}}"""))
        assertEquals("on", new.state)
        assertEquals(200, new.attrs.getInt("brightness"))
        assertEquals("x", new.name)
    }
}
