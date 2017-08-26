/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

package com.jmstudios.redmoon.filter

import com.jmstudios.redmoon.model.Profile
import java.io.DataOutputStream
import java.io.File

import com.jmstudios.redmoon.util.Logger

class RootFilter(filePath: String) : Filter {

    companion object : Logger()

    private val path = filePath
    private var f = File(filePath)

    override fun start() {
        Log.i("Starting root mode listener")
        if (!f.exists()) {
            Log.i("Pipe doesn't exist, creating")
            // `mkfifo` if pipe is non-existent
            val sh = Runtime.getRuntime().exec("sh")
            val shOut = DataOutputStream(sh.outputStream)
            shOut?.writeBytes("mkfifo $path \n")
            shOut?.flush()
        }

        // TODO: Start listener
        // Waiting for smichel to implement NDK
    }

    override fun setColor(profile: Profile) {
        // TODO: Generate command from profile
        val surfaceCommand = ""
        f.printWriter().use { out ->
            out.println(surfaceCommand)
        }
    }

    override fun stop() {
        Log.i("Stopping root mode listener")
        f.printWriter().use { out ->
            out.println("exit")
        }
    }
}