package io.hacompanion.watch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.wear.remote.interactions.RemoteActivityHelper

private val LINK = Regex("""(https?://|www\.)[^\s<>"]+""", RegexOption.IGNORE_CASE)

/** URLs in a message (with their position), trailing punctuation left out; "www." links get https:// added. */
fun findLinks(text: String): List<Pair<IntRange, String>> = LINK.findAll(text).map { m ->
    val raw = m.value.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '…')
    val range = m.range.first until m.range.first + raw.length
    range to (if (raw.startsWith("www.", ignoreCase = true)) "https://$raw" else raw)
}.toList()

private val HREF = Regex("""<a\s[^>]*href\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
private val BREAK = Regex("""<br\s*/?>|</p>\s*<p[^>]*>""", RegexOption.IGNORE_CASE)
private val TAG = Regex("""<[^>]+>""")
private val ENTITIES = mapOf("&amp;" to "&", "&lt;" to "<", "&gt;" to ">", "&quot;" to "\"", "&#39;" to "'", "&apos;" to "'", "&nbsp;" to " ")

/**
 * Beeper passes some messages as HTML (Telegram formatting, links): plain text plus the hrefs, which aren't
 * always visible in the text. Text without tags or entities is returned unchanged.
 */
fun stripHtml(text: String): Pair<String, List<String>> {
    if ('<' !in text && '&' !in text) return text to emptyList()
    val hrefs = HREF.findAll(text).map { it.groupValues[1] }.filter { it.startsWith("http", ignoreCase = true) }.toList()
    var plain = TAG.replace(BREAK.replace(text, "\n"), "")
    for ((entity, char) in ENTITIES) plain = plain.replace(entity, char)
    return plain.trim() to hrefs
}

/** Asks the paired phone to open [url]; Android there picks the app (Instagram, YouTube…) or the browser. */
fun openOnPhone(ctx: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
    val future = RemoteActivityHelper(ctx).startRemoteActivity(intent)
    Toast.makeText(ctx, "Abriendo en el teléfono…", Toast.LENGTH_SHORT).show()
    future.addListener({
        if (runCatching { future.get() }.isFailure) Toast.makeText(ctx, "No se pudo abrir en el teléfono", Toast.LENGTH_SHORT).show()
    }, ContextCompat.getMainExecutor(ctx))
}
