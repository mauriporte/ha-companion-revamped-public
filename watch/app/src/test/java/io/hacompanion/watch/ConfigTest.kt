package io.hacompanion.watch

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConfigTest {
    @Test
    fun nullTempIsNoTempNotTheTextNull() {
        Config.apply(
            JSONObject(
                """{"homeUrl":"http://h:8123","externalUrl":"","servers":[{"name":"NAS","cpu":"sensor.c","ram":"sensor.r","disk":"sensor.d","temp":null}],
                   "ring":[{"id":"light.a","icon":"mdi:lightbulb","color":"amber"},{"id":"alarm_control_panel.x","icon":"mdi:shield-home","color":"red","route":"alarm"}]}"""
            )
        )
        assertNull(Config.servers[0].temp)
        assertEquals(listOf("sensor.c", "sensor.r", "sensor.d"), Config.servers[0].ids)
        assertNull(Config.ring[0].route)
        assertEquals("alarm", Config.ring[1].route)
        // a blank remote address is dropped instead of tried
        assertEquals(listOf("http://h:8123"), Config.urls(null))
    }
}
