package com.nes.recallist.ui.cards


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nes.recallist.R
import com.nes.recallist.ui.MainActivity

import com.nes.recallist.ui.files.FilesFragment
import com.nes.transfragment.BaseTransFragment
import kotlinx.android.synthetic.main.fragment_cards.*
import org.jetbrains.anko.support.v4.onUiThread

import android.widget.Button
import com.google.api.services.drive.model.File
import com.nes.recallist.model.Card

enum class CardSide {
    FRONT, BACK
}

/**
 * A simple [Fragment] subclass.
 *
 */
class CardsFragment : BaseTransFragment(), CardsViewContract.View {

    //CardsViewContract.View
    override fun context(): Context {
        return context!!
    }

    //CardsViewContract.getSelectedFile
    override fun getSelectedFile(): File? {
        val act: MainActivity = activity as MainActivity

        return act.selectedFile
    }

    //CardsViewContract.showProgress
    override fun showProgress() {
        onUiThread {
            super.showProgress()
        }
    }

    //CardsViewContract.hideProgress
    override fun hideProgress() {
        onUiThread {
            super.hideProgress()
        }
    }

    //CardsViewContract.setDataSource
    override fun setDataSource(cardsAdapter: CardsDataAdapter) {
        cardStack.adapter = cardsAdapter
    }

    //CardsViewContract.notifyDataChanged
    override fun notifyDataChanged(items: List<Card>) {
        with(cardStack.adapter as CardsDataAdapter) {
            items.forEach {
                onUiThread {
                    add(it)
                }
            }
            onUiThread {
                notifyDataSetChanged()
                leftRadioButton.text = getItem(0)?.from
                rightRadioButton.text = getItem(0)?.to
                bottomToolBarLayout.visibility = View.VISIBLE
            }
        }

        hideProgress()
    }

    //CardsViewContract.resetCards
    override fun resetCards() {
        onUiThread {
            cardStack!!.reset(true)
        }
    }

    override fun getCurrentCardIndex():Int{
        return cardStack.currIndex
    }

    companion object {
        const val VISIBLE_CARD_NUMBER = 5
        const val PLAY_BUTTON_TAG = 100
        const val STOP_BUTTON_TAG = 200
    }

    lateinit var cardsPresenter: CardsPresenter

    override fun getFragmentContainer(): Int {
        return R.id.fragmentContainer
    }

    override fun getBackFragmentClass(): Class<*> {
        return FilesFragment::class.java
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomToolBarLayout.visibility = View.INVISIBLE

        cardsPresenter = CardsPresenter(this, CardsInteractor())
        cardStack.setListener(cardsPresenter)
        cardStack.setVisibleCardNum(VISIBLE_CARD_NUMBER)

        val act: MainActivity = activity as MainActivity
        titleTextView.text = act.selectedFile?.name

        playButton.setOnClickListener {
            if (it.tag != PLAY_BUTTON_TAG) {
                it.tag = PLAY_BUTTON_TAG
                (it as Button).text = getString(R.string.stop_button)
                cardsPresenter.playAll()
            } else {
                it.tag = STOP_BUTTON_TAG
                (it as Button).text = getString(R.string.play_button)
                cardsPresenter.stopAll()
            }
        }

        leftRadioButton.setOnClickListener {
            cardsPresenter.setCurrentSide(CardSide.FRONT)
        }

        rightRadioButton.setOnClickListener {
            cardsPresenter.setCurrentSide(CardSide.BACK)
        }

        cardsPresenter.onResume()
    }

    override fun showNextCard(){
        onUiThread {
            cardStack.discardTop(1)
        }
    }


    override fun onDetach() {
//        speechController.onDetach()
        cardsPresenter.onDestroy()
        super.onDetach()
    }
}
