package com.kxy.theweather.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object TdtClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(ClientInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl("http://api.tianditu.gov.cn/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(clazz: Class<T>): T {
        return retrofit.create(clazz)
    }
}