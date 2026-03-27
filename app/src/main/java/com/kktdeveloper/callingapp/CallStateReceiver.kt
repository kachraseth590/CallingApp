package com.kktdeveloper.callingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state  = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {

                    CallEndCallback.onIncomingCall?.invoke(number)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {

                    CallEndCallback.onCallConnected?.invoke()
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {

                    CallEndCallback.onCallEnded?.invoke()
                }
            }
        }
    }
}

object CallEndCallback {
    var onCallEnded: (() -> Unit)?       = null
    var onIncomingCall: ((String) -> Unit)? = null
    var onCallConnected: (() -> Unit)?   = null
}