package de.christinecoenen.code.zapp.utils.logging

import timber.log.Timber

class LogTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logMessage = "$tag: $message"
        LogRepository.addLog(logMessage)
        t?.let {
            LogRepository.addLog(it.stackTraceToString())
        }
    }
}
