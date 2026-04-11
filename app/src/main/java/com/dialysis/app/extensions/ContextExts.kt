package com.dialysis.app.extensions

import android.content.Context
import android.widget.Toast

private var toast: Toast? = null
fun Context.toast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    if (toast != null) {
        toast?.cancel()
    }
    toast = Toast.makeText(this, message, duration)
    toast?.show()
}