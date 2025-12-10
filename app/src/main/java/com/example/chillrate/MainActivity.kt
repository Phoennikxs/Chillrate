package com.example.chillrate

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    private val REQUEST_PERMISSIONS = 1

    // Для сканирования
    private val devicesList = mutableListOf<BluetoothDevice>()
    private lateinit var devicesAdapter: ArrayAdapter<String>

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!devicesList.contains(it)) {
                        devicesList.add(it)
                        devicesAdapter.add("${it.name ?: "Неизвестное"}\n${it.address}")
                        devicesAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.button_start).setOnClickListener {
            checkPermissionsAndStart()
        }
    }

    // -----------------------------
    // 1. Разрешения
    // -----------------------------
    private fun checkPermissionsAndStart() {
        val required = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            required += listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        }

        val notGranted = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), REQUEST_PERMISSIONS)
        } else {
            startBluetoothFlow()
        }
    }

    private fun startBluetoothFlow() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth не поддерживается", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return

        if (!bluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableIntent)
        } else {
            makeDeviceDiscoverable()
        }
    }

    // -----------------------------
    // Modern Activity Result для включения Bluetooth
    // -----------------------------
    private val enableBtLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                makeDeviceDiscoverable()
            } else {
                Toast.makeText(this, "Bluetooth не включён", Toast.LENGTH_SHORT).show()
            }
        }

    // -----------------------------
    // Сделать устройство видимым
    // -----------------------------
    private val discoverableLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) @androidx.annotation.RequiresPermission(
            android.Manifest.permission.BLUETOOTH_SCAN
        ) { result ->
            if (result.resultCode != 0) {
                showDevicesDialog()
            } else {
                Toast.makeText(this, "Устройство не стало обнаруживаемым", Toast.LENGTH_SHORT).show()
            }
        }

    private fun makeDeviceDiscoverable() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) return

        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        discoverableLauncher.launch(intent)
    }

    // -----------------------------
    // Диалог с устройствами
    // -----------------------------
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun showDevicesDialog() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return

        // Очистка предыдущих данных
        devicesList.clear()
        devicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        // ListView для диалога
        val listView = ListView(this)
        listView.adapter = devicesAdapter
        listView.setOnItemClickListener @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT) { _, _, position, _ ->
            val device = devicesList[position]
            Toast.makeText(this, "Вы выбрали: ${device.name ?: "Неизвестное"}", Toast.LENGTH_SHORT).show()
            // Здесь позже можно подключаться к устройству
        }

        // Создание диалога
        val dialog = AlertDialog.Builder(this)
            .setTitle("Доступные устройства")
            .setView(listView)
            .setNegativeButton("Закрыть") { d, _ ->
                bluetoothAdapter?.cancelDiscovery()
                d.dismiss()
            }
            .setCancelable(false)
            .create()

        // Регистрация BroadcastReceiver
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        // Начало поиска
        bluetoothAdapter?.startDiscovery()

        dialog.setOnDismissListener @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN) {
            bluetoothAdapter?.cancelDiscovery()
            unregisterReceiver(receiver)
        }

        dialog.show()
    }

    private fun hasPermission(p: String): Boolean =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
}
