package com.nes.recallist.ui.cards

import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.nes.recallist.model.Card
import java.util.*

enum class SpeechMode {
    ONE_WORD, PLAY_ALL_CARDS, STOPPED
}

enum class PlayWordsTempo(val pause: Long) {
    SIDES(500),
    WORDS(1500)
}

class SpeechController(val presenter: CardsViewContract.Presenter) {
    val TAG = this@SpeechController.javaClass.simpleName

    private var text2Speech: TextToSpeech? = null
    var currentWord: CardSide = CardSide.FRONT
    var currentPlayCard = 0
    var speechMode = SpeechMode.ONE_WORD

    init {
        text2Speech = TextToSpeech(presenter.cardsView?.context(), TextToSpeech.OnInitListener { status ->
            Log.i(TAG, "text2Speech: status=>$status")
            if (status != TextToSpeech.ERROR) {
                text2Speech?.language = Locale.UK
                text2Speech?.setOnUtteranceProgressListener(speechListener)
            }
        })
    }

    fun sayOneWord(card: Card) {
        speechMode = SpeechMode.ONE_WORD

        text2Speech?.language = card.fromLanguage()
        var wordToSay = card.frontVal
        if (presenter.getCurrentSide() == CardSide.BACK) {
            wordToSay = card.backVal
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

    fun sayBothWords(index: Int, cardSide: CardSide) {
        val card: Card = presenter.getCard(index)
//        Log.i(TAG, "card.frontVal:${card.frontVal} index:$index; cardSide:$cardSide")
        var wordToSay = card.frontVal
        if (cardSide == CardSide.FRONT) {
            text2Speech?.language = card.fromLanguage()
        } else {
            wordToSay = card.backVal
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

    fun stopSpeech(){
        speechMode = SpeechMode.STOPPED
        text2Speech?.stop()
    }

    fun playSpeech(){
        speechMode = SpeechMode.PLAY_ALL_CARDS
        currentPlayCard = presenter.cardsView!!.getCurrentCardIndex()
        sayBothWords(currentPlayCard, currentWord)

    }

    fun onDetach() {
        if (text2Speech != null) {
            text2Speech?.stop()
            text2Speech?.shutdown()
        }
    }

    private val speechListener = object : UtteranceProgressListener() {
        override fun onError(utteranceId: String?) {
                text2Speech?.stop()
            Log.i(TAG, "text2Speech: onError2")
        }

        override fun onStart(utteranceId: String?) {
            Log.i(TAG, "text2Speech: onStart")
        }

        override fun onDone(utteranceId: String?) {
            Log.i(TAG, "text2Speech: onDone")
            if (speechMode == SpeechMode.ONE_WORD || speechMode == SpeechMode.STOPPED) {
                return
            }
//            onUiThread {
                if (currentWord == CardSide.FRONT) {
                    currentWord = CardSide.BACK
                    Thread.sleep(PlayWordsTempo.SIDES.pause)
                } else {
                    presenter.cardsView?.showNextCard()
                    currentWord = CardSide.FRONT
                    Thread.sleep(PlayWordsTempo.WORDS.pause)

                    //cardStack bug fix - discarded is not fire in program mode
                    if (currentPlayCard == presenter.getCardsCount() - 1) {
                        presenter.cardsView?.showNextCard()
                        currentPlayCard = 0
                    } else {
                        currentPlayCard += 1
                    }
                }
                sayBothWords(currentPlayCard, currentWord)
//            }

        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            super.onError(utteranceId, errorCode)
            Log.i(TAG, "text2Speech: onError")
//            onUiThread {
                text2Speech?.stop()
//            }
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            super.onStop(utteranceId, interrupted)
            Log.i(TAG, "text2Speech: onStop")
        }
    }
}