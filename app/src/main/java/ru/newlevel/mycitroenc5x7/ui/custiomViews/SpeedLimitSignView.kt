package ru.newlevel.mycitroenc5x7.ui.custiomViews


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SpeedLimitSignView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var speedLimitText: String = "10" // Текст по умолчанию
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }


    fun setSpeedLimit(speed: Int) {
        speedLimitText = speed.toString()
        invalidate() // Перерисовываем View
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        textPaint.textSize = width / 2.1f
        textPaint.isFakeBoldText = true
        borderPaint.strokeWidth = width / 11f
        val radius = width.coerceAtMost(height) / 2f - borderPaint.strokeWidth


        canvas.drawCircle(centerX, centerY, radius, circlePaint)
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
        val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(speedLimitText, centerX, textY, textPaint)
    }
}