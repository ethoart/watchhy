package com.watch.omnitrix

import android.graphics.Color

/**
 * Long-press anywhere on the home dial to cycle modes.
 * IDLE       -> green,  normal standby glow
 * NEW_SCAN   -> yellow, "scanning for new DNA" pulse (faster pulse)
 * LOW_POWER  -> red,    slow dim pulse, battery-saving visual cue
 */
enum class OmnitrixMode(val tint: Int, val label: String, val pulseMillis: Long) {
    IDLE(Color.parseColor("#00FF2A"), "READY", 1400L),
    NEW_SCAN(Color.parseColor("#FFD400"), "NEW DNA SCAN", 700L),
    LOW_POWER(Color.parseColor("#FF1A1A"), "LOW POWER", 2200L);

    fun next(): OmnitrixMode = when (this) {
        IDLE -> NEW_SCAN
        NEW_SCAN -> LOW_POWER
        LOW_POWER -> IDLE
    }
}
