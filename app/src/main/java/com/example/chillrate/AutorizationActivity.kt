package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.chillrate.api.RetrofitClient
import com.example.chillrate.model.UserResponse
import kotlinx.coroutines.launch

class AutorizationActivity : AppCompatActivity() {

    private lateinit var errorText: TextView
    private lateinit var welcomeLayout: View
    private lateinit var welcomeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autorization)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.autorization)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        errorText = findViewById(R.id.autorization_error_text)
        welcomeLayout = findViewById(R.id.welcome_screen)
        welcomeText = findViewById(R.id.welcome_text)

        val btnLogin = findViewById<Button>(R.id.btn_log_in)
        val btnRegister = findViewById<Button>(R.id.btn_go_to_reg)

        checkExistingToken()

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.edit_email).text.toString().trim()
            val password = findViewById<EditText>(R.id.edit_pass).text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            login(email, password)
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
    }

    private fun checkExistingToken() {
        val token = getSharedPreferences("app", MODE_PRIVATE).getString("token", null)
        if (!token.isNullOrEmpty()) {
            loadUserProfile(token)
        }
    }

    private fun login(email: String, password: String) {
        lifecycleScope.launch {
            errorText.visibility = View.GONE
            try {
                val response = RetrofitClient.api.login(email, password)

                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    if (token != null) {
                        getSharedPreferences("app", MODE_PRIVATE).edit()
                            .putString("token", token)
                            .apply()

                        loadUserProfile(token)
                    }
                } else {
                    errorText.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@AutorizationActivity, "Ошибка сети", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadUserProfile(token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getCurrentUser("Bearer $token")

                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        saveUserData(user)

                        welcomeLayout.visibility = View.VISIBLE
                        welcomeText.text = "Добро пожаловать\n${user.full_name}"

                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(this@AutorizationActivity, MainActivity::class.java))
                            finish()
                        }, 2500)
                    }
                }
            } catch (e: Exception) {
                // Если не удалось загрузить профиль — просто переходим
                startActivity(Intent(this@AutorizationActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun saveUserData(user: UserResponse) {
        getSharedPreferences("app", MODE_PRIVATE).edit().apply {
            putString("user_email", user.email)
            putString("user_name", user.full_name)
            putString("user_sex", user.sex)
            putInt("user_age", user.age ?: -1)
            putInt("user_height", user.height_cm ?: -1)
            putInt("user_weight", user.weight_kg ?: -1)
            apply()
        }
    }
}