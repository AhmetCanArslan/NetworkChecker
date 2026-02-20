package com.arslan.networkchecker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class NetworkLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val isAppForeground: Boolean,
    val hasNetworkAccess: Boolean
)

object NetworkLog {
    private val _logs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val logs: StateFlow<List<NetworkLogEntry>> = _logs

    fun addLog(isAppForeground: Boolean, hasNetworkAccess: Boolean) {
        _logs.update { currentLogs ->
            val newLog = NetworkLogEntry(
                isAppForeground = isAppForeground,
                hasNetworkAccess = hasNetworkAccess
            )
            val updated = listOf(newLog) + currentLogs
            // Keep the last 100 logs to avoid memory issues
            updated.take(100)
        }
    }
    
    fun clear() {
        _logs.value = emptyList()
    }
}
