package com.example.chillrate

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.neurosdk2.neuro.Callibri
import com.neurosdk2.neuro.Scanner
import com.neurosdk2.neuro.Sensor
import com.neurosdk2.neuro.interfaces.CallibriSignalDataReceived
import com.neurosdk2.neuro.types.SensorCommand
import com.neurosdk2.neuro.types.SensorInfo
import com.neurotech.callibriutils.CallibriMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewSessionActivity : AppCompatActivity() {

    private var currentSensor: Callibri? = null
    private var callibriMath: CallibriMath? = null
    private val signalBuffer = mutableListOf<Double>()

    private lateinit var chartHR: LineChart
    private lateinit var textViewHR: TextView

    private val heartRateEntries = ArrayList<Entry>()
    private var entryIndex = 0f          // X-координата (время)
    private var lastHR = 0               // последний показанный пульс

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_session)

        textViewHR = findViewById(R.id.textViewHR)
        chartHR = findViewById(R.id.chartHR)

        setupChart()

        val sensorInfo = SensorHolder.selectedSensorInfo
        SensorHolder.selectedSensorInfo = null

        if (sensorInfo == null) {
            Toast.makeText(this, "Ошибка: данные датчика не переданы", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val samplingRate = 250
        val dataWindow = samplingRate / 2
        val nwins = 30

        callibriMath = CallibriMath(samplingRate, dataWindow, nwins)
        callibriMath?.setPressureAverage(6)

        connectToSensor(sensorInfo)
    }

    private fun setupChart() {
        chartHR.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 5f
                textColor = Color.GRAY
            }

            axisLeft.apply {
                setDrawGridLines(true)
                textColor = Color.GRAY
                axisMinimum = 40f
                axisMaximum = 220f
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
        }

        // Настраиваем LineDataSet без сглаживания и без заливки
        val dataSet = LineDataSet(heartRateEntries, "Пульс").apply {
            color = Color.parseColor("#7C0202")      // ярко-розовый (можно поменять)
            lineWidth = 4f
            setDrawCircles(false)                    // без точек на линии
            setDrawValues(false)                     // без цифр на точках
            mode = LineDataSet.Mode.LINEAR            // ← острая линия (без закруглений)

            // Убираем заполнение полностью
            setDrawFilled(false)                     // ← главное изменение
            // fillColor и fillAlpha можно оставить или удалить — они больше не используются
        }

        val lineData = LineData(dataSet)
        chartHR.data = lineData
    }

    private fun updateHeartRate(hr: Int) {
        lastHR = hr
        textViewHR.text = "$hr уд/мин"

        // Добавляем новую точку
        heartRateEntries.add(Entry(entryIndex, hr.toFloat()))
        entryIndex += 1f

        // Оставляем только последние 60 точек (≈ 1 минута)
        if (heartRateEntries.size > 60) {
            heartRateEntries.removeAt(0)
        }

        // Обновляем данные графика
        val dataSet = chartHR.data.getDataSetByIndex(0) as LineDataSet
        dataSet.values = heartRateEntries

        chartHR.data.notifyDataChanged()
        chartHR.notifyDataSetChanged()

        // Автопрокрутка вправо
        chartHR.setVisibleXRangeMaximum(60f)
        chartHR.moveViewToX(entryIndex)
    }

    private fun connectToSensor(sensorInfo: SensorInfo) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val scanner = Scanner(com.neurosdk2.neuro.types.SensorFamily.SensorLECallibri)
                val sensor = scanner.createSensor(sensorInfo) as Callibri
                currentSensor = sensor
                scanner.close()

                sensor.sensorStateChanged = Sensor.SensorStateChanged { state ->
                    println("STATE: $state")
                }

                sensor.callibriSignalDataReceived = CallibriSignalDataReceived { dataArray ->
                    for (data in dataArray) {
                        for (s in data.samples) {
                            signalBuffer.add(s.toDouble())
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
                            val hr = callibriMath?.getHR()?.toInt() ?: 0
                            callibriMath?.setRRchecked()

                            if (hr > 0) {
                                runOnUiThread {
                                    updateHeartRate(hr)
                                }
                            }
                        }
                    }
                }

                sensor.connect()
                sensor.execCommand(SensorCommand.StartSignal)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewSessionActivity, "Ошибка подключения: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentSensor?.execCommand(SensorCommand.StopSignal)
        currentSensor?.disconnect()
        currentSensor?.close()
        currentSensor = null
        callibriMath = null
        signalBuffer.clear()
    }
}