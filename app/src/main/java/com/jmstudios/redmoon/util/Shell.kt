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
    val STDOUT = proc.inputStream
    val STDOUTReader = STDOUT.bufferedReader()
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
            STDOUTReader.readLine() ?: ""
        })

        return try {
            feature.get(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override fun close() {
        println("Closing 1")
        serialExecutor.shutdownNow()
        println("Closing 2")

        STDOUT.close()
        println("Closing 3")

        STDIN.flush()
        STDIN.close()

        STDERR.close()
    }

    companion object {
        const val READ_TIMEOUT_SECONDS = 5L

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