package com.kktdeveloper.callingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kktdeveloper.callingapp.model.CallState
import com.kktdeveloper.callingapp.model.getContactName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel : ViewModel() {

    // ── Call State ──────────────────────────────────────────────
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    // ── Dial Pad Input ──────────────────────────────────────────
    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber.asStateFlow()

    // ── Call Timer ──────────────────────────────────────────────
    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    // ── Toggle States ───────────────────────────────────────────
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    // ── Internal Jobs ───────────────────────────────────────────
    private var timerJob: Job? = null
    private var incomingCallJob: Job? = null

    // ── Simulated incoming caller ───────────────────────────────
    private val simulatedCallerNumber = "9876543210"

    // ────────────────────────────────────────────────────────────
    //  Dial Pad Actions
    // ────────────────────────────────────────────────────────────

    fun onKeyPress(key: String) {
        if (_dialedNumber.value.length < 15) {
            _dialedNumber.value += key
        }
    }

    fun onBackspace() {
        val current = _dialedNumber.value
        if (current.isNotEmpty()) {
            _dialedNumber.value = current.dropLast(1)
        }
    }

    fun onClearNumber() {
        _dialedNumber.value = ""
    }

    // ────────────────────────────────────────────────────────────
    //  Call Actions
    // ────────────────────────────────────────────────────────────

    fun startOutgoingCall() {
        val number = _dialedNumber.value
        if (number.isEmpty()) return

        _callState.value = CallState.Calling(number)

        // Simulate the call connecting after 3 seconds → Active
        viewModelScope.launch {
            delay(3000)
            if (_callState.value is CallState.Calling) {
                val name = getContactName(number)
                _callState.value = CallState.Active(number = number, callerName = name)
                startCallTimer()
            }
        }
    }

    fun simulateIncomingCall() {
        // Can be triggered manually or auto-triggered
        if (_callState.value is CallState.Idle) {
            val callerName = getContactName(simulatedCallerNumber)
            _callState.value = CallState.Ringing(
                callerName = callerName,
                callerNumber = simulatedCallerNumber
            )
        }
    }

    fun acceptCall() {
        val ringing = _callState.value as? CallState.Ringing ?: return
        _callState.value = CallState.Active(
            number = ringing.callerNumber,
            callerName = ringing.callerName
        )
        startCallTimer()
    }

    fun rejectCall() {
        endCall()
    }

    fun endCall() {
        timerJob?.cancel()
        incomingCallJob?.cancel()
        _callState.value = CallState.Ended

        // Auto-reset to Idle after showing "Ended" briefly
        viewModelScope.launch {
            delay(1500)
            resetToIdle()
        }
    }

    fun resetToIdle() {
        timerJob?.cancel()
        _callState.value = CallState.Idle
        _dialedNumber.value = ""
        _callDurationSeconds.value = 0
        _isMuted.value = false
        _isSpeakerOn.value = false
    }

    // ────────────────────────────────────────────────────────────
    //  Toggle Actions
    // ────────────────────────────────────────────────────────────

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    // ────────────────────────────────────────────────────────────
    //  Timer
    // ────────────────────────────────────────────────────────────

    private fun startCallTimer() {
        timerJob?.cancel()
        _callDurationSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _callDurationSeconds.value++
            }
        }
    }

    fun formatDuration(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        incomingCallJob?.cancel()
    }
}