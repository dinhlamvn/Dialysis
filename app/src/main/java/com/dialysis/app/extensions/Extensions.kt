package com.dialysis.app.extensions

import android.content.res.Resources
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

inline fun <reified T : Any> Any.castTo(): T {
    return this as T
}

inline fun <reified T : Any> Any.castToAsNull(): T? {
    return this as? T
}

fun <T : Any?> Boolean.ifTrue(trueValue: T, falseValue: T): T {
    return if (this) trueValue else falseValue
}

infix fun <T : Any> T.sameAs(other: Any): Boolean {
    return this == other
}

infix fun <T : Any?> Boolean.ifTrue(trueValue: T): T? {
    return if (this) trueValue else null
}

infix fun <T : Any?> T?.guardNull(value: T): T {
    return this ?: value
}

val Number.dp
    get() = dp()

val Number.dpF
    get() = dpF()

fun Number.dp() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
).toInt()

fun Number.dpF() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
)

fun ComponentActivity.screenWidth() = resources.displayMetrics.widthPixels

fun ComponentActivity.screenHeight() = resources.displayMetrics.heightPixels

fun Fragment.screenWidth() = resources.displayMetrics.widthPixels

fun Fragment.screenHeight() = resources.displayMetrics.heightPixels