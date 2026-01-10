package net.hmjn.hyperstorage.infrastructure.logging

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

/**
 * Interface for receiving logs from Wasm.
 */
interface LogReceiver {
    fun log(
        level: Int,
        file: String,
        line: Int,
        message: String,
    )
}

/**
 * LogReceiver implementation that routes logs to Log4j.
 */
class Log4jLogReceiver(private val logger: Logger) : LogReceiver {
    override fun log(
        level: Int,
        file: String,
        line: Int,
        message: String,
    ) {
        val log4jLevel =
            when (level) {
                1 -> Level.ERROR
                2 -> Level.WARN
                3 -> Level.INFO
                4 -> Level.DEBUG
                5 -> Level.TRACE
                else -> Level.INFO
            }
        logger.log(log4jLevel, "[Wasm] $file:$line - $message")
    }
}
