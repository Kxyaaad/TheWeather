package com.kxy.theweather.utils

import android.view.View
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.StringUtils.getString
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kxy.theweather.R

/**
 * viewModel的扩展函数，用来处理网络请求错误
 * @param msg 弹窗的错误消息
 * @param callBack 如果需要重试加载，需要调用的方法
 */
fun ViewModel.handelRequestFail(msg: String, callBack: (()->Unit)) {
    WaitDialog.dismiss()
    MessageDialog.show(getString(R.string.network_err), msg)
        .setOkButton(getString(R.string.retry))
        .setOkButton { _, _ ->
            callBack.invoke()
            return@setOkButton false
        }
        .setCancelButton(getString(R.string.cancel))
}

fun ViewModel.handelRequestFail(msg: String) {
    WaitDialog.dismiss()
    MessageDialog.show(getString(R.string.network_err), msg)
        .setCancelButton(getString(R.string.cancel))
}

/**
 * 隐藏视图
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 显示视图
 */
fun View.visible() {
    visibility = View.VISIBLE
}