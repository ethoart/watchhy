package com.watch.omnitrix

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class AlienActivity : Activity() {

    companion object {
        const val EXTRA_ALIEN_ID = "extra_alien_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alien)
        hideSystemBars()

        val alienId = intent.getIntExtra(EXTRA_ALIEN_ID, 1)
        val alien = AlienRoster.ALL.firstOrNull { it.id == alienId } ?: AlienRoster.ALL.first()

        val glowView = findViewById<AlienGlowView>(R.id.glowView)
        val nameLabel = findViewById<TextView>(R.id.alienNameLabel)
        nameLabel.text = alien.name

        glowView.show(alien, OmnitrixMode.IDLE)
        SoundGenerator.playTransform()

        glowView.setOnClickListener { finish() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
    }
}
