package com.verbatim.studio.model

data class HistoryEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val date: String,
    val tone: String,
    val input: String,
    val output: String
)
