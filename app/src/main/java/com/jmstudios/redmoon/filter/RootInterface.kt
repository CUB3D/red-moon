/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.filter

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

class RootFilter() : Filter {
    override var profile = activeProfile.off
        set(value) {
            Overlay.Log.i("profile set to: $value")
            updateFilter(value)
            field = value
        }

    companion object : Logger() {
        const val FIFO_PATH = "/data/local/tmp/redmoon-root-control"
    }

    private val path = FIFO_PATH
    private var f = File(path)

    private var thread: Thread? = null
    private var active = false

    fun updateFilter(profile: Profile) {
        println("Updating filter")
        Shell.exec("su").use {
            it.exec("print '1015 i32 1 f 0.68 f -5.94743e-05 f -5.94743e-05 f 0 f 0.978947 f 0.978947 f -1.05344e-15 f 0 f 0.00472379 f -5.94743e-05 f 0.978947 f 0 f 0 f 0 f 0 f 0.978947' | tee $path")
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
            while(active) {
                // Run the executable
                Shell.exec("su").use {
                    it.exec("$executablePath $FIFO_PATH")
                    while (active) {
                        val line = it.readLine()
                        if (line == "Goodbye") {
                            break
                        }
                        println("Got output $line")
                    }
                }
            }
            println("FIFO poll ended")
        }

        thread?.start()

        updateFilter(profile)
    }

    override fun onDestroy() {
        Log.i("Stopping root mode listener")

//        f.printWriter().use {
//            it.println("exit")
//        }

        active = false

        Shell.exec("su").use {
            it.exec("service call SurfaceFlinger 1015 i32 0")
        }
    }
}