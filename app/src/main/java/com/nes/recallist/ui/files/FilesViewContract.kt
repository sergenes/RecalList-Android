package com.nes.recallist.ui.files

import com.google.api.services.drive.model.File

interface FilesViewContract {
    interface View {
        fun showProgress()

        fun hideProgress()

        fun setEmail(email: String?)

        fun setDataSource(filesAdapter: FilesListAdapter)

        fun notifyDataChanged()

        fun navigateToCards(selectedFile: File)
    }

    interface Presenter {

        fun onResume()

        fun onItemClicked(position: Int)

        fun onDestroy()
    }

}