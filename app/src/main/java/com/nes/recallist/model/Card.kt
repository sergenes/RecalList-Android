package com.nes.recallist.model

import java.util.*

// MARK: - Card is data model
class Card(var index: Int, item: ArrayList<*>) {

    var frontVal: String = item[2] as String
    var backVal: String = item[3] as String
    var from: String = item[0] as String
    var to: String = item[1] as String
    var peeped: Int = if (item.size == 5)
        (item[4] as String).toInt()
    else
        0

    fun fromLanguage(): Locale {
        return when {
            "Russian" in from -> {
                Locale("ru", "RU")
            }
            "Hebrew" in from -> {
                Locale("he", "IL")
            }
            else -> Locale.UK
        }
    }

    fun toLanguage(): Locale {
        return when {
            "Russian" in to -> {
                Locale("ru", "RU")
            }
            "Hebrew" in to -> {
                Locale("he", "IL")
            }
            else -> Locale.UK
        }
    }
}