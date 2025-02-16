package com.kxy.theweather.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat

class LocationHelper(private val context: Context) {
    private lateinit var locationManager: LocationManager

    fun getLocation(locationListener: LocationListener) {
        this.requestPermission(locationListener)
    }

    /**
     * 首先请求权限
     */
    private fun requestPermission(locationListener: LocationListener) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestSingleUpdate(criteria, locationListener, Looper.getMainLooper())
    }

}