package com.quasistar.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(context: Context, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    // Add a loading state
    var isLoading by remember { mutableStateOf(true) }
    val showLabels = remember { mutableStateOf(true) }
    val vibrationEnabled = remember { mutableStateOf(true) }
    val winningCondition = remember { mutableStateOf(1) }

    // Load initial values when the composable is first composed
    LaunchedEffect(Unit) {
        scope.launch {
            showLabels.value = SettingsManager.getShowLabels(context)
            vibrationEnabled.value = SettingsManager.getVibrationEnabled(context)
            winningCondition.value = SettingsManager.getWinningCondition(context)
            isLoading = false // Set loading to false after settings are loaded
        }
    }

    // Show a loading indicator while the settings are being loaded
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        // Only show the settings screen when loading is complete
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF003C46), Color(0xFFEABF6A))
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Settings Item - Show Board Labels
            SettingSwitch(
                title = "Show Board Labels",
                checked = showLabels.value,
                onCheckedChange = { checked ->
                    showLabels.value = checked
                    scope.launch { SettingsManager.setShowLabels(context, checked) }
                }
            )

            // Settings Item - Vibration
            SettingSwitch(
                title = "Vibration",
                checked = vibrationEnabled.value,
                onCheckedChange = { checked ->
                    vibrationEnabled.value = checked
                    scope.launch { SettingsManager.setVibrationEnabled(context, checked) }
                }
            )

            // Settings Item - Winning Condition
            SettingSlider(
                title = "Winning Condition (Pieces in Opponent's Zone)",
                value = winningCondition.value.toFloat(),
                valueRange = 1f..5f,
                onValueChange = { value ->
                    winningCondition.value = value.toInt()
                    scope.launch { SettingsManager.setWinningCondition(context, value.toInt()) }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Reset Settings Button
            Button(
                onClick = {
                    scope.launch {
                        SettingsManager.resetSettings(context)
                        showLabels.value = true
                        vibrationEnabled.value = true
                        winningCondition.value = 1
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Reset Settings", fontSize = 20.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Back Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF017374)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back", fontSize = 20.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFEABF6A),
                    uncheckedThumbColor = Color(0xFF003C46),
                    checkedTrackColor = Color(0xFFEABF6A).copy(alpha = 0.5f),
                    uncheckedTrackColor = Color(0xFF003C46).copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun SettingSlider(title: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = valueRange.endInclusive.toInt() - valueRange.start.toInt() - 1
            )
            Text(
                text = "Value: ${value.toInt()}",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
