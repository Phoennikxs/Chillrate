package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.example.chillrate.model.ResendCodeRequest
import com.example.chillrate.model.VerifyEmailRequest
import kotlinx.coroutines.launch

class EmailConfirmActivity : AppCompatActivity() {

    private lateinit var etCodes: List<EditText>
    private lateinit var btnConfirm: Button
    private lateinit var tvResend: TextView
    private lateinit var email: String

    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_confirm)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.emailConfirmation)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        email = intent.getStringExtra("EMAIL") ?: ""

        if (email.isEmpty()) {
            Toast.makeText(this, "Ошибка: email не передан", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        setupCodeInputs()
        startResendTimer()
    }

    private fun initViews() {
        etCodes = listOf(
            findViewById(R.id.emailConfEditText1),
            findViewById(R.id.emailConfEditText2),
            findViewById(R.id.emailConfEditText3),
            findViewById(R.id.emailConfEditText4),
            findViewById(R.id.emailConfEditText5),
            findViewById(R.id.emailConfEditText6)
        )

        btnConfirm = findViewById(R.id.button)
        tvResend = findViewById(R.id.textViewResend) // ← измени id в xml если нужно

        btnConfirm.setOnClickListener { verifyCode() }
        tvResend.setOnClickListener { resendCode() }
    }

    private fun setupCodeInputs() {
        for (i in etCodes.indices) {
            etCodes[i].addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < etCodes.size - 1) {
                        etCodes[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    private fun getCode(): String {
        return etCodes.joinToString("") { it.text.toString().trim() }
    }

    private fun verifyCode() {
        val code = getCode()
        if (code.length != 6) {
            Toast.makeText(this, "Введите 6-значный код", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.verifyEmail(VerifyEmailRequest(email, code))

                if (response.isSuccessful) {
                    Toast.makeText(this@EmailConfirmActivity, "Почта успешно подтверждена!", Toast.LENGTH_LONG).show()

                    // Переходим на экран входа
                    val intent = Intent(this@EmailConfirmActivity, PersonalDataActivity::class.java).apply {
                        putExtra("EMAIL", email)
                        putExtra("NAME", intent.getStringExtra("NAME") ?: "")
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@EmailConfirmActivity, "Неверный код", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EmailConfirmActivity, "Ошибка сети", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resendCode() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.resendCode(ResendCodeRequest(email))

                if (response.isSuccessful) {
                    Toast.makeText(this@EmailConfirmActivity, "Код отправлен повторно", Toast.LENGTH_SHORT).show()
                    startResendTimer()
                } else {
                    Toast.makeText(this@EmailConfirmActivity, "Не удалось отправить код", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EmailConfirmActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startResendTimer() {
        tvResend.isEnabled = false
        resendTimer?.cancel()

        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResend.text = "Отправить повторно через $seconds сек"
            }

            override fun onFinish() {
                tvResend.text = "Отправить код повторно"
                tvResend.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}