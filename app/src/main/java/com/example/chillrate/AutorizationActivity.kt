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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import com.example.chillrate.api.RetrofitClient
import kotlinx.coroutines.launch

private lateinit var AutorizationError: TextView

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
        AutorizationError = findViewById<TextView>(R.id.autorization_error_text)
        var WelcomeLayout = findViewById<View>(R.id.welcome_screen)


        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if ((token != null) or false) {
            WelcomeLayout.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({

                startActivity(Intent(this@AutorizationActivity, MainActivity::class.java))
                finish()

            }, 3000)
        }

        AutorizationButton.setOnClickListener {

            val email = findViewById<EditText>(R.id.edit_email).text.toString().trim()
            val pass = findViewById<EditText>(R.id.edit_pass).text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                AutorizationError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            login(email, pass)

        }

        GoToRegistrationButton.setOnClickListener {

            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()

        }
    }

    private fun login(email: String, password: String) {



        lifecycleScope.launch {

            AutorizationError.visibility = View.GONE
            try {
                val response = RetrofitClient.api.login(email, password)

                if (response.isSuccessful) {

                    val token = response.body()?.access_token

                    // сохранить токен
                    val prefs = getSharedPreferences("app", MODE_PRIVATE)
                    prefs.edit().putString("token", token).apply()

                    Toast.makeText(this@AutorizationActivity, "Вход успешен", Toast.LENGTH_LONG).show()

                    // открыть главный экран
                    var welcomeLayout = findViewById<View>(R.id.welcome_screen)
                    welcomeLayout.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed({

                        startActivity(Intent(this@AutorizationActivity, MainActivity::class.java))
                        finish()

                    }, 3000)

                } else {
                    Toast.makeText(this@AutorizationActivity, "Неверный логин", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@AutorizationActivity, "Ошибка сети", Toast.LENGTH_LONG).show()
            }
        }
    }

}