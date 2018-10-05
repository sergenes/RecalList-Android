package com.nes.recallist.ui.files


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.nes.recallist.R
import com.nes.recallist.api.AppAPI
import com.nes.recallist.api.getFiles
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
class FilesFragment : BaseTransFragment() {


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

        val filesAdapter = FilesListAdapter(context!!)

        val act: MainActivity = activity as MainActivity

        emailText.text = AppAPI.singleton().getEmail()
        showProgress()

        AppAPI.singleton().getFiles(onSuccess = { list ->
            list.forEach {
                if (APP_MIME_TYPE in it.mimeType) {
                    Log.d("test", "response=> ${it.toPrettyString()}")
                    filesAdapter.files.add(0, it)
                }
            }
            onUiThread {
                filesAdapter.notifyDataSetChanged()
                hideProgress()
            }

        }, onFailure = {
            Log.d("Auth", it.localizedMessage)
            onUiThread {
                hideProgress()
            }
        })

        filesListView!!.adapter = filesAdapter

        filesListView!!.setOnItemClickListener { parent, view, position, id ->
            (filesListView!!.adapter as FilesListAdapter).selected = position
            act.selectedFile = (filesListView!!.adapter as FilesListAdapter).files[position]
            forwardToFragment(CardsFragment())
        }

    }

}
