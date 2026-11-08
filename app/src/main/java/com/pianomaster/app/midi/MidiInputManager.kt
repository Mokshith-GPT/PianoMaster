package com.pianomaster.app.midi

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Handler
import android.os.Looper

/** Optional future MIDI path. Current user setup is audio-output-only, so this is not the active input path. */
class MidiInputManager(context: Context, private val onNoteOn: (midiNote: Int, velocity: Int) -> Unit) {
    private val manager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
    private var device: MidiDevice? = null

    fun listDevices(): List<MidiDeviceInfo> = manager.devices.toList()

    fun open(info: MidiDeviceInfo, onError: (Throwable) -> Unit = {}) {
        manager.openDevice(info, { opened ->
            if (opened == null) { onError(IllegalStateException("Unable to open MIDI device")); return@openDevice }
            device = opened
            val input = opened.openOutputPort(0) ?: run { onError(IllegalStateException("No MIDI output port")); return@openDevice }
            input.connect(object : MidiReceiver() {
                override fun onSend(data: ByteArray, offset: Int, count: Int, timestamp: Long) {
                    var i = offset
                    val end = offset + count
                    while (i + 2 < end) {
                        val status = data[i].toInt() and 0xFF
                        val note = data[i + 1].toInt() and 0x7F
                        val velocity = data[i + 2].toInt() and 0x7F
                        if ((status and 0xF0) == 0x90 && velocity > 0) onNoteOn(note, velocity)
                        i += 3
                    }
                }
            })
        }, Handler(Looper.getMainLooper()))
    }

    fun close() { device?.close(); device = null }
}
