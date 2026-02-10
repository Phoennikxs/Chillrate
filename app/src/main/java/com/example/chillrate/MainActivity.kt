package com.example.chillrate

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.BluetoothLeScanner
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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

class MainActivity : BaseActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var scanner: BluetoothLeScanner? = null
    private val scannedDevices = mutableListOf<BluetoothDevice>()
    private var gatt: BluetoothGatt? = null


    private val REQUEST_PERMISSIONS = 1001

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
            startBleScanWithPermissions()
        }

        setupSideMenu()
    }




    private fun startBleScanWithPermissions() {


        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }
        val notGranted = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), REQUEST_PERMISSIONS)
        } else {
            startBleScan()
        }
    }

    private fun startBleScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Bluetooth не включён или не поддерживается", Toast.LENGTH_SHORT).show()
            return
        }
        scanner = bluetoothAdapter!!.bluetoothLeScanner
        scannedDevices.clear()
        scanner?.startScan(scanCallback)
        Toast.makeText(this, "Сканирование запущено…", Toast.LENGTH_SHORT).show()

        // Прекратить скан через 10 секунд
        window.decorView.postDelayed({
            stopScanAndShowDialog()
        }, 10_000)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: result.scanRecord?.deviceName
            if (name != null && name.contains("Callibri", ignoreCase = true)) {
                if (!scannedDevices.any { it.address == device.address }) {
                    scannedDevices.add(device)
                }
            }
        }
    }

    private fun stopScanAndShowDialog() {
        scanner?.stopScan(scanCallback)
        if (scannedDevices.isEmpty()) {
            Toast.makeText(this, "Устройств Callibri не найдено", Toast.LENGTH_SHORT).show()
        } else {
            showDeviceListDialog()
        }
    }

    private fun showDeviceListDialog() {
        val items = scannedDevices.map { d ->
            "${d.name ?: "Unknown"} — ${d.address}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Выберите устройство Callibri")
            .setItems(items) { dialog, which ->
                val device = scannedDevices[which]
                connectToDevice(device)
            }
            .setCancelable(true)
            .show()
    }

    private fun connectToDevice(device: BluetoothDevice) {
        // Подключение по BLE — transport = LE
        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(this, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(g, status, newState)
            if (newState == android.bluetooth.BluetoothProfile.STATE_CONNECTED) {
                g.discoverServices()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Подключено к ${g.device.address}", Toast.LENGTH_SHORT).show()
                }
            } else if (newState == android.bluetooth.BluetoothProfile.STATE_DISCONNECTED) {
                g.close()
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(g, status)
            // Здесь позже можно проверить, что у устройства есть нужные сервисы (ECG/HR и т.п.)
        }

        // При необходимости: чтение / подписка на характеристики
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            val denied = grantResults.any { it != PackageManager.PERMISSION_GRANTED }
            if (denied) {
                Toast.makeText(this, "Нужны разрешения для BLE", Toast.LENGTH_SHORT).show()
            } else {
                startBleScan()
            }
        }
    }
}
