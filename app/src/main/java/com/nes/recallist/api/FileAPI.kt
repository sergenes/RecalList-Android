package com.nes.recallist.api

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.model.File
import kotlin.concurrent.thread


fun AppAPI.getFiles(onSuccess: (MutableList<File>) -> Unit, onFailure: (Exception) -> Unit) {
    val driveAPI = com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            getCredentials())
            .setApplicationName("RecalList-Android")
            .build()
    thread {
        try {
            val response = driveAPI
                    .files()
                    .list()
                    .setFields("files(name,mimeType,id,modifiedTime)")
                    .execute()

            if (response != null) {
                onSuccess(response.files)
            } else {
                onFailure(Exception("err:empty response"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}