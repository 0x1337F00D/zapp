package de.christinecoenen.code.zapp.utils.logging

import java.util.concurrent.ConcurrentLinkedDeque

import java.util.concurrent.atomic.AtomicInteger

object LogRepository {
    private val logs = ConcurrentLinkedDeque<String>()
    private const val MAX_LOGS = 1000
    private val size = AtomicInteger(0)

    fun addLog(message: String) {
        logs.add(message)
        if (size.incrementAndGet() > MAX_LOGS) {
            logs.pollFirst()
            size.decrementAndGet()
        }
    }

    fun getLogs(): List<String> {
        return logs.toList()
    }

    fun clear() {
        logs.clear()
        size.set(0)
    }
}
