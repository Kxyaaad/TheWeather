package com.kxy.theweather

import android.app.Application
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.IOSStyle
import com.kxy.theweather.worker.WeatherUpdateWorkerScheduler

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DialogX.init(this)
        DialogX.globalTheme = DialogX.THEME.LIGHT
        DialogX.autoRunOnUIThread = true
        DialogX.globalStyle = IOSStyle()

        // 执行后台更新任务
        WeatherUpdateWorkerScheduler.scheduleHourlyUpdate(this)
    }
}