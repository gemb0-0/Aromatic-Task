package com.example.aromatictask

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aromatictask.BluetoothController.Companion.availableDevices
import com.example.aromatictask.ui.theme.AromaticTaskTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {


    companion object {
        lateinit var bluetoothLauncher: ActivityResultLauncher<Intent>
        lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    }

    private val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)

    }
    private val openSettingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

    //rename it later
    val bs = BluetoothController(this)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.values.all { it }) {
                    bs.checkBluetoothStatus()
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        bluetoothLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) {
                    Toast.makeText(this, "enable Bluetooth", Toast.LENGTH_SHORT).show()
                } else
                    bs.startBluetoothDiscovery()
            }

        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        bs.checkBluetoothPermission()

        setContent {
            AromaticTaskTheme {
                val txt = remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (!BluetoothController.isBounded.value)
                                "Available and saved devices ${availableDevices.size}"
                            else "Type something",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 75.dp)
                                .weight(2f)
                        )


                        if (!BluetoothController.isBounded.value) {
                            DeviceList(availableDevices, bs)
                            Row(modifier = Modifier
                                .weight(2f)
                                .padding(top = 10.dp)) {
                                Button(onClick = {
                                    bs.startBluetoothDiscovery()
                                }) { Text("start scanning") }
                                Button(onClick = {
                                    startActivity(openSettingsIntent)
                                }, modifier = Modifier.padding(horizontal = 7.dp))
                                { Text("open bluetooth settings") }
                            }

                        } else {
                            OutlinedTextField(value = txt.value, onValueChange = { txt.value = it })
                            Spacer(modifier = Modifier.weight(2f))
                        }
                    }

                }

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (_: Exception) {

        }
    }

}

@Composable
fun DeviceList(devices: List<BluetoothDevice>, bs: BluetoothController) {


    LazyColumn(modifier = Modifier.height(555.dp)) {
        items(devices, key = { it.address }) { device ->
            DeviceItem(device, bs)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(device: BluetoothDevice, bs: BluetoothController) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 7.dp),
        onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                bs.connect(device)
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.name ?: "Unknown Device",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "MAC Address: ${device.address}",
                fontSize = 12.sp
            )

            Text(
                text = "Bound State: ${
                    getDeviceStatus(device.bondState)
                }",
                fontSize = 12.sp
            )
        }
    }


}

private fun getDeviceStatus(bondState: Int): String {
    return when (bondState) {
        BluetoothDevice.BOND_BONDED -> "Paired Before"
        BluetoothDevice.BOND_BONDING -> "Pairing"
        BluetoothDevice.BOND_NONE -> "Not Paired"
        else -> "Unknown"
    }
}




