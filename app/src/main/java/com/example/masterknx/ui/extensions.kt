package com.example.masterknx.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View

fun View.animateAlpha(from: Float, to: Float, duration: Long = 600L, startDelay: Long = 0L) {
    ObjectAnimator
        .ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(View.ALPHA, from, to))
        .apply {
            this.duration = duration
            this.startDelay = startDelay
        }
        .start()
}