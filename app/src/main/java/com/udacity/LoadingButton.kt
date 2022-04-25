package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator()

    /**
     * https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-delegates/observable.html
     * */
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }

    private var bgColor = 0
    private var progressColor = 0
    private var txtColor = 0
    private var indicatorColor = 0
    private var buttonText = ""
    private var loadingText = ""

    private var path = Path()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        color = Color.WHITE
    }


    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            bgColor = getColor(R.styleable.LoadingButton_LoadingButton_bgColor, 0)
            progressColor = getColor(R.styleable.LoadingButton_LoadingButton_progressColor, 0)
            txtColor = getColor(R.styleable.LoadingButton_LoadingButton_txtColor, 0)
            indicatorColor = getColor(R.styleable.LoadingButton_LoadingButton_indicatorColor, 0)
            buttonText = getString(R.styleable.LoadingButton_LoadingButton_buttonText).toString()
            loadingText = getString(R.styleable.LoadingButton_LoadingButton_loadingText).toString()
        }
        textPaint.color = txtColor
        backgroundPaint.color = bgColor
    }



    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        path.reset()
        val roundRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        path.addRoundRect(roundRect, 100f, 100f, Path.Direction.CCW)
        canvas?.clipPath(path)
        canvas?.drawColor(bgColor)
        val centerX = measuredWidth.toFloat() / 2
        val centerY = measuredHeight.toFloat() / 2 + (textPaint.textSize/3)
        if (buttonState == ButtonState.Completed) {
            canvas?.drawText(buttonText, centerX, centerY, textPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}