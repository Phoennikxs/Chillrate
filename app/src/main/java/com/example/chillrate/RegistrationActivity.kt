package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.chillrate.api.RetrofitClient
import com.example.chillrate.model.RegisterRequest
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar   // добавим позже в XML

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val etName = findViewById<EditText>(R.id.edit_name)
        val etEmail = findViewById<EditText>(R.id.edit_email_reg)
        val etPassword = findViewById<EditText>(R.id.edit_pass_reg)
        btnRegister = findViewById(R.id.button_register)

        // Если добавишь ProgressBar — раскомментируй
        // progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, password)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        // Блокируем кнопку и показываем загрузку (если добавишь ProgressBar)
        btnRegister.isEnabled = false
        // progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(email = email, password = password, full_name = name)
                )

                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@RegistrationActivity, "Код подтверждения отправлен на почту", Toast.LENGTH_LONG).show()

                    // ← Переход на экран подтверждения
                    val intent = Intent(this@RegistrationActivity, EmailConfirmActivity::class.java).apply {
                        putExtra("EMAIL", email)
                    }
                    startActivity(intent)
                    finish()

                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@RegistrationActivity, "Ошибка регистрации: $errorMsg", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RegistrationActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Возвращаем кнопку в активное состояние
                btnRegister.isEnabled = true
                // progressBar.visibility = View.GONE
            }
        }
    }
}