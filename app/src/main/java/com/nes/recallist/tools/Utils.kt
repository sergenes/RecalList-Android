package com.nes.recallist.tools

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build

fun Context.getExtDrawable(resId: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getDrawable(resId)
    } else {
        resources.getDrawable(resId)
    }
}