package com.kktdeveloper.callingapp.ui.screens
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kktdeveloper.callingapp.model.CallLogEntry
import com.kktdeveloper.callingapp.ui.theme.*
import com.kktdeveloper.callingapp.viewmodel.CallViewModel

@Composable
fun CallLogsScreen(viewModel: CallViewModel, onCallNumber: (String) -> Unit) {
    val context  = LocalContext.current
    val callLogs by viewModel.callLogs.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.loadCallLogs(context)
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
            == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.loadCallLogs(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Text(
            text = "Recent Calls",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        )

        if (callLogs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No call logs found", color = TextSecondary, fontSize = 16.sp)
            }
        } else {
            LazyColumn {
                items(callLogs) { log ->
                    CallLogItem(log = log, onCallClick = { onCallNumber(log.number) })
                    Divider(color = CardDark, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun CallLogItem(log: CallLogEntry, onCallClick: () -> Unit) {
    val typeColor = when (log.type) {
        "Incoming" -> GreenAccept
        "Outgoing" -> BlueActive
        "Missed"   -> RedDecline
        else       -> TextSecondary
    }
    val typeIcon = when (log.type) {
        "Incoming" -> Icons.Default.CallReceived
        "Outgoing" -> Icons.Default.CallMade
        "Missed"   -> Icons.Default.CallMissed
        else       -> Icons.Default.Call
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(CardDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = typeIcon,
                contentDescription = log.type,
                tint = typeColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.name,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${log.type}  •  ${log.date}",
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(text = log.duration, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call back",
                tint = GreenAccept,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}