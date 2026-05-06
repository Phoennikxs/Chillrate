package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.chillrate.api.RetrofitClient
import com.example.chillrate.model.UpdateUserParamsRequest
import kotlinx.coroutines.launch

class PersonalDataActivity : AppCompatActivity() {

    private lateinit var etBirthday: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnFinish: Button

    private var email: String = ""
    private var fullName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_data)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        email = intent.getStringExtra("EMAIL") ?: ""
        fullName = intent.getStringExtra("NAME") ?: ""

        if (email.isEmpty()) {
            Toast.makeText(this, "Ошибка: email не передан", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
    }

    private fun initViews() {
        etBirthday = findViewById(R.id.edit_birthday_reg)
        etHeight = findViewById(R.id.edit_height_reg)
        etWeight = findViewById(R.id.edit_weight_reg)
        btnFinish = findViewById(R.id.button_register_2)

        btnFinish.setOnClickListener {
            savePersonalData()
        }
    }

    private fun savePersonalData() {
        val heightStr = etHeight.text.toString().trim()
        val weightStr = etWeight.text.toString().trim()

        val height = heightStr.filter { it.isDigit() }.toIntOrNull() ?: 0
        val weight = weightStr.filter { it.isDigit() }.toIntOrNull() ?: 0

        if (height < 50 || height > 250) {
            Toast.makeText(this, "Рост должен быть от 50 до 250 см", Toast.LENGTH_SHORT).show()
            return
        }
        if (weight < 20 || weight > 300) {
            Toast.makeText(this, "Вес должен быть от 20 до 300 кг", Toast.LENGTH_SHORT).show()
            return
        }

        btnFinish.isEnabled = false

        lifecycleScope.launch {
            try {
                val request = UpdateUserParamsRequest(
                    email = email,
                    height_cm = height,
                    weight_kg = weight,
                    age = null,      // можно добавить позже
                    sex = null
                )

                val response = RetrofitClient.api.updateUserParams(request)

                if (response.isSuccessful) {
                    Toast.makeText(this@PersonalDataActivity, "Данные успешно сохранены!", Toast.LENGTH_LONG).show()

                    // Сохраняем данные локально
                    saveUserToPreferences()

                    // Переходим на экран входа с welcome
                    val intent = Intent(this@PersonalDataActivity, AutorizationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@PersonalDataActivity, "Ошибка сохранения на сервере", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PersonalDataActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnFinish.isEnabled = true
            }
        }
    }

    private fun saveUserToPreferences() {
        getSharedPreferences("app", MODE_PRIVATE).edit().apply {
            putString("user_email", email)
            putString("user_name", fullName)
            putString("user_birthday", etBirthday.text.toString())
            putInt("user_height", etHeight.text.toString().filter { it.isDigit() }.toIntOrNull() ?: 0)
            putInt("user_weight", etWeight.text.toString().filter { it.isDigit() }.toIntOrNull() ?: 0)
            apply()
        }
    }
}