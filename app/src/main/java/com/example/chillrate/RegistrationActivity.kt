package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.chillrate.api.RetrofitClient
import com.example.chillrate.model.RegisterRequest
import android.widget.Toast



class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        var email = findViewById<EditText>(R.id.edit_email_reg)
        var name = findViewById<EditText>(R.id.edit_name)
        var pass = findViewById<EditText>(R.id.edit_pass_reg)

        var btn_register = findViewById<Button>(R.id.button_register)

        btn_register.setOnClickListener {
            register(email.text.toString().trim(), pass.text.toString().trim(), name.text.toString().trim())
            val intent = Intent(this, AutorizationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun register(email: String, password: String, name: String) {

        lifecycleScope.launch {

            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(email, password, name)
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@RegistrationActivity, "Регистрация успешна", Toast.LENGTH_LONG).show()


                    finish() // вернуться к логину
                } else {
                    Toast.makeText(this@RegistrationActivity, "Ошибка регистрации", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RegistrationActivity, "Ошибка сети", Toast.LENGTH_LONG).show()
            }
        }
    }

}

