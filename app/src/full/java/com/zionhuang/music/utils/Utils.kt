package com.zionhuang.music.utils

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

fun reportException(throwable: Throwable) {
    Firebase.crashlytics.recordException(throwable)
    throwable.printStackTrace()
}
