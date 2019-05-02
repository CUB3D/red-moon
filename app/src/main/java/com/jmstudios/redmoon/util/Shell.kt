package com.jmstudios.redmoon.util

import android.telecom.Call
import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

fun InputStream.clean() {
    try {
        while(available() != 0)
            skip(available().toLong())
    } catch (e: Exception) {}
}

class Shell(proc: Process): Closeable {
    val STDOUT = proc.inputStream.bufferedReader()
    val STDIN = proc.outputStream
    val STDERR = proc.errorStream

    val serialExecutor = Executors.newSingleThreadExecutor()

    init {
        STDERR.clean()
    }

    fun exec(cmd: String) {
        STDIN.write(cmd.toByteArray())
        STDIN.write("\n".toByteArray())
        STDIN.flush()
    }

    fun readLine(): String {
        val feature : Future<String> = serialExecutor.submit(Callable<String> {
            STDOUT.readLine() ?: ""
        })

        return try {
            feature.get(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override fun close() {
        serialExecutor.shutdownNow()

        STDOUT.close()

        STDIN.flush()
        STDIN.close()

        STDERR.close()
    }

    companion object {
        const val READ_TIMEOUT_SECONDS = 10L

        fun exec(cmd: String): Shell {
            return Shell(Runtime.getRuntime().exec(cmd))
        }
    }
}

object ShellUtils {
    fun isRootShellAvailable(): Boolean {
        println("Checking for root perms")
        val line = Shell.exec("su").use {
            it.exec("id")
            it.readLine()
        }

        println("Done")

        return line.contains("uid=0")
    }
}