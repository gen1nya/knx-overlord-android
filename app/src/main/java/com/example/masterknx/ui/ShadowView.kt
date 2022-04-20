package com.example.masterknx.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.example.masterknx.R


class ShadowView(
    context: Context,
    attributeSet: AttributeSet
) : View(
    context, attributeSet
) {

    private val shadowRadius = 10f.dpToPx()
    private val paddingHorizontal = 10F.dpToPx()
    private val paddingVertical = 10F.dpToPx()
    private val cornerRadius = 12F.dpToPx()

    private val paint = Paint().apply {
        color = context.getColor(R.color.purple_500)
        setShadowLayer(shadowRadius, 0f, 0f, context.getColor(R.color.purple_200))
        this@ShadowView.setLayerType(LAYER_TYPE_SOFTWARE, this)
    }

    private val corners = floatArrayOf(
        cornerRadius,
        cornerRadius,
        cornerRadius,
        cornerRadius,
        cornerRadius,
        cornerRadius,
        cornerRadius,
        cornerRadius
    )

    private var path: Path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val rect = RectF(paddingHorizontal, paddingVertical, w.toFloat() - paddingHorizontal, h.toFloat() - paddingVertical)
        path.addRoundRect(rect, corners, Path.Direction.CW)
    }

    private fun Float.dpToPx(): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics
    )
}