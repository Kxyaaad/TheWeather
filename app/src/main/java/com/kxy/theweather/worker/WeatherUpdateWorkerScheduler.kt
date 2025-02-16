package com.kxy.theweather.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WeatherUpdateWorkerScheduler {

    private const val UNIQUE_WORK_NAME = "DataUpdateWork"

    fun scheduleHourlyUpdate(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WeatherDataUpdateWorker>(
            15, // 间隔时间，安卓系统限制，最低 15分钟
            TimeUnit.MINUTES
        )
            .addTag("update_weather")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // 如果已存在则更新
            workRequest
        )
    }
}