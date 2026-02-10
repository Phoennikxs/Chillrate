package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var menuLayout: LinearLayout
    private var isMenuOpen = false
    private val animationDuration = 250L

    protected fun setupSideMenu() {
        menuLayout = findViewById(R.id.menu_layout)

        // Скрываем меню за экраном после того, как layout измерится
        menuLayout.post {
            menuLayout.translationX = -menuLayout.width.toFloat()
            menuLayout.visibility = View.VISIBLE
        }

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
        if (isMenuOpen) {
            closeMenu()
        } else {
            openMenu()
        }
    }

    private fun openMenu() {
        menuLayout.animate()
            .translationX(0f)
            .setDuration(animationDuration)
            .start()
        isMenuOpen = true
    }

    private fun closeMenu() {
        menuLayout.animate()
            .translationX(-menuLayout.width.toFloat())
            .setDuration(animationDuration)
            .start()
        isMenuOpen = false
    }

    protected fun openScreen(activity: Class<*>) {
        closeMenu()
        startActivity(Intent(this, activity))
    }
}
