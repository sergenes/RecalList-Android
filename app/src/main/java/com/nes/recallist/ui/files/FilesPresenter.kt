package com.nes.recallist.ui.files

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import com.google.api.services.drive.model.File


class FilesPresenter(filesView: FilesViewContract.View,
                     private val filesAdapter: FilesListAdapter,
                     private val filesInteractor: FilesInteractor) : FilesViewContract.Presenter,
        OnItemClickListener, FilesViewContract.Interactor.OnFinishedListener {

    var filesView: FilesViewContract.View? = null
        private set

    init {
        this.filesView = filesView
        this.filesView?.setDataSource(filesAdapter)
    }

    //FilesViewContract.Presenter.onResume
    override fun onResume() {
        filesView?.showProgress()

        filesInteractor.getFiles(this)
        filesView?.setEmail(filesInteractor.getEmail())
    }

    //FilesViewContract.Presenter.onDestroy
    override fun onDestroy() {
        filesView = null
    }

    //OnItemClickListener
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        filesAdapter.selected = position
        filesView?.navigateToCards(filesAdapter.files[position])
    }

    override fun onItemClicked(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // FilesViewContract.Interactor.OnFinishedListener
    override fun onSuccess(items: MutableList<File>) {
        val sortedList = items.asSequence().sortedWith(compareBy {
            it.modifiedTime.value
        }).toList()
        filesView?.notifyDataChanged(sortedList)
        filesView?.hideProgress()
    }

    override fun onFailure(error: Exception) {
        Log.d("Auth", error.localizedMessage)
        filesView?.hideProgress()
    }

}