package com.example.chillrate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import com.example.chillrate.data.AppDatabase
import com.example.chillrate.data.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class NewSessionActivity : BaseActivity() {

    private var currentSensor: Callibri? = null
    private var callibriMath: CallibriMath? = null
    private val signalBuffer = mutableListOf<Double>()

    private lateinit var chartHR: LineChart
    private lateinit var textViewHR: TextView
    private lateinit var btnPulseTab: TextView
    private lateinit var btnStressTab: TextView
    private lateinit var btnEndSession: Button

    private val heartRateEntries = ArrayList<Entry>()
    private val stressEntries = ArrayList<Entry>()

    private var entryIndex = 0f
    private var lastHR = 0

    private var currentMode = ChartMode.PULSE

    private var sessionStartTime: Date = Date()
    private val heartRates = mutableListOf<Int>()

    private enum class ChartMode { PULSE, STRESS }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_session)

        textViewHR = findViewById(R.id.textViewHR)
        chartHR = findViewById(R.id.chartHR)
        btnPulseTab = findViewById(R.id.text_pulse_tab)
        btnStressTab = findViewById(R.id.text_stress_tab)
        btnEndSession = findViewById(R.id.btn_session_end)

        setupSideMenu()           // ← из BaseActivity
        setupChart()
        setupTabs()

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

        sessionStartTime = Date()

        connectToSensor(sensorInfo)

        btnEndSession.setOnClickListener { endSession() }
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
                axisMinimum = 0f
                axisMaximum = 220f
            }

            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun setupTabs() {
        btnPulseTab.setOnClickListener { switchToMode(ChartMode.PULSE) }
        btnStressTab.setOnClickListener { switchToMode(ChartMode.STRESS) }
        switchToMode(ChartMode.PULSE)
    }

    private fun switchToMode(mode: ChartMode) {
        currentMode = mode

        if (mode == ChartMode.PULSE) {
            btnPulseTab.background = getDrawable(R.drawable.tab_selected)
            btnStressTab.background = getDrawable(R.drawable.tab_unselected)
            updateChartWithData(heartRateEntries, "Пульс")
        } else {
            btnStressTab.background = getDrawable(R.drawable.tab_selected)
            btnPulseTab.background = getDrawable(R.drawable.tab_unselected)
            updateChartWithData(stressEntries, "Стресс")
        }
    }

    private fun updateChartWithData(entries: ArrayList<Entry>, label: String) {
        val dataSet = LineDataSet(entries, label).apply {
            color = if (currentMode == ChartMode.PULSE) Color.parseColor("#7C0202") else Color.parseColor("#2196F3")
            lineWidth = 4f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(false)
        }

        chartHR.data = LineData(dataSet)
        chartHR.invalidate()
    }

    private fun updateHeartRate(hr: Int) {
        lastHR = hr
        textViewHR.text = "$hr уд/мин"

        heartRates.add(hr)
        heartRateEntries.add(Entry(entryIndex, hr.toFloat()))

        if (heartRateEntries.size > 60) heartRateEntries.removeAt(0)

        if (currentMode == ChartMode.PULSE) {
            updateChartWithData(heartRateEntries, "Пульс")
        }

        entryIndex += 1f

        // Расчёт стресса каждые 8 RR-интервалов
        if (heartRates.size % 8 == 0) {
            val stress = calculateStress()
            updateStress(stress)
        }
    }

    private fun calculateStress(): Float {
        return try {
            callibriMath?.getPressureIndex()?.toFloat() ?: 50f
        } catch (e: Exception) {
            50f
        }
    }

    private fun updateStress(stressValue: Float) {
        stressEntries.add(Entry(entryIndex, stressValue))

        if (stressEntries.size > 60) stressEntries.removeAt(0)

        if (currentMode == ChartMode.STRESS) {
            updateChartWithData(stressEntries, "Стресс")
        }
    }

    private fun connectToSensor(sensorInfo: SensorInfo) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val scanner = Scanner(com.neurosdk2.neuro.types.SensorFamily.SensorLECallibri)
                val sensor = scanner.createSensor(sensorInfo) as? Callibri
                    ?: throw Exception("Не удалось создать сенсор")

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
                        for (i in 0 until 25) chunk[i] = signalBuffer[i]
                        repeat(25) { signalBuffer.removeAt(0) }

                        callibriMath?.pushAndProcessData(chunk)

                        if (callibriMath?.rrDetected() == true) {
                            val hr = callibriMath?.getHR()?.toInt() ?: 0
                            callibriMath?.setRRchecked()

                            if (hr > 0) {
                                runOnUiThread { updateHeartRate(hr) }
                            }
                        }
                    }
                }

                sensor.connect()
                sensor.execCommand(SensorCommand.StartSignal)

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewSessionActivity, "Ошибка подключения: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun endSession() {
        if (heartRates.isEmpty()) {
            Toast.makeText(this, "Сеанс слишком короткий", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val endTime = Date()
        val durationSeconds = ((endTime.time - sessionStartTime.time) / 1000).toInt()
        val avgHR = heartRates.average().toInt()
        val maxHR = heartRates.maxOrNull() ?: avgHR

        // Средний стресс за сеанс
        val avgStress = if (stressEntries.isNotEmpty()) {
            stressEntries.map { it.y }.average().toFloat()
        } else {
            calculateStress() // последнее значение
        }

        val hrDataJson = heartRates.joinToString(",")

        val sessionEntity = SessionEntity(
            userEmail = getCurrentUserEmail(),
            startTime = sessionStartTime,
            endTime = endTime,
            durationSeconds = durationSeconds,
            averageHR = avgHR,
            maxHR = maxHR,
            stressLevel = avgStress,           // ← сохраняем стресс
            hrDataJson = hrDataJson,
            notes = null
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@NewSessionActivity)
                db.sessionDao().insertSession(sessionEntity)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@NewSessionActivity,
                        "Сеанс сохранён (${durationSeconds} сек)",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this@NewSessionActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NewSessionActivity, "Ошибка сохранения сеанса", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getCurrentUserEmail(): String {
        return getSharedPreferences("app", MODE_PRIVATE)
            .getString("user_email", "unknown@user.com") ?: "unknown@user.com"
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