package com.lagradost.cloudstream3.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class AspectRatioImageView @JvmOverloads constructor(
    context: Context, 
    attrs: AttributeSet? = null, 
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var ratio: Float = 1.5f // Default 2:3 vertical

    init {
        val a = context.obtainStyledAttributes(attrs, intArrayOf(R.styleable.AspectRatioImageView_ratio))
        ratio = a.getFloat(0, 1.5f)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width * ratio).toInt()
        setMeasuredDimension(width, height)
    }
}
