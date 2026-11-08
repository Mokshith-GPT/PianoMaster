package com.pianomaster.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/** Captures mono PCM from the phone mic and reports a best-effort monophonic pitch estimate. */
class MicrophonePitchListener(
    private val sampleRate: Int = 44_100,
    private val onPitch: (frequencyHz: Double?) -> Unit
) {
    private val running = AtomicBoolean(false)
    private var recorder: AudioRecord? = null
    private val estimator = PitchEstimator(sampleRate)

    fun start() {
        if (!running.compareAndSet(false, true)) return
        val minBuffer = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuffer <= 0) { running.set(false); onPitch(null); return }
        val bufferSize = maxOf(minBuffer, 8192)
        val audio = try { AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize) } catch (_: Exception) { running.set(false); onPitch(null); return }
        if (audio.state != AudioRecord.STATE_INITIALIZED) { audio.release(); running.set(false); onPitch(null); return }
        recorder = audio
        try { audio.startRecording() } catch (_: Exception) { audio.release(); recorder = null; running.set(false); onPitch(null); return }
        thread(name = "PianoMasterMic", isDaemon = true) {
            val buffer = ShortArray(4096)
            while (running.get()) {
                val read = audio.read(buffer, 0, buffer.size)
                if (read > 0) onPitch(estimator.estimate(if (read == buffer.size) buffer else buffer.copyOf(read)))
            }
        }
    }

    fun stop() {
        running.set(false)
        try { recorder?.stop() } catch (_: IllegalStateException) { }
        recorder?.release()
        recorder = null
    }
}
