package com.kktdeveloper.callingapp.model

data class CallLogEntry(
    val id: String,
    val name: String,
    val number: String,
    val type: String,
    val date: String,
    val duration: String
)