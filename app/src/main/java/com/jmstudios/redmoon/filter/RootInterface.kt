/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.filter

import android.content.Context
import android.graphics.Color
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
            if(thread != null)
                updateFilter(value)
            field = value
        }

    companion object : Logger()

    private var f = File(ctx.cacheDir, "redmoon-root-control")
    private val path = f.path

    private var thread: Thread? = null

    fun updateFilter(profile: Profile) {
        Log.i("Updating filter")

        f.printWriter().use {
            val red = Color.red(profile.filterColor) / 255.0f
            val green = Color.green(profile.filterColor) / 255.0f
            val blue = Color.blue(profile.filterColor) / 255.0f

            it.println("$red $green $blue")
        }
    }

    override fun onCreate() {
        if(thread != null) {
            return
        }

        Log.i("Starting root mode listener")
        if (!f.exists()) {
            Log.i("Pipe doesn't exist, creating")

            Shell.exec("su").use {
                it.exec("mkfifo $path")
                it.exec("chmod 666 $path") // Allow others to write to the fifo
            }

            f.printWriter().use {
                it.println("1 1 1")
            }
        }

        Log.i("Start root mode listener imp")

        val app = RedMoonApplication.app
        val pkg = app.packageManager.getPackageInfo(app.packageName, 0)
        val executablePath = "${pkg.applicationInfo.nativeLibraryDir}/re-shift.so"


        thread = Thread {
            Log.i("Started fifo polling thread")

            // Run the executable
            Shell.exec("su").use {
                it.exec("$executablePath $path")
                while (true) {
                    val line = it.readLine()
                    if (line == "Goodbye") {
                        Log.i("Got exit command")
                        break
                    }
                    Log.i("Got output $line")
                }
            }
        }

        thread?.start()

        updateFilter(profile)
    }

    override fun onDestroy() {
        Log.i("Stopping root mode listener")

        f.printWriter().use {
            it.println("exit")
        }

        // Wait for the fifo to be done
        thread?.join()

        // Remove the fifo to cleanup any data leftover
        Shell.exec("su").use {
            it.exec("rm $path")
        }

        thread = null
    }
}