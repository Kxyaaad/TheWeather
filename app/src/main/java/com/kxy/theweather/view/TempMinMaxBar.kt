package com.kxy.theweather.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.kxy.theweather.R

class TempMinMaxBar : View {

    private var width = 0
    private var height = 0
    private var minValue = 0f
    private var maxValue = 10f
    private var minTemp = 0f
    private var maxTemp = 0f
    private lateinit var barDrawable: Drawable

    constructor(context: Context) : super(context) {
        this.init(null)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        this.init(attributeSet)
    }

    private fun init(attributeSet: AttributeSet?) {
        barDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_color_orange_180)!!
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        width = w
        height = h
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (::barDrawable.isInitialized) {
            barDrawable.apply {
                if (maxValue != minValue) {
                    val radio: Float = width / (maxValue - minValue)
                    bounds = Rect(
                        ((minTemp - minValue) * radio).toInt(),
                        0,
                        width - ((maxValue - maxTemp) * radio).toInt(),
                        height
                    )
                }else {
                    bounds = Rect(
                        0,
                        0,
                        0,
                        height
                    )
                }
                draw(canvas)
            }
        }
    }

    /**
     * 设置进度条的参数值
     * @param minValue 整个进度条的最小值
     * @param maxValue 整个进度条的最大值
     * @param minTemp 当前天的最低温度
     * @param maxTemp 当前天的最高温度
     */
    fun setValues(
        minValue: Float,
        maxValue: Float,
        minTemp: Float,
        maxTemp: Float
    ) {
        this.minValue = minValue
        this.maxValue = maxValue
        this.minTemp = minTemp
        this.maxTemp = maxTemp
        this.invalidate()
    }

}