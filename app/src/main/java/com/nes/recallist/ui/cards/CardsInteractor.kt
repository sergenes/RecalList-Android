package com.nes.recallist.ui.cards

import android.util.Log
import com.nes.recallist.api.AppAPI
import com.nes.recallist.api.getSheetById
import com.nes.recallist.api.updateSheetWithId
import com.nes.recallist.model.Card
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

class CardsInteractor : CardsViewContract.Interactor {
    val TAG = this@CardsInteractor.javaClass.simpleName

    override fun getCards(fileId: String, delegate: CardsViewContract.Interactor.OnFinishedListener) {
        GlobalScope.launch {
            AppAPI.singleton().getSheetById(fileId, onSuccess = { list ->
                delegate.onSuccess(list)
            }, onFailure = {
                delegate.onFailure(it)
            })
        }
    }

    override fun updateCard(fileId: String, card: Card) {
        GlobalScope.launch {
            AppAPI.singleton().updateSheetWithId(fileId, card, onSuccess = {
                Log.i(TAG, "card.updateSheetWithId: ok")
            }, onFailure = {
                Log.i(TAG, "card.updateSheetWithId: error")
            })
        }
    }
}