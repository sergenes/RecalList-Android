package com.nes.recallist.ui.cards

import android.animation.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.nes.recallist.R
import com.nes.recallist.tools.getExtDrawable
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.find
import android.view.View.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator


class CardsDataAdapter(private var controller: CardsScreenProtocol) :
        ArrayAdapter<Card>(controller.context(), 0) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var distance = 10000
    var scale = context.resources.displayMetrics.density * distance

    private fun flipForward(from: View, to: View) {
        val frontAnim = ObjectAnimator.ofFloat(from, "rotationX", 0.0f, 90.0f)
        val backAnim = ObjectAnimator.ofFloat(to, "rotationX", -90.0f, 0f)
        frontAnim.interpolator = DecelerateInterpolator()
        backAnim.interpolator = AccelerateDecelerateInterpolator()
        frontAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                to.visibility = GONE
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                from.visibility = GONE

                backAnim.start()
            }
        })
        backAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                to.visibility = VISIBLE

            }
        })
        backAnim.duration = 400
        frontAnim.duration = 400
        frontAnim.start()
    }

    private fun flipBackward(from: View, to: View) {
        val backAnim = ObjectAnimator.ofFloat(from, "rotationX", 0f, -90.0f)
        val frontAnim = ObjectAnimator.ofFloat(to, "rotationX", 90.0f, 0f)
        backAnim.interpolator = DecelerateInterpolator()
        frontAnim.interpolator = AccelerateDecelerateInterpolator()
        backAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                to.visibility = GONE
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                from.visibility = GONE

                frontAnim.start()
            }
        })
        frontAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                super.onAnimationStart(animation, isReverse)
                to.visibility = VISIBLE

            }
        })
        frontAnim.duration = 400
        backAnim.duration = 400
        backAnim.start()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val holder: CardViewHolder

        if (convertView == null || convertView.tag !is View) {
            view = this.inflater.inflate(R.layout.card_view, parent, false)
            holder = CardViewHolder(view, scale)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as CardViewHolder
        }

        val card = this.getItem(position)
        holder.setup(card!!, position, count)

        if (!holder.sayButton.hasOnClickListeners()) {
            holder.sayButton.setOnClickListener {
                controller.sayPressed(it.tag as Int)
            }
        }

//        if (!holder.peepButton.hasOnClickListeners()) {
        holder.peepButton.setOnClickListener {
            controller.peepPressed(it.tag as Int)
            flipForward(holder.cardFrontContainer, holder.cardBackContainer)
        }
//        }

//        if (!holder.backButton.hasOnClickListeners()) {
        holder.backButton.setOnClickListener {
            flipBackward(holder.cardBackContainer, holder.cardFrontContainer)
        }
//        }

        if (position % 2 == 0) {
            holder.cardContainer.background = null
            holder.cardFrontContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_blue)
            holder.cardBackContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_blue)
        } else {
            holder.cardContainer.background = null
            holder.cardFrontContainer.background = context.getExtDrawable(R.drawable.rnd_bg_purple)
            holder.cardBackContainer.background = context.getExtDrawable(R.drawable.rnd_bg_purple)
        }

        return view!!
    }
}

private class CardViewHolder(row: View, scale: Float) {
    var wordTextView: TextView = row.find(R.id.wordTextView)
    var textNumber: TextView = row.find(R.id.textNumber)
    var cardFrontContainer: FrameLayout = row.find(R.id.cardFrontContainer)

    var peepButton: Button = row.find(R.id.peepButton)
    var sayButton: Button = row.find(R.id.sayButton)

    var translateTextView: TextView = row.find(R.id.translateTextView)
    var cardBackContainer: FrameLayout = row.find(R.id.cardBackContainer)
    var backButton: Button = row.find(R.id.backButton)
    var markButton: Button = row.find(R.id.markButton)

    var cardContainer: FrameLayout = row.find(R.id.cardContainer)

    fun setup(card:Card, position:Int, count:Int){
        val label: String = String.format("%s of %s", (position + 1), count)
        wordTextView.text = card.word
        textNumber.text = label
        translateTextView.text = card.translation
        peepButton.tag = position
        backButton.tag = position
        sayButton.tag = position
    }

    init {
        cardFrontContainer.cameraDistance = scale
        cardBackContainer.cameraDistance = scale
    }
}