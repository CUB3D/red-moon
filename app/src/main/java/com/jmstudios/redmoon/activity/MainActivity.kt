/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
 *
 *     Permission to use, copy, modify, and/or distribute this software
 *     for any purpose with or without fee is hereby granted, provided
 *     that the above copyright notice and this permission notice appear
 *     in all copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 *     CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *     OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *     NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 *     CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.jmstudios.redmoon.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.fragment.FilterFragment
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {

    lateinit private var mSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        ScreenFilterService.start()
        val intent = intent
        if (DEBUG) Log.i(TAG, "Got intent")

        val fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false)
        if (fromShortcut) { toggleAndFinish() }
        if (Config.darkThemeFlag) setTheme(R.style.AppThemeDark)

        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        // Only create and attach a new fragment on the first Activity creation.
        if (savedInstanceState == null) {
            if (DEBUG) Log.i(TAG, "onCreate - First creation")

            val view = FilterFragment()
            val tag = FRAGMENT_TAG_FILTER
            fragmentManager.beginTransaction()
                           .replace(R.id.fragment_container, view, tag)
                           .commit()
        }

        if (!Config.introShown) {
            startIntro()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        mSwitch = menu.findItem(R.id.screen_filter_switch).actionView as Switch
        mSwitch.isChecked = Config.filterIsOn
        mSwitch.setOnClickListener {
            if (Config.requestOverlayPermission(this)) {
                val state = if (mSwitch.isChecked) ScreenFilterService.Command.ON
                            else ScreenFilterService.Command.OFF
                ScreenFilterService.moveToState(state)
            } else mSwitch.isChecked = false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }
    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    override fun onDestroy() {
        // Really we want to post an eventbus event, "uiClosed"
        // So the service can turn itself off if the filter is paused
        /* ScreenFilterService.stop() */
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        val fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false)
        if (fromShortcut) { toggleAndFinish() }
        if (DEBUG) Log.i(TAG, "onNewIntent")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.show_intro_button -> {
                startIntro()
                return true
            }
            R.id.view_github -> {
                val github = resources.getString(R.string.project_page_url)
                val projectIntent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(github))
                startActivity(projectIntent)
                return super.onOptionsItemSelected(item)
            }
            R.id.email_developer -> {
                val email = resources.getString(R.string.contact_email_adress)
                val emailIntent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(email))
                startActivity(emailIntent)
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun startIntro() {
        val introIntent = Intent(this, Intro::class.java)
        startActivity(introIntent)
        Config.introShown = true
    }

    private fun toggleAndFinish() {
        ScreenFilterService.toggle()
        finish()
    }

    @Subscribe
    fun onFilterIsOnChanged(event: filterIsOnChanged) {
        mSwitch.isChecked = Config.filterIsOn
    }

    companion object {
        private val TAG = "MainActivity"
        private val DEBUG = true
        private val FRAGMENT_TAG_FILTER = "jmstudios.fragment.tag.FILTER"
        val EXTRA_FROM_SHORTCUT_BOOL = "com.jmstudios.redmoon.activity.MainActivity.EXTRA_FROM_SHORTCUT_BOOL"
    }
}
