package com.lagradost.cloudstream3.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.lagradost.cloudstream3.R

class DynamicHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class State {
        S0_GRADIENT,        // y = 0
        S1_TRANSITION,      // 0 < y < 115
        S2_HARD_CUT         // y >= 115
    }

    private var currentState = State.S0_GRADIENT
    private var currentY = 0

    private val dThreshold = (115 * resources.displayMetrics.density).toInt()
    private val s0Height = (150 * resources.displayMetrics.density).toInt()
    private val s2Height = (110 * resources.displayMetrics.density).toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var headerHeight = s0Height
    private var headerAlpha = 0.7f
    private var gradientStop = 0.2f

    private var gradient: LinearGradient? = null

    init {
        updateGradient()
    }

    private fun updateGradient() {
        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.primaryBlackBackground),
            Color.TRANSPARENT
        )
        val positions = floatArrayOf(0f, gradientStop)
        
        gradient = LinearGradient(
            0f, 0f, 0f, s0Height.toFloat(),
            colors, positions, Shader.TileMode.CLAMP
        )
    }

    fun updateScroll(y: Int) {
        currentY = y

        val nextState = when {
            y <= 0 -> State.S0_GRADIENT
            y < dThreshold -> State.S1_TRANSITION
            else -> State.S2_HARD_CUT
        }

        currentState = nextState
        updateVisuals()
        invalidate()
    }

    private fun updateVisuals() {
        when (currentState) {
            State.S0_GRADIENT -> {
                headerHeight = s0Height
                gradientStop = 0.2f
            }
            State.S1_TRANSITION -> {
                val progress = currentY.toFloat() / dThreshold.toFloat().coerceAtLeast(1f)
                headerHeight = (s0Height - (s0Height - s2Height) * progress).toInt()
                gradientStop = 0.2f + (0.75f * progress)
            }
            State.S2_HARD_CUT -> {
                headerHeight = s2Height
                gradientStop = 0.95f
            }
        }
        updateGradient()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.alpha = (headerAlpha * 255).toInt()
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, s0Height)
    }
}
