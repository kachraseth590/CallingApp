package com.kktdeveloper.callingapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kktdeveloper.callingapp.ui.theme.*
import com.kktdeveloper.callingapp.viewmodel.CallViewModel

@Composable
fun ActiveCallScreen(
    number: String,
    callerName: String,
    viewModel: CallViewModel
) {
    val callDuration by viewModel.callDurationSeconds.collectAsState()
    val isMuted     by viewModel.isMuted.collectAsState()
    val isSpeaker   by viewModel.isSpeakerOn.collectAsState()

    val displayName = if (callerName.isNotEmpty() && callerName != number) callerName else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(modifier = Modifier.height(32.dp))


        Column(horizontalAlignment = Alignment.CenterHorizontally) {


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(GreenAccept, CircleShape)
                )
                Text(
                    text = "Active Call",
                    color = GreenAccept,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))


            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(CardDark, CircleShape)
                    .border(2.dp, GreenAccept, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (displayName != null) {
                Text(
                    text = displayName,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = number,
                color = if (displayName != null) TextSecondary else TextPrimary,
                fontSize = if (displayName != null) 18.sp else 28.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(20.dp))


            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardDark
            ) {
                Text(
                    text = viewModel.formatDuration(callDuration),
                    color = GreenAccept,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }


        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                CallToggleButton(
                    icon      = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label     = if (isMuted) "Unmute" else "Mute",
                    isActive  = isMuted,
                    activeColor = Color(0xFFFFA726),
                    onClick   = { viewModel.toggleMute() }
                )


                CallToggleButton(
                    icon      = if (isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    label     = if (isSpeaker) "Speaker On" else "Speaker",
                    isActive  = isSpeaker,
                    activeColor = BlueActive,
                    onClick   = { viewModel.toggleSpeaker() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))


            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FloatingActionButton(
                    onClick = { viewModel.endCall() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    containerColor = RedDecline
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "End Call", color = TextSecondary, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CallToggleButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (isActive) activeColor.copy(alpha = 0.2f) else CardDark,
            modifier = Modifier
                .size(64.dp)
                .then(
                    if (isActive) Modifier.border(1.5.dp, activeColor, CircleShape)
                    else Modifier
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) activeColor else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = if (isActive) activeColor else TextSecondary,
            fontSize = 12.sp
        )
    }
}