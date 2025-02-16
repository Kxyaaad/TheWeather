package com.kxy.theweather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.kxy.theweather.databinding.ItemDayTempBinding
import com.kxy.theweather.ui.MainActivity.MainViewModel
import com.kxy.theweather.utils.gone
import com.kxy.theweather.utils.visible

class DayWeatherAdapter(private val mainViewModel: MainViewModel) :
    RecyclerView.Adapter<DayWeatherViewHolder>() {

    companion object {
        const val LIST_TYPE_DAY = 0 // 按天排列
        const val LIST_TYPE_HOUR = 1 // 具体到时段的排列
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayWeatherViewHolder {
        val binding = ItemDayTempBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayWeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayWeatherViewHolder, position: Int) {
        if (mainViewModel.listTyp.value == LIST_TYPE_DAY) {
            val minTemp = mainViewModel.dayTempMap[mainViewModel.dates.value?.get(position)]?.min()
            val maxTemp = mainViewModel.dayTempMap[mainViewModel.dates.value?.get(position)]?.max()

            holder.maxTemp.text = buildString {
                append(maxTemp)
                append("℃")
            }

            holder.minTemp.visible()
            holder.minTemp.text = buildString {
                append(minTemp)
                append("℃")
            }

            holder.lineBar.setValues(
                minValue = mainViewModel.minY + 2f,
                maxValue = mainViewModel.maxY - 2f,
                maxTemp = maxTemp ?: 0f,
                minTemp = minTemp ?: 0f
            )

            val date = mainViewModel.dates.value?.get(position)
            if (TimeUtils.getNowString().substring(0, 10) == date) {
                holder.labelDate.text = "今天"
            } else {
                val timeFormat = TimeUtils.getSafeDateFormat("yyyy-MM-dd")
                val week = TimeUtils.getChineseWeek(date, timeFormat)
                holder.labelDate.text = week
            }
        } else {
            holder.minTemp.gone()
            holder.maxTemp.text = buildString {
                append(mainViewModel.temperatures[position])
                append("℃")
            }

            val date = mainViewModel.times[position]
            if (TimeUtils.getNowString().take(10) == date.take(10)) {
                holder.labelDate.text = buildString {
                    append("今天 ")
                    append(date.substring(11, 13).toInt())
                    append("时")
                }
            } else {
                val timeFormat = TimeUtils.getSafeDateFormat("yyyy-MM-dd")
                val week = TimeUtils.getChineseWeek(date, timeFormat)
                holder.labelDate.text = buildString {
                    append(week)
                    append(" ")
                    append(date.substring(11, 13).toInt())
                    append("时")
                }
            }

            holder.lineBar.setValues(
                minValue = mainViewModel.minY + 2f,
                maxValue = mainViewModel.maxY - 2f,
                maxTemp = mainViewModel.temperatures[position] + 0.05f, //把线条加宽点
                minTemp = mainViewModel.temperatures[position] - 0.05f
            )
        }

    }

    override fun getItemCount(): Int {
        return if (mainViewModel.listTyp.value == LIST_TYPE_DAY) mainViewModel.dates.value?.count()
            ?: 0 else mainViewModel.times.count()
    }
}

class DayWeatherViewHolder(binding: ItemDayTempBinding) : RecyclerView.ViewHolder(binding.root) {
    val lineBar = binding.lineBar
    val maxTemp = binding.maxTemp
    val minTemp = binding.minTemp
    val labelDate = binding.labelDate
}