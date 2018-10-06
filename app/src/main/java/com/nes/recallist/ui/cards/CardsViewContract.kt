package com.nes.recallist.ui.cards

import android.content.Context
import com.google.api.services.drive.model.File
import com.nes.recallist.model.Card

interface CardsViewContract {
    interface View {
        fun context(): Context

        fun showProgress()

        fun hideProgress()

        fun getSelectedFile(): File?

        fun setDataSource(cardsAdapter: CardsDataAdapter)

        fun notifyDataChanged(items: List<Card>)

        fun resetCards()

        fun getCurrentCardIndex():Int

        fun showNextCard()
    }

    interface Presenter {
        var cardsView: CardsViewContract.View?

        fun getCurrentSide(): CardSide

        fun setCurrentSide(side: CardSide)

        fun onResume()

        fun onDestroy()

        fun getCard(index: Int):Card

        fun getCardsCount():Int

        fun peepPressed(index: Int)

        fun sayPressed(index: Int)

        fun markPressed(index: Int)

        fun playAll()

        fun stopAll()
    }

    interface Interactor {
        fun updateCard(fileId: String, card: Card)

        fun getCards(fileId: String, delegate: OnFinishedListener)

        interface OnFinishedListener {
            fun onSuccess(items: List<Card>)
            fun onFailure(error: Exception)
        }
    }
}