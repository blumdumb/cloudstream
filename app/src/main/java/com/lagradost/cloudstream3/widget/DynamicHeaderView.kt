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
        S0_GRADIENT,        // Soft fade, alpha 0.3
        S1_TRANSITION_DOWN, // Rapid collapse (0 -> 105dp)
        S2_HARD_CUT,        // Solid block, alpha 0.8
        S3_TRANSITION_UP    // Recovery (30dp -> 0)
    }

    private var currentState = State.S0_GRADIENT
    private var lastY = 0
    private var currentY = 0

    private val dDown = (105 * resources.displayMetrics.density).toInt()
    private val dUpStart = (30 * resources.displayMetrics.density).toInt()
    private val s0Height = (150 * resources.displayMetrics.density).toInt()
    private val s2Height = (80 * resources.displayMetrics.density).toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var headerHeight = s0Height
    private var headerAlpha = 0.3f

    private var gradient: LinearGradient? = null

    init {
        // Initial gradient setup
        updateGradient()
    }

    private fun updateGradient() {
        val colors = intArrayOf(
            ContextCompat.getColor(context, R.color.primaryBlackBackground),
            Color.TRANSPARENT
        )
        gradient = LinearGradient(
            0f, 0f, 0f, s0Height.toFloat(),
            colors, null, Shader.TileMode.CLAMP
        )
    }

    fun updateScroll(y: Int) {
        val dir = if (y > lastY) "DOWN" else "UP"
        currentY = y

        val nextState = when (currentState) {
            State.S0_GRADIENT -> {
                if (dir == "DOWN" && y > 0) State.S1_TRANSITION_DOWN else State.S0_GRADIENT
            }
            State.S1_TRANSITION_DOWN -> {
                if (y >= dDown) State.S2_HARD_CUT else State.S1_TRANSITION_DOWN
            }
            State.S2_HARD_CUT -> {
                if (dir == "UP" && y <= dUpStart) State.S3_TRANSITION_UP else State.S2_HARD_CUT
            }
            State.S3_TRANSITION_UP -> {
                if (y <= 0) State.S0_GRADIENT 
                else if (dir == "DOWN" && y > dUpStart) State.S2_HARD_CUT 
                else State.S3_TRANSITION_UP
            }
        }

        currentState = nextState
        updateVisuals()
        lastY = y
        invalidate()
    }

    private fun updateVisuals() {
        when (currentState) {
            State.S0_GRADIENT -> {
                headerHeight = s0Height
                headerAlpha = 0.3f
            }
            State.S1_TRANSITION_DOWN -> {
                val progress = currentY.toFloat() / dDown.toFloat().coerceAtLeast(1f)
                headerHeight = (s0Height + (s2Height - s0Height) * progress).toInt()
                headerAlpha = 0.3f + (0.5f * progress)
            }
            State.S2_HARD_CUT -> {
                headerHeight = s2Height
                headerAlpha = 0.8f
            }
            State.S3_TRANSITION_UP -> {
                val progress = currentY.toFloat() / dUpStart.toFloat().coerceAtLeast(1f)
                headerHeight = (s2Height + (s0Height - s2Height) * (1f - progress)).toInt()
                headerAlpha = 0.8f - (0.5f * (1f - progress))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.alpha = (headerAlpha * 255).toInt()
        
        if (currentState == State.S2_HARD_CUT) {
            paint.shader = null
            paint.color = ContextCompat.getColor(context, R.color.primaryBlackBackground)
            canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), paint)
        } else {
            paint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        // We set the measure height to the maximum possible height (S0)
        setMeasuredDimension(width, s0Height)
    }
}
