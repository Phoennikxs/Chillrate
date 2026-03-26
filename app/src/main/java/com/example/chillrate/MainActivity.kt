package com.example.chillrate

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.neurosdk2.*
import com.neurosdk2.helpers.PermissionHelper
import com.neurosdk2.neuro.Callibri

import com.neurosdk2.neuro.types.SensorFamily
import com.neurosdk2.neuro.types.SensorInfo
import com.neurosdk2.neuro.Scanner
import com.neurosdk2.neuro.Sensor
import kotlin.collections.mutableListOf
import androidx.lifecycle.lifecycleScope
import com.neurosdk2.neuro.types.SensorCommand
import com.neurosdk2.neuro.types.SensorFeature
import com.neurosdk2.neuro.interfaces.CallibriSignalDataReceived
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.neurosdk2.neuro.types.CallibriSignalData
import com.neurotech.callibriutils.CallibriMath


class MainActivity : BaseActivity() {

    private lateinit var startButton: ImageButton
    private lateinit var startText: TextView
    private var scanner: Scanner? = null
    private var pulseAnimator: ObjectAnimator? = null
    private var textColorAnimator: ObjectAnimator? = null

    private val foundSensors = mutableListOf<SensorInfo>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SensorAdapter
    private var currentSensor: Callibri? = null
    private var callibriMath: CallibriMath? = null
    private val signalBuffer = mutableListOf<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if(!PermissionHelper.HasAllPermissions(this)){
            PermissionHelper.RequestPermissions(this
            ) { grantedPermissions, deniedPermissions, deniedPermanentlyPermissions ->
                if (deniedPermissions.isEmpty()) {
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

        val samplingRate = 250
        val dataWindow = samplingRate / 2
        val nwins = 30

        callibriMath = CallibriMath(samplingRate, dataWindow, nwins)
        callibriMath?.setPressureAverage(6)


        setupSideMenu()
    }

    private fun startScan() {

//        Toast.makeText(this, "Поиск Callibri...", Toast.LENGTH_SHORT).show()

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

//        Toast.makeText(this, "Поиск завершён", Toast.LENGTH_SHORT).show()
        stopSearchUI()

    }

    private fun connectToSensor(sensorInfo: SensorInfo) {

        lifecycleScope.launch(Dispatchers.IO) {

            try {

                val sensor = scanner?.createSensor(sensorInfo) as Callibri
                currentSensor = sensor

                // состояние подключения
                sensor.sensorStateChanged = Sensor.SensorStateChanged { state ->
                    println("STATE: $state")
                }

                // уровень батареи
                sensor.batteryChanged = Sensor.BatteryChanged { battery ->

                    runOnUiThread {

                        adapter.updateBattery(sensorInfo.address, battery)

                    }
                }

                sensor.callibriSignalDataReceived =
                    CallibriSignalDataReceived { dataArray ->

                        for (data in dataArray) {

                            val samples = data.samples  // <-- это массив

                            for (s in samples) {
                                signalBuffer.add(s.toDouble())  // <-- ВОТ ТАК ПРАВИЛЬНО
                            }
                        }

                        while (signalBuffer.size >= 25) {

                            val chunk = DoubleArray(25)

                            for (i in 0 until 25) {
                                chunk[i] = signalBuffer[i]
                            }

                            repeat(25) { signalBuffer.removeAt(0) }

                            callibriMath?.pushAndProcessData(chunk)

                            if (callibriMath?.rrDetected() == true) {

                                val hr = callibriMath?.getHR() ?: 0.0
                                callibriMath?.setRRchecked()

                                runOnUiThread {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Пульс: ${hr.toInt()} bpm",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                // подключение
                sensor.connect()

                // запуск потока сигнала
                sensor.execCommand(SensorCommand.StartSignal)

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка подключения: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showDataDialog(message: String) {

        AlertDialog.Builder(this)
            .setTitle("Данные датчика")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner?.stop()
        scanner?.close()
    }

    private fun startSearchUI() {
        startText.text = "Поиск"

        // Пульсация кнопки
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

        // Перелив цвета текста
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


}
