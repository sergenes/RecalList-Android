package com.nes.recallist.ui.files

import com.google.api.services.drive.model.File
import com.nes.recallist.api.AppAPI
import com.nes.recallist.api.getFiles
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import java.util.ArrayList

class FilesInteractor : FilesViewContract.Interactor {
    override fun getEmail(): String? {
        return AppAPI.singleton().getEmail()
    }

    override fun getFiles(delegate: FilesViewContract.Interactor.OnFinishedListener) {
        GlobalScope.launch {
            // launch new coroutine in background and continue
            AppAPI.singleton().getFiles(onSuccess = { list ->
                val files: MutableList<File> = ArrayList(5)
                list.forEach {
                    if (APP_MIME_TYPE in it.mimeType) {
                        files.add(0, it)
                    }
                }
                delegate.onSuccess(files)
            }, onFailure = {
                delegate.onFailure(it)
            })
        }
    }
}