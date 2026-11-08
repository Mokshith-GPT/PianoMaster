package com.pianomaster.app.domain

import kotlin.math.roundToInt

data class PianoNote(val midi: Int, val name: String, val octave: Int, val frequencyHz: Double) {
    companion object {
        fun fromMidi(midi: Int): PianoNote {
            val names = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
            val n = names[Math.floorMod(midi, 12)]
            val octave = Math.floorDiv(midi, 12) - 1
            val hz = 440.0 * Math.pow(2.0, (midi - 69) / 12.0)
            return PianoNote(midi, n, octave, hz)
        }
        fun fromFrequency(hz: Double): PianoNote? {
            if (!hz.isFinite() || hz <= 0.0) return null
            val midi = (69 + 12 * (Math.log(hz / 440.0) / Math.log(2.0))).roundToInt()
            return fromMidi(midi)
        }
    }
}

data class LessonStep(val id: String, val instruction: String, val expectedMidi: Int? = null, val hint: String = "")
data class Lesson(val id: String, val title: String, val level: String, val steps: List<LessonStep>)
data class PracticeResult(val expectedMidi: Int, val detectedMidi: Int?, val correct: Boolean, val timestampMs: Long)
