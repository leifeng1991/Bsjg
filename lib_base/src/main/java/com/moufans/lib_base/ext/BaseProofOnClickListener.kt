package com.moufans.lib_base.ext;

import android.view.View

fun View.setOnClickListener2(consumptionWindowInMs: Long = 1000L, listener: View.OnClickListener) {
    setOnClickListener(ClickListenerWrapper(consumptionWindowInMs, listener))
}

@JvmOverloads
fun View.setOnClickListener2(consumptionWindowInMs: Long = 1000L, listener: (View) -> Unit) {
    setOnClickListener(ClickListenerWrapper(consumptionWindowInMs, listener))
}

fun View.setSaraProofOnClickListener(consumptionWindowInMs: Long = 1000L, listener: View.OnClickListener) {
    setOnClickListener(ClickListenerWrapper(consumptionWindowInMs, listener))
}

fun View.setSaraProofOnClickListener(consumptionWindowInMs: Long = 1000L, listener: (View) -> Unit) {
    setOnClickListener(ClickListenerWrapper(consumptionWindowInMs, listener))
}

private var lastClickTimestamp: Long = 0

open class ClickListenerWrapper : View.OnClickListener {
    private val listener1: View.OnClickListener?
    private val listener2: ((View) -> Unit)?
    private val consumptionWindowInMs: Long

    constructor(consumptionWindowInMs: Long, listener: View.OnClickListener) {
        this.consumptionWindowInMs = consumptionWindowInMs
        listener1 = listener
        listener2 = null
    }

    constructor(consumptionWindowInMs: Long, listener: (View) -> Unit) {
        this.consumptionWindowInMs = consumptionWindowInMs
        listener1 = null
        listener2 = listener
    }

    override fun onClick(view: View) {
        val timestamp = System.currentTimeMillis()
        if (canConsumeClickAt(timestamp)) {
            listener1?.onClick(view)
            listener2?.invoke(view)
            lastClickTimestamp = timestamp
        }
    }

    private fun canConsumeClickAt(timestamp: Long): Boolean = (timestamp - lastClickTimestamp) >= consumptionWindowInMs
}
