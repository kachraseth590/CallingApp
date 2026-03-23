package com.kktdeveloper.callingapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kktdeveloper.callingapp.model.getContactName
import com.kktdeveloper.callingapp.ui.theme.*
import com.kktdeveloper.callingapp.viewmodel.CallViewModel

@Composable
fun OutgoingCallScreen(number: String, viewModel: CallViewModel) {
    val contactName = remember(number) { getContactName(number) }
    val displayName = if (contactName != number) contactName else null

    // Pulsing animation for the avatar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(modifier = Modifier.height(48.dp))


        Column(horizontalAlignment = Alignment.CenterHorizontally) {


            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(CardDark, CircleShape)
                    .border(2.dp, BlueActive.copy(alpha = 0.5f), CircleShape),
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

            Spacer(modifier = Modifier.height(16.dp))


            CallingStatusText()
        }


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
    }
}

@Composable
private fun CallingStatusText() {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    val dots = ".".repeat(dotCount)
    Text(
        text = "Calling$dots",
        color = BlueActive,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}