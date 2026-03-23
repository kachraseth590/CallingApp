package com.kktdeveloper.callingapp.model

sealed class CallState {
    object Idle : CallState()
    data class Calling(val number: String) : CallState()
    data class Ringing(val callerName: String, val callerNumber: String) : CallState()
    data class Active(val number: String, val callerName: String = "") : CallState()
    object Ended : CallState()
}
