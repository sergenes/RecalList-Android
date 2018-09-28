package com.nes.recallist.ui.cards


import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nes.recallist.R
import com.nes.recallist.api.AppAPI
import com.nes.recallist.ui.MainActivity

import com.nes.recallist.ui.files.FilesFragment
import com.nes.transfragment.BaseTransFragment
import com.wenchao.cardstack.CardStack
import kotlinx.android.synthetic.main.fragment_cards.*
import org.jetbrains.anko.support.v4.onUiThread
import android.speech.tts.TextToSpeech
import java.util.*
import android.widget.Toast




interface CardsScreenProtocol {
    fun context(): Context
    fun peepPressed(index: Int)
    fun sayPressed(index: Int)
    fun markPressed(index: Int)
}

// MARK: - Card is data model
class Card(var index: Int, item: ArrayList<*>) {

    var word: String = item[2] as String
    var translation: String = item[3] as String
    var from: String = item[0] as String
    var to: String = item[1] as String
    var peeped: Int = if (item.size == 5)
        (item[4] as String).toInt()
    else
        0
}

/**
 * A simple [Fragment] subclass.
 *
 */
class CardsFragment : BaseTransFragment(), CardStack.CardEventListener, CardsScreenProtocol {

    //CardsScreenProtocol
    override fun context(): Context {
        return context!!
    }

    override fun markPressed(index: Int){

    }

    override fun peepPressed(index: Int) {
        val card: Card = cardStack.adapter?.getItem(index) as Card
        Log.i(TAG, "card.word: " + card.word)
    }

    override fun sayPressed(index: Int) {
        val card: Card = cardStack.adapter?.getItem(index) as Card
        when {
            "Russian" in card.from -> {
                t1?.language = Locale("ru", "RU")
            }
            "Hebrew" in card.from -> {
                t1?.language = Locale("he", "IL")
            }
            else -> {
                t1?.language = Locale.UK
            }
        }
        val toSpeak = card.word
        Toast.makeText(activity, toSpeak, Toast.LENGTH_SHORT).show()
        t1?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)
    }

    private var dataAdapter: CardsDataAdapter? = null
    private var t1: TextToSpeech? = null

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

        dataAdapter = CardsDataAdapter(this)

        t1 = TextToSpeech(activity, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                t1?.language = Locale.UK
            }
        })

        val act: MainActivity = activity as MainActivity

        titleTextView.text = act.selectedFile?.name

        act.selectedFile?.id.let { selectedFileId ->
            AppAPI.singleton().getSheetById(selectedFileId!!, onSuccess = { list ->
                onUiThread {
                    list.forEach {
                        dataAdapter?.add(it)
                    }
                    bottomToolBarLayout.visibility = View.VISIBLE
                    leftRadioButton.text = dataAdapter?.getItem(0)?.from
                    rightRadioButton.text = dataAdapter?.getItem(0)?.to
                    hideProgress()
                }

            }, onFailure = {
                Log.d("Auth", it.localizedMessage)
                onUiThread {
                    hideProgress()
                }
            })
            cardStack.adapter = dataAdapter
            cardStack.setListener(this)
            cardStack.setVisibleCardNum(5)
            cardStack.setStackMargin(-150)

            if (cardStack.adapter != null) {
                Log.i(TAG, "Card Stack size: " + cardStack.adapter?.count)
            }
        }
        showProgress()

        playButton.setOnClickListener {
            cardStack.discardTop(1)

            //cardStack bug fix - discarded is not fire in program mode
            if (cardStack.currIndex == cardStack.adapter.count - 1) {
                cardStack.discardTop(1)
            }
        }

        leftRadioButton.setOnClickListener {
            dataAdapter?.direction = 0
            dataAdapter?.notifyDataSetChanged()
        }

        rightRadioButton.setOnClickListener {
            dataAdapter?.direction = 1
            dataAdapter?.notifyDataSetChanged()
        }
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
        if (mIndex == cardStack!!.adapter.count) {
            cardStack!!.reset(true)
        }
    }

    override fun onDetach() {
        if(t1 !=null){
            t1?.stop()
            t1?.shutdown()
        }
        super.onDetach()
    }
}
