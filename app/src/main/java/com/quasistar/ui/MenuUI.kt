package com.quasistar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MenuUI {
    @Composable
    fun MainMenuScreen(
        onStartLocalGame: () -> Unit,
        onStartAIGame: () -> Unit,
        onStartOnlineGame: () -> Unit,
        onViewRules: () -> Unit,
        onAboutUs: () -> Unit,
        onSettings: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF003C46), Color(0xFFEABF6A))
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Quasistar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartLocalGame,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF017374))
            ) {
                Text("Local Multiplayer", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartAIGame,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A83EA))
            ) {
                Text("Single Player (AI)", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartOnlineGame,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61))
            ) {
                Text("Online (Coming Soon)", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewRules,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEABF6A))
            ) {
                Text("Rules", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A83EA))
            ) {
                Text("Settings", fontSize = 18.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "© 2024 Quasistar. All rights reserved.",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Follow us:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                "Facebook | Instagram | YouTube",
                fontSize = 14.sp,
                color = Color(0xFFEABF6A),
                modifier = Modifier.clickable {
                    // Add actual social media link actions here
                },
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
