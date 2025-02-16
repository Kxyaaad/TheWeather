package com.kxy.theweather.ui.MainActivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
    private var lastCheckOutTime = 0L

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

    /**
     * 获取当前定位，并通过天地图解析出地理位置名
     */
    private fun getLocation() {
        LocationHelper(this).getLocation { location ->
            MainViewModel.latitude = location.latitude.toString()
            MainViewModel.longitude = location.longitude.toString()
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
                this.lastCheckOutTime = TimeUtils.getNowMills() // 记录下本次获取数据的时间
                if (dates.isNotEmpty()) {
                    this@MainActivity.viewModel.currentTemp.toString() // 获取到当前时段的温度时，更新 UI，发送通知
                        .also {
                            mViewBinding.currentTemp.text = it
                            this.sendNotification("天气报告", "当前室外气温${it}℃，请关注天气变化。")
                        }
                    mViewBinding.hintNoData.gone()

                    TabLayoutMediator(
                        mViewBinding.tabs, mViewBinding.chartViewPager
                    ) { tab: TabLayout.Tab, position: Int ->
                        val date = dates[position]
                        val timeFormat = TimeUtils.getSafeDateFormat("yyyy-MM-dd")
                        val day = date.substring(8, 10)
                        val week = TimeUtils.getChineseWeek(date, timeFormat)
                        tab.setText("${day}\n${week}") // 设置 Tab 的文本为日期和周几
                    }.attach()
                    chartAdapter.notifyDataSetChanged()
                    dayWeatherAdapter.notifyDataSetChanged()
                } else {
                    mViewBinding.hintNoData.visible()
                    mViewBinding.hintNoData.text = getString(R.string.no_data_type_to_retry)
                }
            }

        // 通知切换了温度列表的显示类型
        this.viewModel.listTyp
            .distinctUntilChanged()
            .observe(this) { dayWeatherAdapter.notifyDataSetChanged() }

        // 若解析出地址，则更新地址信息
        this.viewModel.placeName
            .distinctUntilChanged()
            .observe(this) { placeName ->
                this@MainActivity.mViewBinding.hintMyPlace.visible()
                this@MainActivity.mViewBinding.placeName.text = placeName
            }

    }


    private fun sendNotification(title: String, message: String) {
        val notificationManager = NotificationManagerCompat.from(this.application)
        val notificationId = 10001
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "weather_update_channel"
            val channelName = "天气更新通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        // 创建通知
        val notification = NotificationCompat.Builder(applicationContext, "weather_update_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // 发送通知
        notificationManager.notify(notificationId, notification)
    }

    override fun onResume() {
        super.onResume()
        if (lastCheckOutTime != 0L) { //如果再次进入前台不是同一个时段，则刷新数据
            if (TimeUtils.millis2String(TimeUtils.getNowMills(), "HH") != TimeUtils.millis2String(
                    lastCheckOutTime,
                    "HH"
                )
            ) {
                viewModel.requestTemp()
            }
        }
    }
}