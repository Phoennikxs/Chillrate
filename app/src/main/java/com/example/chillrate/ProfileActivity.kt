package com.example.chillrate

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : BaseActivity() {

    private lateinit var editName: EditText
    private lateinit var editBirthday: EditText
    private lateinit var editHeight: EditText
    private lateinit var editWeight: EditText

    private lateinit var btnMale: Button
    private lateinit var btnFemale: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Profile)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupSideMenu()
        initViews()
        loadUserData()
    }

    private fun initViews() {
        editName = findViewById(R.id.edit_person_name)
        editBirthday = findViewById(R.id.edit_birthday)
        editHeight = findViewById(R.id.edit_height)
        editWeight = findViewById(R.id.edit_weight)

        btnMale = findViewById(R.id.btn_set_male)
        btnFemale = findViewById(R.id.btn_set_female)

        // Обработка выбора пола
        btnMale.setOnClickListener {
            selectGender(true)
        }

        btnFemale.setOnClickListener {
            selectGender(false)
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("app", MODE_PRIVATE)

        val name = prefs.getString("user_name", "")
        val birthday = prefs.getString("user_birthday", "")
        val height = prefs.getInt("user_height", 0)
        val weight = prefs.getInt("user_weight", 0)
        val sex = prefs.getString("user_sex", "")

        editName.setText(name)
        editBirthday.setText(birthday)

        if (height > 0) editHeight.setText("$height см")
        if (weight > 0) editWeight.setText("$weight кг")

        // Устанавливаем пол
        when (sex?.lowercase()) {
            "male", "мужской", "м" -> selectGender(true)
            "female", "женский", "ж" -> selectGender(false)
        }
    }

    private fun selectGender(isMale: Boolean) {
        if (isMale) {
            btnMale.setBackgroundTintList(getColorStateList(R.color.button_pressed_color))
            btnFemale.setBackgroundTintList(getColorStateList(R.color.button_unpressed_color))
        } else {
            btnFemale.setBackgroundTintList(getColorStateList(R.color.button_pressed_color))
            btnMale.setBackgroundTintList(getColorStateList(R.color.button_unpressed_color))
        }

        // Сохраняем выбор пола
        getSharedPreferences("app", MODE_PRIVATE).edit()
            .putString("user_sex", if (isMale) "male" else "female")
            .apply()
    }

    // Можно добавить кнопку "Сохранить изменения" позже
}