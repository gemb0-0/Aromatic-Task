package com.example.aromatictask

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.core.app.ActivityCompat
import com.example.aromatictask.MainActivity.Companion.bluetoothLauncher
import com.example.aromatictask.MainActivity.Companion.permissionLauncher


class BluetoothController(val context: Context) {

    val bluetoothManager by lazy { context.getSystemService(BluetoothManager::class.java) }
    val bluetoothAdapter by lazy { bluetoothManager.adapter }


    companion object {
        var isBounded = mutableStateOf(false)
        var availableDevices = mutableStateListOf<BluetoothDevice>()
    }

    fun checkBluetoothPermission() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permissions.all {
                ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            checkBluetoothStatus()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    @SuppressLint("MissingPermission")
    fun checkBluetoothStatus() {
        availableDevices = bluetoothAdapter.bondedDevices.toMutableStateList()
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothLauncher.launch(enableBtIntent)
        } else
            filterBoundedDevices(availableDevices)
        //  startBluetoothDiscovery()

    }

    @SuppressLint("MissingPermission")
    fun startBluetoothDiscovery() {
        availableDevices.clear()

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.let { devices ->
            availableDevices.addAll(devices)
        }

        if (bluetoothAdapter?.isDiscovering == false) {
            bluetoothAdapter.startDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    private fun filterBoundedDevices(lst: List<BluetoothDevice>) {
                lst.forEach { device ->
            if (device.isAlreadyConnected() &&
                device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.PERIPHERAL
            )
                isBounded.value = true
        }

    }

    //if the device is bounded and online
    private fun BluetoothDevice.isAlreadyConnected(): Boolean {
        return try {
            javaClass.getMethod("isConnected").invoke(this) as? Boolean? ?: false

        } catch (e: Throwable) {
            false
        }
    }


    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice) {
        try {
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                device.createBond()
            }

        } catch (e: Exception) {
            Log.i("Bluetooth ", "failed with exception $e")
        }


    }


}

