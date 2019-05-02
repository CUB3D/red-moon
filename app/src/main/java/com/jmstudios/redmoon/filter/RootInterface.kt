/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.filter

import com.jmstudios.redmoon.RedMoonApplication
import com.jmstudios.redmoon.filter.overlay.Overlay
import com.jmstudios.redmoon.model.Profile
import java.io.DataOutputStream
import java.io.File

import com.jmstudios.redmoon.util.Logger
import com.jmstudios.redmoon.util.Shell
import com.jmstudios.redmoon.util.ShellUtils
import com.jmstudios.redmoon.util.activeProfile

class RootFilter() : Filter {
    override var profile = activeProfile.off
        set(value) {
            Overlay.Log.i("profile set to: $value")
            field = value
        }

    companion object : Logger() {
        const val FIFO_PATH = "/data/local/tmp/redmoon-root-control"
    }

    private val path = FIFO_PATH
    private var f = File(path)

    private var reShiftProcess: Process? = null

    override fun onCreate() {
        Log.i("Starting root mode listener")
        if (!f.exists()) {
            Log.i("Pipe doesn't exist, creating")
            // `mkfifo` if pipe is non-existent
            val sh = Runtime.getRuntime().exec("sh")
            val shOut = DataOutputStream(sh.outputStream)
            shOut?.writeBytes("mkfifo $path \n")
            shOut?.flush()
        }

        println("Start root mode listener imp")

        val app = RedMoonApplication.app
        val pkg = app.packageManager.getPackageInfo(app.packageName, 0)
        val executablePath = "${pkg.applicationInfo.nativeLibraryDir}/re-shift.so"
        println("native lib dir: $executablePath")

        Thread {
            // Run the executable
            Shell.exec("su").use {
                it.exec("$executablePath $FIFO_PATH")
                while(true) {
                    val line = it.readLine()
                    if(line == "Goodbye") {
                        break
                    }
                    println("Got output $line")
                }
            }
            println("FIFO poll ended")
        }.start()

        //TODO: can this be lateinit
//        reShiftProcess = Runtime.getRuntime().exec("")


//
//
//
//        val input = File(pkg.applicationInfo.nativeLibraryDir, "re-shift.so")
////        val output = File("/data/local/tmp/re-shift")
////        if(!output.exists())
////            output.createNewFile()
////        input.copyTo(output)
//
////        Runtime.getRuntime().exec("chmod 777 /data/local/tmp/re-shift && ./data/local/tmp/re-shift $path")
//        Runtime.getRuntime().exec(".${input.absolutePath} $path")
//
//        println("Ending here")

        // TODO: Start listener
        // Waiting for smichel to implement NDK
    }

    fun setColor(profile: Profile) {
        // TODO: Generate command from profile
        val surfaceCommand = ""
        f.printWriter().use { out ->
            out.println(surfaceCommand)
        }
    }

    override fun onDestroy() {
        Log.i("Stopping root mode listener")

        if(f.exists()) {
            f.printWriter().use { out ->
                out.println("exit")
            }
        }
    }
}