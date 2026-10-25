package io.hacompanion.watch

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class BeeperTest {
    @Test
    fun chatsNewestFirstWithoutArchived() {
        val chats = Ha.parseChats(
            JSONObject(
                """{"available":true,"chats":[
                  {"id":"!a","title":"Old","network":"WhatsApp","unread":0,"last_activity":"2026-10-20T10:00:00Z","preview":"hola"},
                  {"id":"!b","title":"New","network":"Telegram","unread":3,"last_activity":"2026-10-25T09:00:00Z","preview":{"text":"qué tal"}},
                  {"id":"!c","title":"Archived","network":"WhatsApp","archived":true,"last_activity":"2026-10-25T10:00:00Z"}]}"""
            )
        )
        assertEquals(listOf("New", "Old"), chats.map { it.title })
        assertEquals("qué tal", chats[0].preview)
        assertEquals(3, chats[0].unread)
    }

    @Test
    fun networkLogoKeys() {
        assertEquals("whatsapp", brandKey("WhatsApp"))
        assertEquals("googlemessages", brandKey("Google Messages"))
        assertEquals("linkedin", brandKey("LinkedIn"))
        assertEquals(null, brandKey("Beeper"))
    }

    @Test
    fun packedArcFlagsAreSpelledOut() {
        // "0 01-5.031-1.378": rotation 0, flags 0 and 1, then x -5.031 y -1.378
        val tokens = expandArcFlags("M1 2a9.87 9.87 0 01-5.031-1.378l-.361-.214").trim().split(Regex("\\s+"))
        assertEquals(listOf("M1", "2a", "9.87", "9.87", "0", "0", "1", "-5.031", "-1.378", "l-.361-.214"), tokens)
        // two arcs in a row (the second without repeating "a")
        val two = expandArcFlags("a1 1 0 00.5.5 1 1 0 11-1 1").trim().split(Regex("\\s+"))
        assertEquals(listOf("a", "1", "1", "0", "0", "0", ".5", ".5", "1", "1", "0", "1", "1", "-1", "1"), two)
    }

    @Test
    fun imagesAndVideosBecomeMedia() {
        val msgs = Ha.parseMessages(
            JSONObject(
                """{"messages":[
                  {"id":"1","mine":false,"time":"2026-10-25T09:00:00Z","text":"","attachments":[{"type":"img","url":"mxc://a/1","width":800,"height":600}]},
                  {"id":"2","mine":false,"time":"2026-10-25T09:01:00Z","text":"mira","attachments":[{"type":"video","url":"mxc://a/2","poster":"mxc://a/2p"}]}]}"""
            )
        )
        val video = msgs[0]
        assertEquals(true, video.media?.video)
        assertEquals("mxc://a/2p", video.media?.poster)
        assertEquals("mira", video.text)
        val image = msgs[1]
        assertEquals(false, image.media?.video)
        assertEquals("mxc://a/1", image.media?.url)
        assertEquals("", image.text) // the picture is shown, no "Imagen" label
    }

    @Test
    fun linksFoundWithoutTrailingPunctuation() {
        val links = findLinks("mira https://www.instagram.com/reel/abc123/?igsh=x. y www.youtube.com/watch?v=1, fin")
        assertEquals(listOf("https://www.instagram.com/reel/abc123/?igsh=x", "https://www.youtube.com/watch?v=1"), links.map { it.second })
        assertEquals("https://www.instagram.com/reel/abc123/?igsh=x", "mira https://www.instagram.com/reel/abc123/?igsh=x. y".substring(links[0].first))
        assertEquals(emptyList<Any>(), findLinks("sin enlaces"))
    }

    @Test
    fun htmlBecomesPlainTextKeepingHrefs() {
        val (plain, hrefs) = stripHtml("""<b>Oferta</b> &amp; más<br>mira <a href="https://t.me/x/1">aquí</a>""")
        assertEquals("Oferta & más\nmira aquí", plain)
        assertEquals(listOf("https://t.me/x/1"), hrefs)
        assertEquals("sin html" to emptyList<String>(), stripHtml("sin html"))
    }

    @Test
    fun messagesLabelAttachments() {
        val msgs = Ha.parseMessages(
            JSONObject(
                """{"messages":[
                  {"id":"1","sender":"Ana","mine":false,"time":"2026-10-25T09:00:00Z","text":"","attachments":[{"type":"audio","voice":true}]},
                  {"id":"2","sender":{"name":"Yo"},"mine":true,"time":"2026-10-25T09:05:00Z","text":"ok"}]}"""
            )
        )
        assertEquals(listOf("ok", "🎤 Nota de voz"), msgs.map { it.text })
        assertEquals("Yo", msgs[0].sender)
    }
}
