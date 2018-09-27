package com.nes.recallist.ui.init


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.nes.recallist.R
import com.nes.recallist.api.AppAPI
import com.nes.recallist.ui.init.LaunchActivity.Companion.RQ_GOOGLE_SIGN_IN
import com.nes.transfragment.BaseTransFragment
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * A simple [Fragment] subclass.
 *
 */
class LoginFragment : BaseTransFragment() {

    override fun getFragmentContainer(): Int {
        return R.id.fragmentContainer
    }

    override fun backEnabled(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton.setOnClickListener {
            startActivityForResult(AppAPI.singleton().getAuthActivity(), RQ_GOOGLE_SIGN_IN)
        }

    }
}
