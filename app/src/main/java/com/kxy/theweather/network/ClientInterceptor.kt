package com.kxy.theweather.network

import android.util.Log
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

const val TAG = "NET_REQUEST => "

class ClientInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        WaitDialog.show("正在加载...")
        val request = chain.request().newBuilder().build()
        val buffer = Buffer()
        request.body?.writeTo(buffer)
        Log.e(TAG, request.url.toString() + "=>" + buffer.readUtf8())
        val response = chain.proceed(request)

        val responseBody = response.body ?: return response

        val businessData = responseBody.string()

        val mediaType = responseBody.contentType()

        val newBody = businessData.toResponseBody(mediaType)

        val newResponse = response.newBuilder()
            .body(newBody)
            .build()

        handleBusinessData(businessData)
        WaitDialog.dismiss()
        return newResponse
    }

    private fun handleBusinessData(businessData: String) {
        Log.e(TAG, businessData)
        try {
//            val data = Gson().fromJson<BaseModel<Any>>(businessData, BaseModel::class.java)
//            if (data.code == 520) {
//                user_id = ""
//                access_token = ""
//                BusUtils.post(TOKEN_UNAVAILABLE)
//            } else if (data.code != 200) {
//                MessageDialog.show("请求失败", data.msg)
//            }

        } catch (e: Exception) {
            MessageDialog.show("请求错误", "请检查网络连接，${e.message}")
        }

    }
}