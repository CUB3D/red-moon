/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.filter

import android.content.Context
import com.jmstudios.redmoon.RedMoonApplication
import com.jmstudios.redmoon.ThemedAppCompatActivity
import com.jmstudios.redmoon.filter.overlay.Overlay
import com.jmstudios.redmoon.model.Profile
import java.io.DataOutputStream
import java.io.File

import com.jmstudios.redmoon.util.Logger
import com.jmstudios.redmoon.util.Shell
import com.jmstudios.redmoon.util.ShellUtils
import com.jmstudios.redmoon.util.activeProfile

class RootFilter(ctx: Context) : Filter {
    override var profile = activeProfile.off
        set(value) {
            Overlay.Log.i("profile set to: $value")
            updateFilter(value)
            field = value
        }

    companion object : Logger() {
//        const val FIFO_PATH =  "/data/data/com.jmstudios.redmoon.debug/cache/redmoon-root-control"
    }

    private var f = File(ctx.cacheDir, "redmoon-root-control")
    private val path = f.path

    private var thread: Thread? = null
    private var active = false

    fun updateFilter(profile: Profile) {
        println("Updating filter")

        f.printWriter().use {
            it.println("1015 i32 1 f 0.68 f -5.94743e-05 f -5.94743e-05 f 0 f 0.978947 f 0.978947 f -1.05344e-15 f 0 f 0.00472379 f -5.94743e-05 f 0.978947 f 0 f 0 f 0 f 0 f 0.978947")
        }
    }

    override fun onCreate() {
        Log.i("Starting root mode listener")
        if (!f.exists()) {
            Log.i("Pipe doesn't exist, creating")

            Shell.exec("su").use {
                it.exec("mkfifo $path")
                it.exec("chmod 666 $path") // Allow others to write to the fifo
            }
        }

        println("Start root mode listener imp")

        val app = RedMoonApplication.app
        val pkg = app.packageManager.getPackageInfo(app.packageName, 0)
        val executablePath = "${pkg.applicationInfo.nativeLibraryDir}/re-shift.so"
        println("native lib dir: $executablePath")

        active = true

        thread = Thread {
            // Run the executable
            Shell.exec("su").use {
                it.exec("$executablePath $path")
                while (active) {
                    val line = it.readLine()
                    if (line == "Goodbye") {
                        break
                    }
                    println("Got output $line")
                }
                println("Ending shell")
            }
            println("FIFO poll ended")
        }

        thread?.start()

        updateFilter(profile)
    }

    override fun onDestroy() {
        Log.i("Stopping root mode listener")

        f.printWriter().use {
            it.println("exit")
        }
        active = false


        thread?.join()

        Shell.exec("su").use {
            it.exec("rm $path")
        }
    }
}