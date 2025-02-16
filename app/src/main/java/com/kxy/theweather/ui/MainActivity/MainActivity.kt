package com.kxy.theweather.ui.MainActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.distinctUntilChanged
import com.blankj.utilcode.util.TimeUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kxy.theweather.R
import com.kxy.theweather.databinding.ActivityMainBinding
import com.kxy.theweather.ui.adapter.ChartViewPagerAdapter
import com.kxy.theweather.ui.adapter.DayWeatherAdapter
import com.kxy.theweather.utils.LocationHelper
import com.kxy.theweather.utils.gone
import com.kxy.theweather.utils.visible

class MainActivity : AppCompatActivity() {
    private lateinit var mViewBinding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val chartAdapter = ChartViewPagerAdapter(this)


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            this.requestLocationPermission()
        }

        // 实现沉浸式
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        mViewBinding = ActivityMainBinding.inflate(layoutInflater)
        mViewBinding.chartViewPager.adapter = chartAdapter
        setContentView(mViewBinding.root)

        mViewBinding.hintNoData.setOnClickListener {
            this.viewModel.requestTemp()
        }

        mViewBinding.typeSwitch.setOnCheckedChangeListener { _, isChecked ->
            this.viewModel.listTyp.postValue(if (isChecked) DayWeatherAdapter.LIST_TYPE_HOUR else DayWeatherAdapter.LIST_TYPE_DAY)
        }

        setObserver()
    }

    private fun requestLocationPermission() {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!(gpsEnabled || networkEnabled)) viewModel.requestTemp() //如果手机没有开定位，则直接请求默认位置

        if (!XXPermissions.isGranted(this, Permission.ACCESS_FINE_LOCATION)) {
            MessageDialog.show(
                "请允许定位权限",
                "如果您不授予定位权限，则默认显示成都天气"
            ).setOkButton("授权")
                .setOkButton { _, _ ->
                    XXPermissions.with(this)
                        .permission(Permission.ACCESS_FINE_LOCATION)
                        .permission(Permission.ACCESS_COARSE_LOCATION)
                        .permission(Permission.ACCESS_BACKGROUND_LOCATION)
                        .request { permissions, _ ->
                            if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                this.getLocation()
                            } else {
                                this.viewModel.requestTemp() //加载默认成都天气
                            }
                        }
                    return@setOkButton false
                }
                .setCancelButton("取消")
                .setCancelButton { _, _ ->
                    this.viewModel.requestTemp()
                    return@setCancelButton false
                }

        } else {
            this.getLocation()
        }
    }

    private fun getLocation() {
        LocationHelper(this).getLocation { location ->
            Log.e("获取位置", location.toString())
            viewModel.latitude = location.latitude.toString()
            viewModel.longitude = location.longitude.toString()
            viewModel.requestTemp()
            mViewBinding.hintMyPlace.text = "当前位置"
            viewModel.getPlaceNameByGeo()
        }
    }

    /**
     * 设置对 viewmodel 的观察
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver() {
        val dayWeatherAdapter = DayWeatherAdapter(viewModel)
        mViewBinding.dayTempList.adapter = dayWeatherAdapter
        // 绑定 TabLayout 和 ViewPager, 实现交互
        this.viewModel.dates
            .distinctUntilChanged() // 值不变，则不触发
            .observe(this) { dates ->
                if (dates.isNotEmpty()) {
                    this@MainActivity.viewModel.currentTemp.toString()
                        .also { mViewBinding.currentTemp.text = it }
                    mViewBinding.hintNoData.gone()
                    chartAdapter.notifyDataSetChanged()
                    dayWeatherAdapter.notifyDataSetChanged()
                    TabLayoutMediator(
                        mViewBinding.tabs, mViewBinding.chartViewPager
                    ) { tab: TabLayout.Tab, position: Int ->
                        val date = dates[position]
                        val timeFormat = TimeUtils.getSafeDateFormat("yyyy-MM-dd")
                        val day = date.substring(8, 10)
                        val week = TimeUtils.getChineseWeek(date, timeFormat)
                        tab.setText("${day}\n${week}") // 设置 Tab 的文本为日期
                    }.attach()
                } else {
                    mViewBinding.hintNoData.visible()
                    mViewBinding.hintNoData.text = getString(R.string.no_data_type_to_retry)
                }
            }

        this.viewModel.listTyp
            .distinctUntilChanged()
            .observe(this) { dayWeatherAdapter.notifyDataSetChanged() }

        this.viewModel.placeName
            .distinctUntilChanged()
            .observe(this) { placeName ->
                this@MainActivity.mViewBinding.hintMyPlace.visible()
                this@MainActivity.mViewBinding.placeName.text = placeName
            }

    }

}