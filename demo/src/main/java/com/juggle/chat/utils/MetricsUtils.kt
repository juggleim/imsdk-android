package com.juggle.chat.utils

import android.content.res.Resources
import kotlin.math.roundToInt

fun Int.toDp(): Int = (this * Resources.getSystem().displayMetrics.density).roundToInt()
