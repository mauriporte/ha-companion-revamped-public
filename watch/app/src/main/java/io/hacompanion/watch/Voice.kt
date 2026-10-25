package io.hacompanion.watch

import android.content.Context
import android.media.MediaRecorder
import android.os.SystemClock
import java.io.File

/**
 * Records a voice note to the app's cache: OGG/Opus mono (~150 KB/min), the format WhatsApp uses for voice notes.
 * Falls back to AAC in MP4 if the watch has no Opus encoder. The file is deleted by [discard] after sending or cancelling.
 */
class VoiceRecorder(private val ctx: Context) {
    private var rec: MediaRecorder? = null
    private var started = 0L
    var file: File? = null
        private set
    var mime = "audio/ogg"
        private set

    fun start(): Boolean {
        for ((format, encoder, type, ext) in listOf(
            Quad(MediaRecorder.OutputFormat.OGG, MediaRecorder.AudioEncoder.OPUS, "audio/ogg", "ogg"),
            Quad(MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC, "audio/mp4", "m4a"),
        )) {
            val f = File(ctx.cacheDir, "voice-note.$ext")
            @Suppress("DEPRECATION") // MediaRecorder(Context) is API 31; the Gen 6 is API 30
            val r = MediaRecorder()
            val ok = runCatching {
                r.setAudioSource(MediaRecorder.AudioSource.MIC)
                r.setOutputFormat(format)
                r.setAudioEncoder(encoder)
                r.setAudioChannels(1)
                r.setAudioSamplingRate(16000)
                r.setAudioEncodingBitRate(24000)
                r.setOutputFile(f.absolutePath)
                r.prepare()
                r.start()
            }.isSuccess
            if (ok) {
                rec = r; file = f; mime = type; started = SystemClock.elapsedRealtime()
                return true
            }
            r.release(); f.delete()
        }
        return false
    }

    /** Stops recording; returns the duration in seconds (0 if it failed). */
    fun stop(): Double {
        val r = rec ?: return 0.0
        rec = null
        val ok = runCatching { r.stop() }.isSuccess // stop() throws if almost nothing was recorded
        r.release()
        return if (ok) (SystemClock.elapsedRealtime() - started) / 1000.0 else 0.0
    }

    /** Stops if needed and deletes the recording, so nothing is left on the watch. */
    fun discard() {
        rec?.let { runCatching { it.stop() }; it.release() }
        rec = null
        file?.delete()
        file = null
    }

    private data class Quad(val format: Int, val encoder: Int, val mime: String, val ext: String)
}
