package com.watch.omnitrix

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class OmnitrixDialView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    /** Called when the user taps the highlighted (top) alien while the ring is open. */
    var onAlienSelected: ((Alien) -> Unit)? = null
    /** Called whenever the mode changes (long press), so the activity can update a label. */
    var onModeChanged: ((OmnitrixMode) -> Unit)? = null

    private var mode = OmnitrixMode.IDLE

    private val aliens = AlienRoster.ALL
    private val slotAngleDeg = 360f / aliens.size

    private var expanded = false
    private var expansion = 0f          // 0 = collapsed, 1 = fully expanded ring
    private var rotationDeg = 0f        // current ring rotation
    private var idlePulse = 0f          // 0..1 breathing glow

    private var centerX = 0f
    private var centerY = 0f
    private var ringRadius = 0f
    private var centerRadius = 0f

    private val symbolBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.omnitrix_symbol)
    private val alienBitmaps: Map<Int, Bitmap> = aliens.associate {
        it.id to BitmapFactory.decodeResource(resources, it.drawableRes)
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#1A1A1A")
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var expandAnimator: ValueAnimator? = null
    private var snapAnimator: ValueAnimator? = null
    private var pulseAnimatorRef: ValueAnimator? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // needed for BlurMaskFilter glow
        restartPulse()
    }

    private fun restartPulse() {
        pulseAnimatorRef?.cancel()
        val pulseAnim = ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = mode.pulseMillis
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                idlePulse = it.animatedValue as Float
                invalidate()
            }
        }
        pulseAnim.start()
        pulseAnimatorRef = pulseAnim
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        val minDim = minOf(w, h).toFloat()
        ringRadius = minDim * 0.40f
        centerRadius = minDim * 0.22f
    }

    // ---------- touch handling ----------

    private var downAngle = 0f
    private var lastAngle = 0f
    private var totalDrag = 0f
    private var downX = 0f
    private var downY = 0f

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            mode = mode.next()
            restartPulse()
            onModeChanged?.invoke(mode)
            SoundGenerator.playClick()
            invalidate()
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        val dx = event.x - centerX
        val dy = event.y - centerY
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val distFromCenter = hypot(dx, dy)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downAngle = angle
                lastAngle = angle
                totalDrag = 0f
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (expanded) {
                    var delta = angle - lastAngle
                    if (delta > 180f) delta -= 360f
                    if (delta < -180f) delta += 360f
                    rotationDeg += delta
                    totalDrag += Math.abs(delta)
                    lastAngle = angle
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                val moved = hypot((event.x - downX).toDouble(), (event.y - downY).toDouble())
                val wasTap = moved < 18 && totalDrag < 12f
                if (wasTap) {
                    handleTap(distFromCenter)
                } else if (expanded) {
                    snapToNearest()
                }
            }
        }
        return true
    }

    private fun handleTap(distFromCenter: Float) {
        if (!expanded) {
            if (distFromCenter <= centerRadius * 1.3f) {
                setExpanded(true)
            }
            return
        }
        // Expanded: tap on the center collapses; tap anywhere on the ring selects
        // whichever alien is currently highlighted at the top marker for a clean,
        // reliable interaction on a tiny square watch face.
        if (distFromCenter <= centerRadius * 0.9f) {
            setExpanded(false)
        } else {
            selectHighlighted()
        }
    }

    private fun currentHighlightIndex(): Int {
        // Top of the ring (-90 deg) is the selection marker.
        var a = ((-90f - rotationDeg) % 360f + 360f) % 360f
        val idx = Math.round(a / slotAngleDeg) % aliens.size
        return idx
    }

    private fun selectHighlighted() {
        val alien = aliens[currentHighlightIndex()]
        SoundGenerator.playTransform()
        onAlienSelected?.invoke(alien)
    }

    private fun snapToNearest() {
        val target = Math.round(rotationDeg / slotAngleDeg) * slotAngleDeg
        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofFloat(rotationDeg, target).apply {
            duration = 180
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                rotationDeg = it.animatedValue as Float
                invalidate()
            }
        }
        snapAnimator?.start()
        SoundGenerator.playClick()
    }

    private fun setExpanded(value: Boolean) {
        expanded = value
        expandAnimator?.cancel()
        expandAnimator = ValueAnimator.ofFloat(expansion, if (value) 1f else 0f).apply {
            duration = 260
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                expansion = it.animatedValue as Float
                invalidate()
            }
        }
        expandAnimator?.start()
        SoundGenerator.playClick()
    }

    fun setMode(newMode: OmnitrixMode) {
        mode = newMode
        restartPulse()
        onModeChanged?.invoke(mode)
        invalidate()
    }

    // ---------- drawing ----------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // outer dial ring, fades in as the wheel expands
        if (expansion > 0.01f) {
            ringPaint.alpha = (expansion * 255).toInt()
            canvas.drawCircle(centerX, centerY, ringRadius, ringPaint)

            val highlightIdx = currentHighlightIndex()
            for ((i, alien) in aliens.withIndex()) {
                val angleDeg = -90f + i * slotAngleDeg + rotationDeg
                val rad = Math.toRadians(angleDeg.toDouble())
                val r = ringRadius
                val ax = centerX + (r * cos(rad)).toFloat()
                val ay = centerY + (r * sin(rad)).toFloat()

                val isHighlighted = i == highlightIdx
                val baseSize = ringRadius * 0.30f
                val size = if (isHighlighted) baseSize * 1.15f else baseSize * 0.85f
                val alpha = if (isHighlighted) 255 else (150 * expansion).toInt()

                if (isHighlighted) drawGlow(canvas, ax, ay, size * 0.8f, mode.tint, (200 * expansion).toInt())

                drawAlienIcon(canvas, alienBitmaps[alien.id], ax, ay, size, alpha, mode.tint)
            }
        }

        // center hourglass symbol: shrinks slightly when expanded, always glows
        val symbolScale = 1f - 0.35f * expansion
        val symbolSize = centerRadius * 1.7f * symbolScale
        val glowAlpha = (90 + idlePulse * 120).toInt().coerceIn(0, 255)
        drawGlow(canvas, centerX, centerY, symbolSize * 0.75f, mode.tint, glowAlpha)
        drawAlienIcon(canvas, symbolBitmap, centerX, centerY, symbolSize, 255, mode.tint)
    }

    private fun drawGlow(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, alpha: Int) {
        if (radius <= 0f || alpha <= 0) return
        glowPaint.shader = RadialGradient(
            cx, cy, radius,
            intArrayOf(withAlpha(color, alpha), withAlpha(color, 0)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, glowPaint)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    private fun drawAlienIcon(
        canvas: Canvas, bmp: Bitmap?, cx: Float, cy: Float, size: Float, alpha: Int, tint: Int
    ) {
        if (bmp == null || size <= 0f) return
        val half = size / 2f
        val dest = RectF(cx - half, cy - half, cx + half, cy + half)
        bitmapPaint.alpha = alpha
        bitmapPaint.colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bmp, null, dest, bitmapPaint)
    }
}
