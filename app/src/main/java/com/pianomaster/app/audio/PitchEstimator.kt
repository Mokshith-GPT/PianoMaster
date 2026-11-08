package com.pianomaster.app.audio

/** Lightweight monophonic autocorrelation pitch estimator. Not a polyphonic piano transcription engine. */
class PitchEstimator(private val sampleRate: Int = 44_100) {
    fun estimate(samples: ShortArray): Double? {
        if (samples.size < 2048) return null
        val x = DoubleArray(samples.size) { samples[it].toDouble() / 32768.0 }
        val rms = kotlin.math.sqrt(x.sumOf { it * it } / x.size)
        if (rms < 0.008) return null
        val minLag = sampleRate / 1000 // ~1000 Hz
        val maxLag = minOf(sampleRate / 55, x.size / 2) // ~55 Hz
        var bestLag = -1
        var best = 0.0
        for (lag in minLag..maxLag) {
            var corr = 0.0
            var normA = 0.0
            var normB = 0.0
            val limit = x.size - lag
            var i = 0
            while (i < limit) {
                corr += x[i] * x[i + lag]
                normA += x[i] * x[i]
                normB += x[i + lag] * x[i + lag]
                i += 2
            }
            val score = if (normA > 0 && normB > 0) corr / kotlin.math.sqrt(normA * normB) else 0.0
            if (score > best) { best = score; bestLag = lag }
        }
        if (bestLag <= 0 || best < 0.55) return null
        return sampleRate.toDouble() / bestLag
    }
}
