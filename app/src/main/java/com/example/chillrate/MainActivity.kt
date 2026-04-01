package com.example.chillrate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.neurosdk2.helpers.PermissionHelper
import com.neurosdk2.neuro.types.SensorFamily
import com.neurosdk2.neuro.types.SensorInfo
import com.neurosdk2.neuro.Scanner

import kotlin.collections.mutableListOf
import androidx.lifecycle.lifecycleScope

class MainActivity : BaseActivity() {

    private lateinit var startButton: ImageButton
    private lateinit var startText: TextView

    private var scanner: Scanner? = null
    private val foundSensors = mutableListOf<SensorInfo>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SensorAdapter

    private var pulseAnimator: ObjectAnimator? = null
    private var textColorAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if (!PermissionHelper.HasAllPermissions(this)) {
            PermissionHelper.RequestPermissions(this) { granted, denied, deniedPermanently ->
                if (denied.isEmpty()) {
                    Toast.makeText(this, "Разрешения получены", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Нужны Bluetooth и Location разрешения", Toast.LENGTH_LONG).show()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        startButton = findViewById(R.id.button_start)
        startText = findViewById(R.id.text_start)

        recyclerView = findViewById(R.id.sensorRecycler)

        adapter = SensorAdapter(mutableListOf()) { sensorInfo ->
            connectToSensor(sensorInfo)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        startButton.setOnClickListener {
            startSearchUI()
            startScan()
        }

        setupSideMenu()
    }

    private fun startScan() {
        scanner = Scanner(SensorFamily.SensorLECallibri)

        scanner?.sensorsChanged = Scanner.ScannerCallback { _, sensors ->
            runOnUiThread {
                recyclerView.visibility = View.VISIBLE

                foundSensors.clear()
                foundSensors.addAll(sensors)

                adapter.update(foundSensors)
            }
        }

        scanner?.start()

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            stopCallibriScan()
        }, 10000)
    }

    private fun stopCallibriScan() {
        scanner?.stop()
        scanner?.sensorsChanged = null
        stopSearchUI()
    }

    private fun connectToSensor(sensorInfo: SensorInfo) {
        stopCallibriScan()                    // останавливаем поиск

        SensorHolder.selectedSensorInfo = sensorInfo   // ← передаём информацию

        val intent = Intent(this, NewSessionActivity::class.java)
        startActivity(intent)

        // Можно закрыть MainActivity после перехода (по желанию)
        // finish()
    }

    private fun startSearchUI() {
        startText.text = "Поиск"

        if (pulseAnimator == null) {
            pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                startButton,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.15f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.15f)
            ).apply {
                duration = 800
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
            }
        }

        if (textColorAnimator == null) {
            textColorAnimator = ObjectAnimator.ofArgb(
                startText,
                "textColor",
                ContextCompat.getColor(this, R.color.white),
                ContextCompat.getColor(this, R.color.soft_blue)
            ).apply {
                duration = 800
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
            }
        }

        pulseAnimator?.start()
        textColorAnimator?.start()
    }

    private fun stopSearchUI() {
        pulseAnimator?.cancel()
        textColorAnimator?.cancel()

        startButton.scaleX = 1f
        startButton.scaleY = 1f

        startText.text = "Начать"
        startText.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner?.stop()
        scanner?.close()
    }
}