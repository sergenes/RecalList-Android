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
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.ValueRange
import com.nes.recallist.BuildConfig
import com.nes.recallist.ui.cards.Card
import java.util.*
import kotlin.concurrent.thread

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
                synchronized(AppAPI::class.java) {
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

    fun getFiles(onSuccess: (MutableList<File>) -> Unit, onFailure: (Exception) -> Unit) {
        val driveAPI = com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                googleAccountCredential)
                .setApplicationName("RecalList-Android")
                .build()
        thread {
            try {
                val response = driveAPI
                        .files()
                        .list()
                        .setFields("files(name,mimeType,id,modifiedTime)")
                        .execute()

                if (response != null) {
                    onSuccess(response.files)
                } else {
                    onFailure(Exception("err:empty response"))
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun updateSheetWithId(spreadsheetId: String, card: Card, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val cardIndex: Int = card.index + 1
        val range = "Phrasebook!E$cardIndex:E$cardIndex"

        val sheetsAPI: Sheets = Sheets.Builder(AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                googleAccountCredential)
                .setApplicationName("RecalList-Android")
                .build()

        //rows[columns[]]
        val values: List<MutableList<Any>> = mutableListOf(mutableListOf(card.peeped as Any))
        val valueRange = ValueRange().setValues(values)

        valueRange.majorDimension = "ROWS"
        valueRange.range = range

        thread {
            try {
                val response = sheetsAPI.spreadsheets().values()
                        .update(spreadsheetId, range, valueRange)
                        .setValueInputOption("RAW")
                        .execute()
                if (response != null) {
                    onSuccess()
                } else {
                    onFailure(Exception("Error:1 - can not update"))
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }


    }

    fun getSheetById(spreadsheetId: String, onSuccess: (List<Card>) -> Unit, onFailure: (Exception) -> Unit) {
        //            val spreadsheetId = "1apdhKnDAO1gERYc867XDspl8DFKRyeVPRWqG6aM50Sg"
        val range = "Phrasebook!A1:E"

        val sheetsAPI: Sheets = Sheets.Builder(AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                googleAccountCredential)
                .setApplicationName("RecalList-Android")
                .build()

        thread {
            try {
                val response = sheetsAPI.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute()

                val array: List<Card> = response.getValues().mapIndexed { index, value
                    ->
                    val item = value as ArrayList<*>
                    Card(index, item)

                }.toList()



                onSuccess(array.sortedWith(compareByDescending {
                    it.peeped
                }))
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}