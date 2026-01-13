package com.example.chillrate

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var menuLayout: LinearLayout

    protected fun setupSideMenu() {
        menuLayout = findViewById(R.id.menu_layout)

        findViewById<ImageButton>(R.id.btn_menu).setOnClickListener {
            toggleMenu()
        }

        menuLayout.findViewById<Button>(R.id.btn_new_session).setOnClickListener {
            openScreen(NewSessionActivity::class.java)
        }

        menuLayout.findViewById<Button>(R.id.btn_history).setOnClickListener {
            openScreen(HistoryActivity::class.java)
        }

        menuLayout.findViewById<Button>(R.id.btn_profile).setOnClickListener {
            openScreen(ProfileActivity::class.java)
        }

        menuLayout.findViewById<Button>(R.id.btn_group).setOnClickListener {
            openScreen(GroupActivity::class.java)
        }

        menuLayout.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            openScreen(SettingsActivity::class.java)
        }

        menuLayout.findViewById<Button>(R.id.btn_exit).setOnClickListener {
            finishAffinity()
        }
    }

    private fun toggleMenu() {
        menuLayout.visibility =
            if (menuLayout.visibility == View.VISIBLE)
                View.GONE
            else
                View.VISIBLE
    }

    protected fun openScreen(activity: Class<*>) {
        menuLayout.visibility = View.GONE
        startActivity(Intent(this, activity))
    }
}
