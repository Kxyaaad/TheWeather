package com.kxy.theweather.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kxy.theweather.ui.MainActivity.MainViewModel
import com.kxy.theweather.ui.MainActivity.fragment.WeatherChartFragment

class ChartViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    // 获取 MainViewModel
    private val mainViewModel by lazy {
        ViewModelProvider(fa)[MainViewModel::class.java]
    }


    override fun getItemCount(): Int {
        return this.mainViewModel.dates.value?.size ?: 0
    }

    override fun createFragment(position: Int): Fragment {
        return WeatherChartFragment.newInstance(position)
    }

}