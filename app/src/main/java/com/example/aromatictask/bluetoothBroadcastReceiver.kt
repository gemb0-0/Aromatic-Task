package com.example.aromatictask

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.aromatictask.BluetoothController.Companion.availableDevices
import com.example.aromatictask.BluetoothController.Companion.isBounded


val receiver = object : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val action: String = intent.action.toString()
        when (action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)!!
                if (!availableDevices.any { it.address == device.address }) {
                    availableDevices.add(device)
                    Log.i("Bluetooth", "new device spotted ${device.name}")
                }

            }

            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                Log.i(
                    "Bluetooth",
                    "ACTION_BOND_STATE_CHANGED ${BluetoothDevice.ACTION_BOND_STATE_CHANGED}"
                )

                val bondState =
                    intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    isBounded.value = true
                    Log.i("Bluetooth", "Device paired successfully ")

                } else if (bondState == BluetoothDevice.BOND_NONE) {
                    Log.e("Bluetooth", "pairing failed")
                    isBounded.value = false
                }
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                Toast.makeText(context, "finished scanning ", Toast.LENGTH_SHORT).show()
            }


        }
    }
}


