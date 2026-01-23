package de.christinecoenen.code.zapp.utils.logging

import java.util.concurrent.ConcurrentLinkedDeque

object LogRepository {
    private val logs = ConcurrentLinkedDeque<String>()
    private const val MAX_LOGS = 1000

    fun addLog(message: String) {
        logs.add(message)
        if (logs.size > MAX_LOGS) {
            logs.pollFirst()
        }
    }

    fun getLogs(): List<String> {
        return logs.toList()
    }

    fun clear() {
        logs.clear()
    }
}
