package io.hacompanion.watch

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmTest {
    private fun ent(id: String, state: String) = Ent(id, state, JSONObject())

    @Test
    fun asksForTheNextAction() {
        assertEquals("¿Apagar Server 1?", confirmQuestion(ent("switch.kill_switch_server1", "on"), "Server 1"))
        assertEquals("¿Encender Server 1?", confirmQuestion(ent("switch.kill_switch_server1", "off"), "Server 1"))
        assertEquals("¿Desbloquear Puerta?", confirmQuestion(ent("lock.puerta", "locked"), "Puerta"))
    }

    @Test
    fun protectedControlsGoLast() {
        val pages = pagesOf(listOf("switch.kill_switch_server1", "lock.front_door", "light.dining", "switch.night_mode", "camera.entrance"))
        // everyday → cameras → power
        assertEquals(listOf("light.dining", "switch.night_mode"), (pages[0] as TilesPage).ids)
        assertEquals("camera.entrance", (pages[1] as CameraPage).id)
        assertEquals(listOf("switch.kill_switch_server1", "lock.front_door"), (pages[2] as TilesPage).ids)
    }

    @Test
    fun websocketUrlForHomeAndTunnel() {
        assertEquals("ws://192.168.1.10:8123/api/websocket", Ha.wsUrl("http://192.168.1.10:8123"))
        assertEquals("wss://ha.example.com/api/websocket", Ha.wsUrl("https://ha.example.com/"))
    }

    @Test
    fun guardsServerPowerAndLocks() {
        listOf("switch.kill_switch_server2", "lock.puerta")
            .forEach { assertTrue(it, guarded(it)) }
        assertEquals(false, guarded("light.dining"))
    }
}
