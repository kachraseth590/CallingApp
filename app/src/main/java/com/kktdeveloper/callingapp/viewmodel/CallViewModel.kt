package com.kktdeveloper.callingapp.viewmodel

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kktdeveloper.callingapp.model.CallLogEntry
import com.kktdeveloper.callingapp.model.CallState
import com.kktdeveloper.callingapp.model.ContactItem
import com.kktdeveloper.callingapp.model.getContactName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CallViewModel : ViewModel() {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLogEntry>>(emptyList())
    val callLogs: StateFlow<List<CallLogEntry>> = _callLogs.asStateFlow()

    private val _contacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var timerJob: Job? = null
    private var incomingCallJob: Job? = null




    fun onKeyPress(key: String) {
        if (_dialedNumber.value.length < 15) _dialedNumber.value += key
    }

    fun onBackspace() {
        val current = _dialedNumber.value
        if (current.isNotEmpty()) _dialedNumber.value = current.dropLast(1)
    }

    fun onClearNumber() { _dialedNumber.value = "" }

    fun setDialedNumber(number: String) { _dialedNumber.value = number }



    fun startOutgoingCall(context: Context) {
        val number = _dialedNumber.value
        if (number.isEmpty()) return
        _callState.value = CallState.Calling(number)
        viewModelScope.launch {
            delay(3000)
            if (_callState.value is CallState.Calling) {
                val name = withContext(Dispatchers.IO) {
                    lookupContactName(context, number)
                }
                _callState.value = CallState.Active(number = number, callerName = name ?: number)
                startCallTimer()
            }
        }
    }



    fun onRealIncomingCall(number: String, context: Context) {
        if (_callState.value is CallState.Idle) {
            viewModelScope.launch {
                val name = withContext(Dispatchers.IO) {
                    lookupContactName(context, number)
                }
                _callState.value = CallState.Ringing(
                    callerName   = name ?: number,
                    callerNumber = number
                )
            }
        }
    }


    private fun lookupContactName(context: Context, number: String?): String? {
        if (number.isNullOrBlank()) return null
        return try {
            val uri = android.net.Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(number)
            )
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )?.use {
                if (it.moveToFirst())
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                else null
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    fun onCallConnected() {
        // If we were in ringing state, move to active
        val ringing = _callState.value as? CallState.Ringing ?: return
        _callState.value = CallState.Active(
            number     = ringing.callerNumber,
            callerName = ringing.callerName
        )
        startCallTimer()
    }
    fun acceptCall() {
        val ringing = _callState.value as? CallState.Ringing ?: return
        _callState.value = CallState.Active(
            number = ringing.callerNumber,
            callerName = ringing.callerName
        )
        startCallTimer()
    }

    fun rejectCall() { endCall() }

    fun endCall() {
        timerJob?.cancel()
        incomingCallJob?.cancel()
        _callState.value = CallState.Ended
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



    fun toggleMute() { _isMuted.value = !_isMuted.value }
    fun toggleSpeaker() { _isSpeakerOn.value = !_isSpeakerOn.value }



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
        return if (hrs > 0) String.format("%02d:%02d:%02d", hrs, mins, secs)
        else String.format("%02d:%02d", mins, secs)
    }



    fun loadCallLogs(context: Context) {
        viewModelScope.launch {
            _callLogs.value = withContext(Dispatchers.IO) {
                fetchCallLogs(context)
            }
        }
    }

    private fun fetchCallLogs(context: Context): List<CallLogEntry> {
        val logs = mutableListOf<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection, null, null,
                CallLog.Calls.DATE + " DESC"
            )
            cursor?.use {
                val idIdx       = it.getColumnIndex(CallLog.Calls._ID)
                val nameIdx     = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numberIdx   = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx     = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx     = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    val number   = it.getString(numberIdx) ?: ""
                    val name = it.getString(nameIdx)?.takeIf { it.isNotBlank() } ?: number
                    val type     = when (it.getInt(typeIdx)) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                        CallLog.Calls.MISSED_TYPE   -> "Missed"
                        else                        -> "Unknown"
                    }
                    val date     = SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a", Locale.getDefault()
                    ).format(Date(it.getLong(dateIdx)))
                    val durSecs  = it.getLong(durationIdx)
                    val duration = if (durSecs == 0L) "—"
                    else String.format(
                        "%02d:%02d",
                        TimeUnit.SECONDS.toMinutes(durSecs),
                        durSecs % 60
                    )
                    logs.add(CallLogEntry(
                        id = it.getString(idIdx),
                        name = name,
                        number = number,
                        type = type,
                        date = date,
                        duration = duration
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return logs
    }



    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun loadContacts(context: Context) {
        viewModelScope.launch {
            _contacts.value = withContext(Dispatchers.IO) {
                fetchContacts(context)
            }
        }
    }

    private fun fetchContacts(context: Context): List<ContactItem> {
        val contacts = mutableListOf<ContactItem>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                val idIdx     = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx   = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    contacts.add(ContactItem(
                        id     = it.getString(idIdx),
                        name   = it.getString(nameIdx) ?: "",
                        number = it.getString(numberIdx) ?: ""
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contacts.distinctBy { it.number }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        incomingCallJob?.cancel()
    }
}