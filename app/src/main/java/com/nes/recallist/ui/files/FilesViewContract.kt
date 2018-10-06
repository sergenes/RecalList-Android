package com.nes.recallist.ui.files

import com.google.api.services.drive.model.File

interface FilesViewContract {
    interface View {
        fun showProgress()

        fun hideProgress()

        fun setEmail(email: String?)

        fun setDataSource(filesAdapter: FilesListAdapter)

        fun notifyDataChanged(items: List<File>)

        fun navigateToCards(selectedFile: File)
    }

    interface Presenter {

        fun onResume()

        fun onItemClicked(position: Int)

        fun onDestroy()
    }

    interface Interactor {
        fun getEmail(): String?
        fun getFiles(delegate: OnFinishedListener)

        interface OnFinishedListener {
            fun onSuccess(items: MutableList<File>)
            fun onFailure(error: Exception)
        }
    }

}