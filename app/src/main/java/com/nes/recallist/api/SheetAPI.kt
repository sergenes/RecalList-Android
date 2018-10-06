package com.nes.recallist.api

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.nes.recallist.model.Card
import java.util.ArrayList
import kotlin.concurrent.thread

fun AppAPI.updateSheetWithId(spreadsheetId: String, card: Card, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val cardIndex: Int = card.index + 1
    val range = "Phrasebook!E$cardIndex:E$cardIndex"

    val sheetsAPI: Sheets = Sheets.Builder(AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            getCredentials())
            .setApplicationName("RecalList-Android")
            .build()

    //rows[columns[]]
    val values: List<MutableList<Any>> = mutableListOf(mutableListOf(card.peeped as Any))
    val valueRange = ValueRange().setValues(values)

    valueRange.majorDimension = "ROWS"
    valueRange.range = range

    thread {
        try {
            val response = sheetsAPI.spreadsheets().values()
                    .update(spreadsheetId, range, valueRange)
                    .setValueInputOption("RAW")
                    .execute()
            if (response != null) {
                onSuccess()
            } else {
                onFailure(Exception("Error:1 - can not update"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }


}

fun AppAPI.getSheetById(spreadsheetId: String, onSuccess: (List<Card>) -> Unit, onFailure: (Exception) -> Unit) {
    //val spreadsheetId = "1apdhKnDAO1gERYc867XDspl8DFKRyeVPRWqG6aM50Sg"
    val range = "Phrasebook!A1:E"

    val sheetsAPI: Sheets = Sheets.Builder(AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            getCredentials())
            .setApplicationName("RecalList-Android")
            .build()

    thread {
        try {
            val response = sheetsAPI.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()

            val array: List<Card> = response.getValues().mapIndexed { index, value
                ->
                val item = value as ArrayList<*>
                Card(index, item)

            }.toList()



            onSuccess(array.sortedWith(compareByDescending {
                it.peeped
            }))
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}