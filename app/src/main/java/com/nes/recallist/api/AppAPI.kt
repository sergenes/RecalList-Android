package com.nes.recallist.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.SheetsScopes
import com.nes.recallist.BuildConfig
import java.util.*

abstract class API<Result> {

}

class AppAPI private constructor(var context: Context) {
    companion object {
        private const val TAG = "RecalList - AppAPI"
        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE)

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var singleton: AppAPI? = null

        fun singleton(): AppAPI {
            return if (singleton == null) {
                throw IllegalStateException("Must Initialize AppAPI before using singleton()")
            } else {
                singleton!!
            }
        }

        fun with(context: Context): AppAPI {
            if (singleton == null) {
                val var1 = AppAPI::class.java
                synchronized(var1) {
                    if (singleton == null) {
                        singleton = AppAPI(context)
                        singleton?.init()
                    }
                }
            }

            return singleton!!
        }
    }


    private var googleSignInClient: GoogleSignInClient? = null
    private var googleAccountCredential: GoogleAccountCredential? = null

    fun getCredentials(): GoogleAccountCredential {
        return googleAccountCredential!!
    }

    private fun init() {
        val signInOptions: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Scope(SheetsScopes.DRIVE),
                                Scope(SheetsScopes.SPREADSHEETS),
                                Scope(DriveScopes.DRIVE_READONLY),
                                Scope(DriveScopes.DRIVE_FILE),
                                Scope(DriveScopes.DRIVE_METADATA_READONLY))
                        .requestEmail()
                        .requestId()
                        .requestIdToken(BuildConfig.ApiKey)
                        .build()

        googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
        googleAccountCredential = GoogleAccountCredential
                .usingOAuth2(context, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())


    }

    private fun getLastSignedAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getEmail(): String? {
        return getLastSignedAccount()?.email
    }

    fun getAuthActivity(): Intent? {
        return googleSignInClient?.signInIntent
    }

    fun isLoggedIn(): Boolean {
        getLastSignedAccount()?.let { return true }
        return false
    }

    fun revoke() {
        Log.d(TAG, "revokeAccess=>")
        googleSignInClient?.revokeAccess()?.continueWith {

            Log.d(TAG, "revokeAccess2=>" + it.toString())
        }?.addOnFailureListener {
            Log.d(TAG, "revokeAccess3=>" + it.localizedMessage)
        }
    }

    fun silentSignIn(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        googleSignInClient?.silentSignIn()?.addOnSuccessListener { _ ->
            googleAccountCredential?.selectedAccount = getLastSignedAccount()?.account
            googleAccountCredential?.selectedAccountName = getLastSignedAccount()?.account?.name
            onSuccess()
        }?.addOnFailureListener {
            Log.d(TAG, it.localizedMessage)
            onFailure(it)
//            startActivityForResult(authManager?.googleSignInClient?.signInIntent, LaunchActivity.RQ_GOOGLE_SIGN_IN)
        }
    }
}