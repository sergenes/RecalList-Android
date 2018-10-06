package com.nes.recallist.ui.cards

import com.nes.recallist.model.Card
import com.wenchao.cardstack.CardStack

class CardsPresenter(cardsView: CardsViewContract.View,
                     private val interactor: CardsViewContract.Interactor) : CardsViewContract.Presenter,
        CardStack.CardEventListener,
        CardsViewContract.Interactor.OnFinishedListener {


    override var cardsView: CardsViewContract.View? = null
    private var dataAdapter: CardsDataAdapter
    private var speechController:SpeechController

    init {
        this.cardsView = cardsView
        this.dataAdapter = CardsDataAdapter(this)
        this.cardsView?.setDataSource(this.dataAdapter)
        this.speechController = SpeechController(this)
    }

    override fun onResume() {
        val file = cardsView?.getSelectedFile()

        file?.id.let {
            cardsView?.showProgress()

            interactor.getCards(it!!, this)
        }
    }

    override fun onDestroy() {
        speechController.onDetach()
        cardsView = null
    }

    override fun getCard(index: Int): Card {
        return dataAdapter.getItem(index) as Card
    }

    override fun peepPressed(index: Int) {
        val card: Card = dataAdapter.getItem(index) as Card
        card.peeped += 1
        val file = cardsView?.getSelectedFile()

        file?.id.let {
            interactor.updateCard(it!!, card)
        }
    }

    override fun sayPressed(index: Int) {
        val card: Card = dataAdapter.getItem(index) as Card
        speechController.sayOneWord(card)
    }

    override fun playAll(){
        speechController.playSpeech()
    }

    override fun stopAll(){
        speechController.stopSpeech()
    }

    override fun markPressed(index: Int) {

    }

    override fun getCardsCount():Int{
       return dataAdapter.count
    }

    override fun getCurrentSide(): CardSide {
        return dataAdapter.currentSide
    }

    override fun setCurrentSide(side: CardSide) {
        dataAdapter.currentSide = side
        dataAdapter.notifyDataSetChanged()
    }

    override fun onSuccess(items: List<Card>) {
        cardsView?.notifyDataChanged(items)
    }

    override fun onFailure(error: Exception) {
        cardsView?.hideProgress()
    }


    // CardStack.CardEventListener
    override fun swipeStart(section: Int, distance: Float): Boolean {
        return true
    }

    override fun swipeEnd(section: Int, distance: Float): Boolean {
        return (distance > 300)
    }

    override fun topCardTapped() {

    }

    override fun swipeContinue(section: Int, distanceX: Float, distanceY: Float): Boolean {
        return true
    }

    override fun discarded(mIndex: Int, direction: Int) {
        if (mIndex == dataAdapter.count) {
            cardsView?.resetCards()
        }
    }
}