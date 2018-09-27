package com.nes.recallist.ui.init


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.nes.recallist.R
import com.nes.recallist.api.AppAPI
import com.nes.recallist.ui.MainActivity
import com.nes.transfragment.BaseTransFragment
import org.jetbrains.anko.support.v4.onUiThread
import kotlin.concurrent.thread


/**
 * A simple [Fragment] subclass.
 *
 */
class LaunchFragment : BaseTransFragment() {

    override fun getFragmentContainer(): Int {
        return R.id.fragmentContainer
    }

    override fun backEnabled(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_launch, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val act: LaunchActivity = activity as LaunchActivity

        thread {
            Thread.sleep(2000)
            if (AppAPI.singleton().isLoggedIn()) {
                AppAPI.singleton().silentSignIn(onSuccess = {
                    onUiThread {
                        val intent = Intent(activity, MainActivity::class.java)
                        // start your next activity
                        startActivity(intent)
                        activity?.finish()
                    }
                }, onFailure = {
                    onUiThread {
                        forwardToFragment(LoginFragment())
                    }
                })
            } else {
                onUiThread {
                    forwardToFragment(LoginFragment())
                }
            }
        }
    }

}
