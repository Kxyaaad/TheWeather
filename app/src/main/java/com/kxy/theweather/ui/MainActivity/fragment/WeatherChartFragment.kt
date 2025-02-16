package com.kxy.theweather.ui.MainActivity.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import com.blankj.utilcode.util.ResourceUtils.getDrawable
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.kxy.theweather.R
import com.kxy.theweather.databinding.FragmentChartBinding
import com.kxy.theweather.ui.MainActivity.MainViewModel

class WeatherChartFragment : Fragment(), OnChartValueSelectedListener {
    private lateinit var mViewBinding: FragmentChartBinding
    private var position = -1
    private val mainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    companion object {
        private const val POSITION = "position"
        fun newInstance(position: Int): WeatherChartFragment {
            val fragment = WeatherChartFragment()
            val bundle = Bundle()
            bundle.putInt(POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        position = arguments?.getInt(POSITION) ?: -1
        mViewBinding = FragmentChartBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (position != -1) {
            this.setData()
        }
    }


    private fun setData() {
        if (mainViewModel.dates.value?.isEmpty() == true) return
        mainViewModel.dates.value?.get(position)?.let { dateStr ->
            val entries = arrayListOf<Entry>()
            mainViewModel.dayTempMap[dateStr]?.forEachIndexed { hourIndex, temp ->
                entries.add(Entry(hourIndex.toFloat() + 1, temp))
            }
            this.initChart(entries)
        }

    }

    // 设置折线图
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initChart(entries: List<Entry>) {
        // 设置数据点属性
        val dataSet = LineDataSet(entries, "").apply {
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawHorizontalHighlightIndicator(false)
            setDrawValues(false)
            setDrawCircles(false)
            fillDrawable = getDrawable(R.drawable.gradient_color_orange_90)
        }

        val lineData = LineData(dataSet)

        // 修改 Y 轴
        mViewBinding.chartView.axisLeft.apply {
            textSize = 14f
            textColor = Color.WHITE
            setLabelCount(3, true)
            axisMinimum = mainViewModel.minY
            axisMaximum = mainViewModel.maxY
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}℃"
                }
            }
        }

        // 修改 X 轴
        mViewBinding.chartView.xAxis.apply {
            textSize = 14f
            textColor = Color.WHITE
            setLabelCount(4, false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}时"
                }
            }

        }

        // 设置图表的属性
        mViewBinding.chartView.apply {
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            data = lineData
            isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            extraBottomOffset = 14f // 设置底部间距，避免文字被遮挡
            setOnChartValueSelectedListener(this@WeatherChartFragment)
            isDoubleTapToZoomEnabled = false // 禁用双击放大
            invalidate()
        }

    }

    // 点击图表上的点位
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        mViewBinding.labelValue.text = buildString {
            append(e?.y)
            append("℃")
        }

        mViewBinding.labelTime.text = buildString {
            if (e != null) {
                append(e.x.toInt())
            }
            append("时")
        }
    }

    // 未点击图表上的点位
    override fun onNothingSelected() {
        mViewBinding.labelValue.text = ""
    }

}