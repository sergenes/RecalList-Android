package com.nes.recallist.ui.cards


import android.content.Context
import android.os.Build
import android.os.Bundle
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
import android.speech.tts.UtteranceProgressListener
import android.widget.Button
import com.nes.recallist.ui.cards.Card.Companion.PLAY_BUTTON_TAG
import com.nes.recallist.ui.cards.Card.Companion.STOP_BUTTON_TAG
import com.nes.recallist.ui.cards.Card.Companion.VISIBLE_CARD_NUMBER
import java.util.*

enum class Direction {
    FRONT, BACK
}

enum class SpeechMode {
    ONE_WORD, PLAY_ALL_CARDS
}

enum class PlayWordsTempo(val pause: Long) {
    SIDES(500),
    WORDS(1500)
}

interface CardsScreenProtocol {
    fun context(): Context
    fun peepPressed(index: Int)
    fun sayPressed(index: Int)
    fun markPressed(index: Int)
}

// MARK: - Card is data model
class Card(var index: Int, item: ArrayList<*>) {

    companion object {
        const val VISIBLE_CARD_NUMBER = 5
        const val PLAY_BUTTON_TAG = 100
        const val STOP_BUTTON_TAG = 200
    }

    var word: String = item[2] as String
    var translation: String = item[3] as String
    var from: String = item[0] as String
    var to: String = item[1] as String
    var peeped: Int = if (item.size == 5)
        (item[4] as String).toInt()
    else
        0

    fun fromLanguage(): Locale {
        return when {
            "Russian" in from -> {
                Locale("ru", "RU")
            }
            "Hebrew" in from -> {
                Locale("he", "IL")
            }
            else -> Locale.UK
        }
    }

    fun toLanguage(): Locale {
        return when {
            "Russian" in to -> {
                Locale("ru", "RU")
            }
            "Hebrew" in to -> {
                Locale("he", "IL")
            }
            else -> Locale.UK
        }
    }
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

    override fun markPressed(index: Int) {

    }

    override fun peepPressed(index: Int) {
        val card: Card = cardStack.adapter?.getItem(index) as Card
        Log.i(TAG, "card.word: " + card.word)
    }


    override fun sayPressed(index: Int) {
        speechMode = SpeechMode.ONE_WORD
        val card: Card = cardStack.adapter?.getItem(index) as Card

        text2Speech?.language = card.fromLanguage()
        var wordToSay = card.word
        if (dataAdapter?.direction == Direction.BACK) {
            wordToSay = card.translation
            text2Speech?.language = card.toLanguage()
        }
//        Toast.makeText(activity, toSpeak, Toast.LENGTH_SHORT).show()
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            text2Speech?.speak(wordToSay, TextToSpeech.QUEUE_FLUSH, params, "com.nes.StudyCards")
        } else {
            text2Speech?.speak(wordToSay, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    fun sayBothWords(index: Int, direction: Direction) {
        val card: Card = cardStack.adapter?.getItem(index) as Card
        Log.i(TAG, "card.word:${card.word} index:$index; direction:$direction" )
        var wordToSay = card.word
        if (direction == Direction.FRONT) {
            text2Speech?.language = card.fromLanguage()
        } else {
            wordToSay = card.translation
            text2Speech?.language = card.toLanguage()
        }

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            text2Speech?.speak(wordToSay, TextToSpeech.QUEUE_FLUSH, params, "com.nes.StudyCards")
        } else {
            text2Speech?.speak(wordToSay, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    private var dataAdapter: CardsDataAdapter? = null
    private var text2Speech: TextToSpeech? = null
    var currentWord: Direction = Direction.FRONT
    var currentPlayCard = 0
    var speechMode = SpeechMode.ONE_WORD

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

        text2Speech = TextToSpeech(activity, TextToSpeech.OnInitListener { status ->
            Log.i(TAG, "text2Speech: status=>$status")
            if (status != TextToSpeech.ERROR) {
                text2Speech?.language = Locale.UK
                text2Speech?.setOnUtteranceProgressListener(speechListener)
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
            cardStack.setVisibleCardNum(VISIBLE_CARD_NUMBER)

            if (cardStack.adapter != null) {
                Log.i(TAG, "Card Stack size: " + cardStack.adapter?.count)
            }
        }
        showProgress()

        playButton.setOnClickListener {
            speechMode = SpeechMode.PLAY_ALL_CARDS
            currentPlayCard = cardStack.currIndex
            if (it.tag != PLAY_BUTTON_TAG) {
                it.tag = PLAY_BUTTON_TAG
                (it as Button).text = getString(R.string.stop_button)
                sayBothWords(currentPlayCard, currentWord)
            } else {
                it.tag = STOP_BUTTON_TAG
                (it as Button).text = getString(R.string.play_button)
                text2Speech?.stop()
            }
        }

        leftRadioButton.setOnClickListener {
            dataAdapter?.direction = Direction.FRONT
            dataAdapter?.notifyDataSetChanged()
        }

        rightRadioButton.setOnClickListener {
            dataAdapter?.direction = Direction.BACK
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
        if (text2Speech != null) {
            text2Speech?.stop()
            text2Speech?.shutdown()
        }
        super.onDetach()
    }

    private val speechListener = object : UtteranceProgressListener() {
        override fun onError(utteranceId: String?) {
            onUiThread {
                text2Speech?.stop()
            }
            Log.i(TAG, "text2Speech: onError2")
        }

        override fun onStart(utteranceId: String?) {
            Log.i(TAG, "text2Speech: onStart")
        }

        override fun onDone(utteranceId: String?) {
            Log.i(TAG, "text2Speech: onDone")
            if (speechMode == SpeechMode.ONE_WORD){
                return
            }
            onUiThread {
                if (currentWord == Direction.FRONT) {
                    currentWord = Direction.BACK
                    Thread.sleep(PlayWordsTempo.SIDES.pause)
                } else {
                    cardStack.discardTop(1)
                    currentWord = Direction.FRONT
                    Thread.sleep(PlayWordsTempo.WORDS.pause)

                    //cardStack bug fix - discarded is not fire in program mode
                    if (currentPlayCard == cardStack.adapter.count - 1) {
                        cardStack.discardTop(1)
                        currentPlayCard = 0
                    }else {
                        currentPlayCard += 1
                    }
                }
                sayBothWords(currentPlayCard, currentWord)
            }

        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            super.onError(utteranceId, errorCode)
            Log.i(TAG, "text2Speech: onError")
            onUiThread {
                text2Speech?.stop()
            }
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            super.onStop(utteranceId, interrupted)
            Log.i(TAG, "text2Speech: onStop")
        }
    }
}
