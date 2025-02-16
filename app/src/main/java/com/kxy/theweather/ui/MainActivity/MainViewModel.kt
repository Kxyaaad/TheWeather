package com.kxy.theweather.ui.MainActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.StringUtils.getString
import com.blankj.utilcode.util.TimeUtils
import com.kxy.theweather.R
import com.kxy.theweather.network.ApiService
import com.kxy.theweather.network.GeoDataModel
import com.kxy.theweather.network.OkClient
import com.kxy.theweather.network.TdtClient
import com.kxy.theweather.network.TempDataModel
import com.kxy.theweather.utils.handelRequestFail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val netClient = OkClient.create(ApiService::class.java)
    val dates = MutableLiveData(arrayListOf<String>())
    val placeName = MutableLiveData<String>()
    val listTyp = MutableLiveData(0) // 温度列表的类型，0 分天，1 分时
    val dayTempMap = mutableMapOf<String, ArrayList<Float>>() // 按天和时段分开存储温度
    var minY = 0f  //设置 Y 轴统一的最大和最小刻度
    var maxY = 0f
    val times = arrayListOf<String>()  // 用于分时段的列表
    val temperatures = arrayListOf<Float>()  // 用于分时段的列表
    var currentTemp: Float = 0f

    companion object {
        var latitude = "30.5728" //成都的经纬度
        var longitude = "104.0668"
    }


    //请求天气数据
    fun requestTemp() {
        this.times.clear()
        this.temperatures.clear()

        netClient.getWeatherByLatLon(
            latitude = latitude,
            longitude = longitude
        ).enqueue(object : Callback<TempDataModel> {
            override fun onResponse(call: Call<TempDataModel>, response: Response<TempDataModel>) {
                if (response.isSuccessful) {
                    val currentTime =
                        TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd'T'HH:00"))
                    response.body()?.let { body ->
                        minY = body.hourly.temperature_2m.min() - 2f
                        maxY = body.hourly.temperature_2m.max() + 2f
                        body.hourly.time.forEachIndexed { timeIndex, timeStr ->
                            val date = timeStr.take(10) // 取前 10 个字符
                            try {
                                times.add(timeStr)
                                temperatures.add(body.hourly.temperature_2m[timeIndex])
                                dayTempMap.getOrPut(date) { arrayListOf() }
                                    .add(body.hourly.temperature_2m[timeIndex])
                                if (timeStr == currentTime) {
                                    this@MainViewModel.currentTemp =
                                        body.hourly.temperature_2m[timeIndex]
                                }
                            } catch (e: IndexOutOfBoundsException) {
                                // 万一出现接口返回的温度数量和时间数量对不上的情况，捕获下异常
                                this@MainViewModel.handelRequestFail("数据错误")
                            }

                        }
                        dates.postValue(dayTempMap.keys.toList() as ArrayList<String>?)
                    }
                } else {
                    this@MainViewModel.handelRequestFail(response.message()) { requestTemp() }
                }
            }

            override fun onFailure(call: Call<TempDataModel>, t: Throwable) {
                this@MainViewModel.handelRequestFail(
                    t.message ?: getString(R.string.unknown_err)
                ) { requestTemp() }
            }
        })
    }


    fun getPlaceNameByGeo() {
        val postStr = "{'lon':${longitude},'lat':${latitude},'ver':1}"
        TdtClient.create(ApiService::class.java)
            .getPlaceName(postStr)
            .enqueue(object : Callback<GeoDataModel> {
                override fun onResponse(
                    call: Call<GeoDataModel>,
                    response: Response<GeoDataModel>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.result?.addressComponent?.let {
                            placeName.postValue("${it.city}${it.town}")
                        }
                    } else {
                        this@MainViewModel.handelRequestFail(response.message())
                    }
                }

                override fun onFailure(call: Call<GeoDataModel>, t: Throwable) {
                    this@MainViewModel.handelRequestFail(
                        t.message ?: getString(R.string.unknown_err)
                    )
                }
            })
    }
}