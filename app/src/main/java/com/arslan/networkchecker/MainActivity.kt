package com.arslan.networkchecker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.arslan.networkchecker.ui.theme.NetworkCheckerTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startNetworkService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetworkCheckerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NetworkCheckerScreen(
                        modifier = Modifier.padding(innerPadding),
                        onStartService = { checkPermissionsAndStart() },
                        onStopService = { stopNetworkService() },
                        onClearLogs = { NetworkLog.clear() }
                    )
                }
            }
        }
    }

    private fun checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startNetworkService()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startNetworkService()
        }
    }

    private fun startNetworkService() {
        val intent = Intent(this, NetworkCheckService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopNetworkService() {
        val intent = Intent(this, NetworkCheckService::class.java)
        stopService(intent)
    }
}

@Composable
fun NetworkCheckerScreen(
    modifier: Modifier = Modifier,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onClearLogs: () -> Unit
) {
    val logs by NetworkLog.logs.collectAsState()
    val context = LocalContext.current
    var selectedIntervalMs by rememberSaveable { mutableStateOf(AppSettings.getCheckIntervalMs(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Network Firewall Checker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onStartService) {
                Text("Start Service")
            }
            Button(onClick = onStopService) {
                Text("Stop Service")
            }
        }

        IntervalSelector(
            selectedIntervalMs = selectedIntervalMs,
            onIntervalSelected = { intervalMs ->
                selectedIntervalMs = intervalMs
                AppSettings.setCheckIntervalMs(context, intervalMs)
            }
        )
        
        Button(
            onClick = onClearLogs,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        ) {
            Text("Clear Logs")
        }

        Text(
            text = "Logs (latest first):",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(logs) { log ->
                LogItem(log)
            }
        }
    }
}

@Composable
private fun IntervalSelector(
    selectedIntervalMs: Long,
    onIntervalSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = "Check interval",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(AppSettings.formatIntervalLabel(selectedIntervalMs))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AppSettings.intervalOptionsMs.forEach { intervalMs ->
                    DropdownMenuItem(
                        text = { Text(AppSettings.formatIntervalLabel(intervalMs)) },
                        onClick = {
                            onIntervalSelected(intervalMs)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LogItem(log: NetworkLogEntry) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = formatter.format(Date(log.timestamp))

    val bgColor = if (log.hasNetworkAccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val stateText = if (log.isAppForeground) "Foreground" else "Background"
    val netText = if (log.hasNetworkAccess) "Connected" else "Blocked"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeString,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = stateText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Text(
                text = netText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (log.hasNetworkAccess) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}