package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var progress : Int = 0 // 0 to 100. Will be updated by the download manager
    private var valueAnimator = ValueAnimator()
    private var angle : Float = 0f

    private var bgColor = 0
    private var progressColor = 0
    private var txtColor = 0
    private var indicatorColor = 0
    private var buttonText = ""
    private var loadingText = ""
    private var textSize = 0f

    private var path = Path()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            color = Color.argb(0.2f, 0f, 0f, 0f)
        }else {
            color = Color.BLACK
        }
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        color = Color.WHITE
        isFakeBoldText = true
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
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
            textSize = getDimension(R.styleable.LoadingButton_LoadingButton_textSize, 50f)
        }
        textPaint.color = txtColor
        textPaint.textSize = textSize
        backgroundPaint.color = progressColor
        progressPaint.color = indicatorColor
    }

    /**
     * https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-delegates/observable.html
     * */
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new){
            ButtonState.Loading -> {
                valueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
                    addUpdateListener {
                        angle = animatedValue as Float
                        invalidate()
                    }
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                    duration = 1000
                    start()
                }
                disableLoadingButton()
                invalidate()
            }
            ButtonState.Clicked -> {
                //TODO: Nothing yet.
            }
            ButtonState.Completed -> {
                enableLoadingButton()
                progress = 0
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.translate(10f, 10f)
        path.reset()
        val roundRect = RectF(0f, 0f, widthSize.toFloat(), heightSize.toFloat())
        path.addRoundRect(roundRect, 100f, 100f, Path.Direction.CCW)
        canvas?.drawPath(path, shadowPaint)
        canvas?.clipPath(path)
        canvas?.drawColor(bgColor)
        val centerX = widthSize.toFloat() / 2
        val centerY = heightSize.toFloat() / 2 + (textPaint.textSize/3)
        when(buttonState) {
            ButtonState.Completed -> {
                textPaint.color = txtColor
                canvas?.drawText(buttonText, centerX, centerY, textPaint)
            }
            ButtonState.Loading -> {
                val rectWidth = ((progress * widthSize.toFloat())/100)
                canvas?.drawRect(0f, 0f, rectWidth, heightSize.toFloat(), backgroundPaint)
                textPaint.color = Color.WHITE
                canvas?.drawText(loadingText, centerX, centerY, textPaint)
                val ovalRect = RectF(widthSize-160f, 10f, widthSize-(-heightSize+180f), heightSize-10f)
                canvas?.drawArc(ovalRect, 0f, angle, true, progressPaint)
            }
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
        widthSize = w - 20
        heightSize = h - 20
        setMeasuredDimension(w, h)
    }

    fun setBtnState(state: ButtonState) {
        buttonState = state
    }

    fun setProgress(p: Int) {
        progress = p
        invalidate()
    }

    private fun disableLoadingButton() {
        isEnabled = false
    }

    private fun enableLoadingButton() {
        isEnabled = true
    }



}