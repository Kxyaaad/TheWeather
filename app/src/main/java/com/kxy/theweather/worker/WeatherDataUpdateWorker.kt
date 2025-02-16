package com.kxy.theweather.worker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.TimeUtils
import com.kxy.theweather.network.ApiService
import com.kxy.theweather.network.OkBackGroundClient
import com.kxy.theweather.network.OkClient
import com.kxy.theweather.ui.MainActivity.MainViewModel
import kotlinx.coroutines.delay

class WeatherDataUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val notificationId = 10001

    override suspend fun doWork(): Result {
        return try {
            updateData()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun updateData() {
        try {
            val response = OkBackGroundClient.create(ApiService::class.java)
                .getWeatherByLatLon(
                    latitude = MainViewModel.latitude,
                    longitude = MainViewModel.longitude
                ).execute()
            if (response.isSuccessful) {
                val currentTime =
                    TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd'T'HH:00"))
                response.body()?.let { body ->
                    body.hourly.time.forEachIndexed { timeIndex, timeStr ->
                        try {
                            if (timeStr == currentTime) {
                                this.sendNotification(
                                    "天气报告",
                                    "当前室外气温${body.hourly.temperature_2m[timeIndex]}℃，请关注天气变化。"
                                )
                            }
                        } catch (e: IndexOutOfBoundsException) {
                            Log.e("后台请求错误", e.message.toString())
                        }

                    }
                }
            }
        } catch (e: Exception) {
            Log.e("后台", "请求失败: ${e.message}")
        }
    }


    private fun sendNotification(title: String, message: String) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // 创建通知渠道（针对 Android 8.0 以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "weather_update_channel"
            val channelName = "天气更新通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        // 构建通知
        val notification = NotificationCompat.Builder(applicationContext, "weather_update_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_info_details) // 设置小图标
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 设置优先级
            .build()

        // 发送通知
        notificationManager.notify(notificationId, notification) // 使用 ID 1 来区分不同的通知
    }
}