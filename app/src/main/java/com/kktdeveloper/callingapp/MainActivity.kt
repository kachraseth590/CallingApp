package com.kktdeveloper.callingapp

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.kktdeveloper.callingapp.model.CallState
import com.kktdeveloper.callingapp.ui.screens.*
import com.kktdeveloper.callingapp.ui.theme.CallingAppTheme
import com.kktdeveloper.callingapp.ui.theme.CardDark
import com.kktdeveloper.callingapp.ui.theme.DarkBg
import com.kktdeveloper.callingapp.ui.theme.TextSecondary
import com.kktdeveloper.callingapp.viewmodel.CallViewModel

sealed class BottomTab(val label: String, val icon: ImageVector) {
    object Dialer   : BottomTab("Dialer",   Icons.Default.Dialpad)
    object CallLogs : BottomTab("Recents",  Icons.Default.History)
    object Contacts : BottomTab("Contacts", Icons.Default.Contacts)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CallViewModel by viewModels()
    private lateinit var callStateReceiver: CallStateReceiver
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionsLauncher.launch(arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
        ))


        CallEndCallback.onCallEnded = { viewModel.endCall() }
        CallEndCallback.onIncomingCall = { number -> viewModel.onRealIncomingCall(number, applicationContext) }
        CallEndCallback.onCallConnected = { viewModel.onCallConnected() }

        callStateReceiver = CallStateReceiver()
        registerReceiver(callStateReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))

        setContent {
            CallingAppTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callStateReceiver)
        CallEndCallback.onCallEnded = null
        CallEndCallback.onIncomingCall = null
        CallEndCallback.onCallConnected = null
    }
}

@Composable
fun MainScreen(viewModel: CallViewModel) {
    val callState by viewModel.callState.collectAsState(initial = CallState.Idle)
    val context   = LocalContext.current


    if (callState !is CallState.Idle) {
        CallStateScreen(callState = callState, viewModel = viewModel)
        return
    }


    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Dialer) }
    val tabs = listOf(BottomTab.Dialer, BottomTab.CallLogs, BottomTab.Contacts)

    val onCallNumber: (String) -> Unit = { number ->
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        context.startActivity(intent)
        viewModel.setDialedNumber(number)
        viewModel.startOutgoingCall(context)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = CardDark) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick  = { selectedTab = tab },
                        icon     = { Icon(tab.icon, contentDescription = tab.label) },
                        label    = { Text(tab.label) },
                        colors   = NavigationBarItemDefaults.colors(
                            selectedIconColor   = androidx.compose.ui.graphics.Color.White,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor   = androidx.compose.ui.graphics.Color.White,
                            unselectedTextColor = TextSecondary,
                            indicatorColor      = androidx.compose.ui.graphics.Color(0xFF1976D2)
                        )
                    )
                }
            }
        },
        containerColor = DarkBg
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                BottomTab.Dialer   -> DialPadScreen(viewModel = viewModel)
                BottomTab.CallLogs -> CallLogsScreen(viewModel = viewModel, onCallNumber = onCallNumber)
                BottomTab.Contacts -> ContactsScreen(viewModel = viewModel, onCallNumber = onCallNumber)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CallStateScreen(callState: CallState, viewModel: CallViewModel) {
    AnimatedContent(
        targetState = callState,
        transitionSpec = {
            when (targetState) {
                is CallState.Calling, is CallState.Ringing ->
                    slideInVertically { -it } + fadeIn(tween(300)) togetherWith
                            slideOutVertically { it } + fadeOut(tween(300))
                is CallState.Active ->
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                is CallState.Ended ->
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                else ->
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            }
        },
        modifier = Modifier.fillMaxSize().background(DarkBg),
        label = "CallStateNavigation"
    ) { state ->
        when (state) {
            is CallState.Calling -> OutgoingCallScreen(
                number = state.number, viewModel = viewModel)
            is CallState.Ringing -> IncomingCallScreen(
                callerName = state.callerName,
                callerNumber = state.callerNumber,
                viewModel = viewModel)
            is CallState.Active  -> ActiveCallScreen(
                number = state.number,
                callerName = state.callerName,
                viewModel = viewModel)
            is CallState.Ended   -> CallEndedScreen()
            else -> {}
        }
    }
}