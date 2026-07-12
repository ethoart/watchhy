package com.watch.omnitrix

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class AlienGlowView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private var tint: Int = Color.GREEN
    private var glowLevel = 0f // 0..1, animates in on entry then breathes

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun show(alien: Alien, mode: OmnitrixMode) {
        bitmap = BitmapFactory.decodeResource(resources, alien.drawableRes)
        tint = mode.tint
        val entrance = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 260
            addUpdateListener {
                glowLevel = it.animatedValue as Float
                invalidate()
            }
        }
        entrance.start()
        val breathe = ValueAnimator.ofFloat(0.55f, 1f, 0.55f).apply {
            duration = mode.pulseMillis
            repeatCount = ValueAnimator.INFINITE
            startDelay = 280
            addUpdateListener {
                glowLevel = it.animatedValue as Float
                invalidate()
            }
        }
        breathe.start()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return
        val cx = width / 2f
        val cy = height / 2f
        val size = minOf(width, height) * 0.62f

        if (size <= 0f) return
        val glowAlpha = (glowLevel * 220).toInt().coerceIn(0, 255)
        glowPaint.shader = RadialGradient(
            cx, cy, size * 0.85f,
            intArrayOf(
                Color.argb(glowAlpha, Color.red(tint), Color.green(tint), Color.blue(tint)),
                Color.argb(0, Color.red(tint), Color.green(tint), Color.blue(tint))
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, size * 0.85f, glowPaint)

        val half = size / 2f
        val dest = RectF(cx - half, cy - half, cx + half, cy + half)
        bitmapPaint.alpha = (150 + glowLevel * 105).toInt().coerceIn(0, 255)
        bitmapPaint.colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bmp, null, dest, bitmapPaint)
    }
}
