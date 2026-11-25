package com.shellforge.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Owns one persistent Android sh process.
 *
 * Commands are intentionally sent to the shell as user-entered terminal input.
 * The process runs with the app's normal Android UID and permissions.
 */
class TerminalSession(
    private val workingDirectory: File,
    private val onOutput: (String) -> Unit,
    private val onExit: (Int) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var process: Process? = null

    @Volatile
    private var writer: BufferedWriter? = null

    private var outputJob: Job? = null
    private var errorJob: Job? = null

    fun start() {
        if (process != null) return

        scope.launch {
            try {
                val started = ProcessBuilder("sh")
                    .directory(workingDirectory)
                    .redirectErrorStream(false)
                    .start()

                process = started
                writer = BufferedWriter(OutputStreamWriter(started.outputStream))

                outputJob = readStream(started.inputStream)
                errorJob = readStream(started.errorStream)

                withContext(Dispatchers.Main) {
                    onOutput("Shellforge: Android sh session connected.")
                    onOutput("Working directory: ${workingDirectory.absolutePath}")
                }

                val exitCode = started.waitFor()
                process = null
                writer = null

                withContext(Dispatchers.Main) {
                    onOutput("Shell process exited (code $exitCode).")
                    onExit(exitCode)
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    onOutput("Unable to start shell: ${error.message ?: error.javaClass.simpleName}")
                }
            }
        }
    }

    fun execute(command: String) {
        if (command.isBlank()) return

        scope.launch {
            try {
                val activeWriter = writer
                if (activeWriter == null || process?.isAlive != true) {
                    withContext(Dispatchers.Main) {
                        onOutput("Shell is not running.")
                    }
                    return@launch
                }

                synchronized(activeWriter) {
                    activeWriter.write(command)
                    activeWriter.newLine()
                    activeWriter.flush()
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    onOutput("Command input failed: ${error.message ?: error.javaClass.simpleName}")
                }
            }
        }
    }

    private fun readStream(stream: java.io.InputStream): Job = scope.launch {
        BufferedReader(InputStreamReader(stream)).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                withContext(Dispatchers.Main) {
                    onOutput(line)
                }
            }
        }
    }

    fun close() {
        try {
            writer?.let { activeWriter ->
                synchronized(activeWriter) {
                    activeWriter.write("exit")
                    activeWriter.newLine()
                    activeWriter.flush()
                }
            }
        } catch (_: Exception) {
            // The shell may already have exited.
        }

        process?.destroy()
        process = null
        writer = null
    }

    fun dispose() {
        close()
        scope.cancel()
    }
}
