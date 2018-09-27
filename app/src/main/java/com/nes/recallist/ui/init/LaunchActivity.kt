package com.nes.recallist.ui.init

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log

import com.nes.recallist.R
import com.nes.recallist.api.AppAPI
import com.nes.recallist.ui.MainActivity
import com.nes.transfragment.BaseTransActivity


class LaunchActivity : BaseTransActivity() {

    companion object {
        const val RQ_GOOGLE_SIGN_IN = 999
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult=>$resultCode")
        val activity = this
//        if (requestCode == RQ_GOOGLE_SIGN_IN) {
            if (AppAPI.singleton().isLoggedIn()) {
                AppAPI.singleton().silentSignIn(onSuccess = {
                    runOnUiThread {
                        val intent = Intent(activity, MainActivity::class.java)
                        // start your next activity
                        startActivity(intent)
                        activity.finish()
                    }
                }, onFailure = {
                    runOnUiThread {
                        //todo error message
                    }
                })
//            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, LaunchFragment())
                .commit()
    }

    fun isGetAccountPermissionGranted(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
                    && activity.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {

                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS), 1)
                return false
            }


        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }


    }

    fun isPermissionGranted(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            return (activity.checkSelfPermission(android.Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
                    && activity.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }


    }
}
