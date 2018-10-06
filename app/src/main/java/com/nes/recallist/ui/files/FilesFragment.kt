package com.nes.recallist.ui.files


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.api.services.drive.model.File

import com.nes.recallist.R
import com.nes.recallist.ui.MainActivity
import com.nes.recallist.ui.cards.CardsFragment
import com.nes.transfragment.BaseTransFragment
import kotlinx.android.synthetic.main.fragment_files.*
import org.jetbrains.anko.support.v4.onUiThread

const val APP_MIME_TYPE = "application/vnd.google-apps.spreadsheet"

/**
 * A simple [Fragment] subclass.
 *
 */
class FilesFragment : BaseTransFragment(), FilesViewContract.View {

    private lateinit var filesPresenter: FilesPresenter

    override fun setEmail(email: String?) {
        emailText.text = "unknown"
        email.let {
            emailText.text = it
        }
    }

    override fun setDataSource(filesAdapter: FilesListAdapter) {
        filesListView!!.adapter = filesAdapter
    }

    override fun notifyDataChanged(items: List<File>) {
        onUiThread {
            with(filesListView!!.adapter as FilesListAdapter){
                items.forEach {
                    files.add(0, it)
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun navigateToCards(selectedFile: File) {
        val act: MainActivity = activity as MainActivity
        act.selectedFile = selectedFile
        forwardToFragment(CardsFragment())
    }

    override fun showProgress() {
        onUiThread {
            super.showProgress()
        }
    }

    override fun hideProgress() {
        onUiThread {
            super.hideProgress()
        }
    }


    override fun getFragmentContainer(): Int {
        return R.id.fragmentContainer
    }

    override fun backEnabled(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filesPresenter = FilesPresenter(this, FilesListAdapter(context!!), FilesInteractor())

        filesListView!!.onItemClickListener = filesPresenter

        filesPresenter.onResume()

    }

    override fun onDetach() {
        filesPresenter.onDestroy()
        super.onDetach()
    }
}
