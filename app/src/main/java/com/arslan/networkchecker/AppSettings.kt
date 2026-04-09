package com.arslan.networkchecker

import android.content.Context

object AppSettings {
    private const val PREFS_NAME = "network_checker_settings"
    private const val KEY_CHECK_INTERVAL_MS = "check_interval_ms"
    private const val DEFAULT_INTERVAL_MS = 3000L
    private const val MINUTE_MS = 60_000L
    private const val HOUR_MS = 3_600_000L

    val intervalOptionsMs = listOf(
        1000L,
        3000L,
        5000L,
        10000L,
        1 * HOUR_MS,
        2 * HOUR_MS,
        6 * HOUR_MS
    )

    fun getCheckIntervalMs(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedValue = prefs.getLong(KEY_CHECK_INTERVAL_MS, DEFAULT_INTERVAL_MS)
        return if (savedValue in intervalOptionsMs) savedValue else DEFAULT_INTERVAL_MS
    }

    fun setCheckIntervalMs(context: Context, intervalMs: Long) {
        val sanitizedValue = if (intervalMs in intervalOptionsMs) intervalMs else DEFAULT_INTERVAL_MS
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_CHECK_INTERVAL_MS, sanitizedValue).apply()
    }

    fun formatIntervalLabel(intervalMs: Long): String {
        return when {
            intervalMs % HOUR_MS == 0L -> {
                val hours = intervalMs / HOUR_MS
                if (hours == 1L) "1 hour" else "$hours hours"
            }
            intervalMs % MINUTE_MS == 0L -> {
                val minutes = intervalMs / MINUTE_MS
                if (minutes == 1L) "1 minute" else "$minutes minutes"
            }
            else -> {
                val seconds = intervalMs / 1000
                if (seconds == 1L) "1 second" else "$seconds seconds"
            }
        }
    }
}
