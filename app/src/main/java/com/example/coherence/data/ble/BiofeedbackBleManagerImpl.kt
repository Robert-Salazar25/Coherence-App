package com.example.coherence.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.example.coherence.domain.model.BiofeedbackData
import com.example.coherence.domain.repository.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.io.path.name

@SuppressLint("MissingPermission")
class BiofeedbackBleManagerImpl @Inject constructor(
    private val context: Context
) : BiofeedbackBleManager {

    // --- 1. CONFIGURACIÓN DE UUIDs ---
    companion object {
        // UUIDs estándar UART (Nordic) que coinciden con tu Arduino
        val SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        val CCCD_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null

    // --- 2. ESTADOS (Flows) ---
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState = _connectionState.asStateFlow()

    private val _biofeedbackData = MutableStateFlow(BiofeedbackData())
    override val biofeedbackData = _biofeedbackData.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100)
    override val batteryLevel = _batteryLevel.asStateFlow()

    // --- 3. CALLBACK DEL BLUETOOTH ---
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = ConnectionState.Connected
                Log.d("BLE", "Conectado. Buscando servicios...")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = ConnectionState.Disconnected
                Log.d("BLE", "Desconectado")
                close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true)
                        val descriptor = characteristic.getDescriptor(CCCD_DESCRIPTOR_UUID)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                            Log.d("BLE", "Notificaciones activadas")
                        }
                    }
                }
            }
        }

        // Soporte para versiones antiguas de Android
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                parseData(String(characteristic.value))
            }
        }

        // Soporte para Android 13+
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            parseData(String(value))
        }
    }

    // --- 4. PROCESAR LOS DATOS (SINCRONIZADO CON ARDUINO) ---
    private fun parseData(rawString: String) {
        try {
            // FORMATO ARDUINO: "TEMP, HR, SPO2, HRV, GSR, STATUS"
            // Ejemplo: "36.5, 72, 98, 45, 35, BUENA"
            // Nota: GSR viene multiplicado por 10 (35 = 3.5 uS)

            val parts = rawString.split(",")

            // Necesitamos al menos 5 partes (los números)
            if (parts.size >= 5) {
                val temp = parts[0].toFloatOrNull() ?: 0f
                val hr = parts[1].toIntOrNull() ?: 0
                val spo2 = parts[2].toIntOrNull() ?: 0

                // ACTUALIZADO: HRV ahora puede ser Float en tu modelo, pero Arduino manda entero
                // Lo leemos como Float para ser consistentes con el modelo
                val hrv = parts[3].toFloatOrNull() ?: 0f

                // ACTUALIZADO: GSR (Touch) viene como entero (ej. 35)
                val touch = parts[4].toIntOrNull() ?: 0

                // Estado en la posición 5
                val status = parts.getOrElse(5) { "" }.trim()

                // En Arduino pusimos: String status = fingerDetected ? "BUENA" : "SIN_DEDO";
                val isStable = status.equals("BUENA", ignoreCase = true)

                // Actualizamos el Flow
                _biofeedbackData.value = BiofeedbackData(
                    timestamp = System.currentTimeMillis(),
                    heartRate = hr,
                    spo2 = spo2,
                    heartRateVariability = hrv, // Ahora pasamos Float
                    temperature = temp,
                    touchValue = touch, // Pasamos el valor crudo (35), el ViewModel lo divide
                    isStable = isStable,
                    coherence = 0f // Se calcula en el ViewModel
                )
            }
        } catch (e: Exception) {
            Log.e("BLE_ERROR", "Error al parsear: ${e.message}")
        }
    }

    // --- ESCÁNER ---
    private val bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner?
        get() = bluetoothAdapter?.bluetoothLeScanner

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            val deviceName = device.name ?: "Sin Nombre"

            // Buscamos por nombre "BIOFEEDBACK_ESP32" (o el que pusiste en Arduino) o por UUID
            // Nota: En Arduino pusimos BLEDevice::init("BIOFEEDBACK_ESP32");
            if (deviceName == "BIOFEEDBACK_ESP32" || deviceName == "GILA_BLE" ||
                result.scanRecord?.serviceUuids?.contains(android.os.ParcelUuid(SERVICE_UUID)) == true) {

                Log.d("BLE_MATCH", "¡ENCONTRADO! Conectando a: $deviceName")
                bluetoothLeScanner?.stopScan(this)
                CoroutineScope(Dispatchers.IO).launch {
                    connect(device.address)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "Error al escanear: $errorCode")
            _connectionState.value = ConnectionState.Error("Error al escanear: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            _connectionState.value = ConnectionState.Error("Bluetooth apagado")
            return
        }

        _connectionState.value = ConnectionState.Connecting
        Log.d("BLE", "Iniciando escaneo...")

        val settings = android.bluetooth.le.ScanSettings.Builder()
            .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner?.startScan(null, settings, scanCallback)

        // Timeout de 10s para no gastar batería infinitamente
        delay(10000)
        if (_connectionState.value is ConnectionState.Connecting) {
            bluetoothLeScanner?.stopScan(scanCallback)
            _connectionState.value = ConnectionState.Error("Dispositivo no encontrado")
        }
    }

    // --- 5. FUNCIONES DE CONEXIÓN ---
    override suspend fun connect(address: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return

        try {
            _connectionState.value = ConnectionState.Connecting
            val device = bluetoothAdapter!!.getRemoteDevice(address)
            // autoConnect = false para conexión directa más rápida
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } catch (e: IllegalArgumentException) {
            _connectionState.value = ConnectionState.Error("Dirección MAC inválida")
        }
    }

    override suspend fun disconnect() {
        bluetoothGatt?.disconnect()
        // Limpiamos datos al desconectar para que la UI no muestre datos viejos
        _biofeedbackData.value = BiofeedbackData()
    }

    private fun close() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}