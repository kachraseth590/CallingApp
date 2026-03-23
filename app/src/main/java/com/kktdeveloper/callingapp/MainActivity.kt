package com.kktdeveloper.callingapp

import android.Manifest
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kktdeveloper.callingapp.model.CallState
import com.kktdeveloper.callingapp.ui.screens.*
import com.kktdeveloper.callingapp.ui.theme.CallingAppTheme
import com.kktdeveloper.callingapp.ui.theme.DarkBg
import com.kktdeveloper.callingapp.viewmodel.CallViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CallViewModel by viewModels()

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)


        CallEndCallback.onCallEnded = {
            viewModel.endCall()
        }


        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(CallStateReceiver(), filter)

        setContent {
            CallingAppTheme {
                CallingAppNavHost(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CallEndCallback.onCallEnded = null
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CallingAppNavHost(viewModel: CallViewModel) {
    val callState by viewModel.callState.collectAsState(initial = CallState.Idle)

    AnimatedContent(
        targetState = callState,
        transitionSpec = {
            when (targetState) {
                is CallState.Idle ->
                    slideInVertically { it } + fadeIn(tween(300)) togetherWith
                            slideOutVertically { -it } + fadeOut(tween(300))

                is CallState.Calling, is CallState.Ringing ->
                    slideInVertically { -it } + fadeIn(tween(300)) togetherWith
                            slideOutVertically { it } + fadeOut(tween(300))

                is CallState.Active ->
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))

                is CallState.Ended ->
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        label = "CallStateNavigation"
    ) { state ->
        when (state) {
            is CallState.Idle -> DialPadScreen(viewModel = viewModel)

            is CallState.Calling -> OutgoingCallScreen(
                number = state.number,
                viewModel = viewModel
            )

            is CallState.Ringing -> IncomingCallScreen(
                callerName = state.callerName,
                callerNumber = state.callerNumber,
                viewModel = viewModel
            )

            is CallState.Active -> ActiveCallScreen(
                number = state.number,
                callerName = state.callerName,
                viewModel = viewModel
            )

            is CallState.Ended -> CallEndedScreen()
        }
    }
}