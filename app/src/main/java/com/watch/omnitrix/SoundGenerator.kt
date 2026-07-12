package com.watch.omnitrix

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.min

/**
 * Generates short sci-fi style tones entirely in code (PCM synthesis), so the app
 * never bundles or reproduces any audio from the original show and stays 100% offline.
 */
object SoundGenerator {

    private const val SAMPLE_RATE = 44100

    /** Rising sweep + a bright "confirm" chirp — played when an alien is selected. */
    fun playTransform() {
        val durationSec = 0.9
        val samples = buildSweep(
            durationSec = durationSec,
            startFreq = 220.0,
            endFreq = 1400.0
        )
        play(samples)
    }

    /** Short low click — played for UI taps / dial rotation snaps. */
    fun playClick() {
        val samples = buildSweep(
            durationSec = 0.06,
            startFreq = 900.0,
            endFreq = 500.0
        )
        play(samples)
    }

    private fun buildSweep(durationSec: Double, startFreq: Double, endFreq: Double): ShortArray {
        val sampleCount = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(sampleCount)
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / sampleCount
            val freq = startFreq + (endFreq - startFreq) * progress
            // simple amplitude envelope: quick attack, smooth decay
            val envelope = min(1.0, t * 40.0) * (1.0 - progress).let { it * it }
            val value = sin(2.0 * PI * freq * t) * envelope
            samples[i] = (value * Short.MAX_VALUE * 0.9).toInt().toShort()
        }
        return samples
    }

    private fun play(samples: ShortArray) {
        val bufferSize = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.setNotificationMarkerPosition(samples.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(t: AudioTrack?) {
                t?.release()
            }
            override fun onPeriodicNotification(t: AudioTrack?) {}
        })
        track.play()
    }
}
