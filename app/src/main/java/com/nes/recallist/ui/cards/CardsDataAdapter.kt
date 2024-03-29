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
import com.nes.recallist.model.Card


class CardsDataAdapter(private var controller: CardsViewContract.Presenter) :
        ArrayAdapter<Card>(controller.cardsView?.context()!!, 0) {

    companion object {
        const val FLIP_ANIMATION_SPEED: Long = 400
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var currentSide = CardSide.FRONT
    val distance = 10000
    private val scale = context.resources.displayMetrics.density * distance

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
        backAnim.duration = FLIP_ANIMATION_SPEED
        frontAnim.duration = FLIP_ANIMATION_SPEED
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
        frontAnim.duration = FLIP_ANIMATION_SPEED
        backAnim.duration = FLIP_ANIMATION_SPEED
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
        holder.setup(card!!, position, count, currentSide)

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

        holder.markButton.setOnClickListener {
            controller.markPressed(it.tag as Int)
            flipBackward(holder.cardBackContainer, holder.cardFrontContainer)

            holder.cardFrontContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_green)
            holder.cardBackContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_green)
        }
//        }

//        if (!holder.backButton.hasOnClickListeners()) {
        holder.backButton.setOnClickListener {
            flipBackward(holder.cardBackContainer, holder.cardFrontContainer)
        }
//        }

        holder.cardContainer.background = null
        if (card.peeped > 10) {
            holder.cardFrontContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_pink)
            holder.cardBackContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_pink)
        } else if (card.peeped == -1) {
            holder.cardFrontContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_green)
            holder.cardBackContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_green)
        } else {
            if (position % 2 == 0) {
                holder.cardFrontContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_blue)
                holder.cardBackContainer.backgroundDrawable = context.getExtDrawable(R.drawable.rnd_bg_blue)
            } else {
                holder.cardFrontContainer.background = context.getExtDrawable(R.drawable.rnd_bg_purple)
                holder.cardBackContainer.background = context.getExtDrawable(R.drawable.rnd_bg_purple)
            }
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

    fun setup(card: Card, position: Int, count: Int, cardSide: CardSide) {
        val label: String = String.format("%s of %s", (position + 1), count)

        if (cardSide == CardSide.FRONT) {
            wordTextView.text = card.frontVal
            translateTextView.text = card.backVal
        } else {
            wordTextView.text = card.backVal
            translateTextView.text = card.frontVal
        }


        textNumber.text = label

        peepButton.tag = position
        backButton.tag = position
        sayButton.tag = position
    }

    init {
        cardFrontContainer.cameraDistance = scale
        cardBackContainer.cameraDistance = scale
    }
}