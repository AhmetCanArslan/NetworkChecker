package com.arslan.networkchecker

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class NetworkLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val isAppForeground: Boolean,
    val hasNetworkAccess: Boolean
) {
    fun toCsv(): String = "$timestamp,$isAppForeground,$hasNetworkAccess"
    
    companion object {
        fun fromCsv(csv: String): NetworkLogEntry? {
            val parts = csv.split(",")
            if (parts.size == 3) {
                return try {
                    NetworkLogEntry(
                        timestamp = parts[0].toLong(),
                        isAppForeground = parts[1].toBoolean(),
                        hasNetworkAccess = parts[2].toBoolean()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            return null
        }
    }
}

object NetworkLog {
    private val _logs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val logs: StateFlow<List<NetworkLogEntry>> = _logs
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences("network_logs", Context.MODE_PRIVATE)
        loadFromDisk()
    }

    private fun loadFromDisk() {
        val savedLogsString = prefs?.getString("logs_csv", "") ?: ""
        if (savedLogsString.isNotEmpty()) {
            val loaded = savedLogsString.split(";")
                .mapNotNull { NetworkLogEntry.fromCsv(it) }
            _logs.value = loaded
        }
    }

    private fun saveToDisk(logs: List<NetworkLogEntry>) {
        val csvString = logs.joinToString(";") { it.toCsv() }
        prefs?.edit()?.putString("logs_csv", csvString)?.apply()
    }

    fun addLog(isAppForeground: Boolean, hasNetworkAccess: Boolean) {
        _logs.update { currentLogs ->
            val newLog = NetworkLogEntry(
                isAppForeground = isAppForeground,
                hasNetworkAccess = hasNetworkAccess
            )
            val updated = listOf(newLog) + currentLogs
            // Keep the last 1000 logs to avoid memory issues but allow a good history
            val newLogs = updated.take(1000)
            saveToDisk(newLogs)
            newLogs
        }
    }
    
    fun clear() {
        _logs.value = emptyList()
        prefs?.edit()?.remove("logs_csv")?.apply()
    }
}
