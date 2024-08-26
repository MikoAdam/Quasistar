package com.quasistar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quasistar.settings.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinishLoading: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            // Simulate loading settings
            SettingsManager.getShowLabels(context)
            SettingsManager.getWinningCondition(context)

            delay(1000) // Simulate loading delay
            isLoaded = true
            onFinishLoading() // Move to the next screen after loading is done
        }
    }

    // Splash screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF003C46)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Quasistar",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
