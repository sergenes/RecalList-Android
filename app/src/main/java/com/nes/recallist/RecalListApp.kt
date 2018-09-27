package com.nes.recallist

import android.support.multidex.MultiDexApplication
import com.nes.recallist.api.AppAPI

open class RecalListApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AppAPI.with(this)
    }
}