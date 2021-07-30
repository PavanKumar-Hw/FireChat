package com.example.firechat.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T) {
            removeObserver(this)
            observer.onChanged(t)
        }
    })
}

fun Gson.fromObject(list: Any?): String {
    if (list == null) {
        return ""
    }
    val type = object : TypeToken<Any>() {}.type

    return this.toJson(list, type)
}

fun Gson.fromString(value: String, type: Type): Any? {
    return this.fromJson(
        value,
        type
    )
}