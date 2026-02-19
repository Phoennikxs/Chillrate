package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet

class AutorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autorization)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.autorization)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        var AutorizationButton: Button = findViewById(R.id.btn_log_in)
        var GoToRegistrationButton: Button = findViewById(R.id.btn_go_to_reg)
        var AutriztionLayout = findViewById<View>(R.id.autorization)
        var WelcomeLayout = findViewById<View>(R.id.welcome_screen)

        AutorizationButton.setOnClickListener {

            WelcomeLayout.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            }, 2500)
        }

        GoToRegistrationButton.setOnClickListener {
            AutorizationButton.setOnClickListener {
                val intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}