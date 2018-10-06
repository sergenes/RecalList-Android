package com.nes.recallist.ui.files

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import com.nes.recallist.api.AppAPI
import com.nes.recallist.api.getFiles
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch


class FilesPresenter(filesView: FilesViewContract.View, private val filesAdapter: FilesListAdapter) : FilesViewContract.Presenter, OnItemClickListener {

    var filesView: FilesViewContract.View? = null
        private set

    init {
        this.filesView = filesView
        this.filesView?.setDataSource(filesAdapter)
    }

    override fun onResume() {
        filesView?.showProgress()
        filesView?.setEmail(AppAPI.singleton().getEmail())
        GlobalScope.launch {
            // launch new coroutine in background and continue
            AppAPI.singleton().getFiles(onSuccess = { list ->
                list.forEach {
                    if (APP_MIME_TYPE in it.mimeType) {
                        Log.d("test", "response=> ${it.toPrettyString()}")
                        filesAdapter.files.add(0, it)
                    }
                }
                filesView?.hideProgress()
                filesView?.notifyDataChanged()
            }, onFailure = {
                Log.d("Auth", it.localizedMessage)
                filesView?.hideProgress()
            })
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        filesAdapter.selected = position
        filesView?.navigateToCards(filesAdapter.files[position])
    }

    override fun onItemClicked(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        filesView = null
    }

}