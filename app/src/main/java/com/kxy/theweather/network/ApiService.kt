package com.kxy.theweather.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    /**
     * 根据经纬度获取当地的温度
     * @param lat 纬度
     * @param long 经度
     * @return
     */
    @GET("/v1/forecast?hourly=temperature_2m")
    fun getWeatherByLatLon(
        @Query("latitude") latitude:String,
        @Query("longitude") longitude:String
    ): Call<TempDataModel>

    /**
     * 用天地图接口逆地理编码查询
     */
    @GET("geocoder?type=geocode&tk=ce12727b4005449d53c96de3619c003a")
    fun getPlaceName(
        @Query("postStr") postStr:String
    ): Call<GeoDataModel>
}