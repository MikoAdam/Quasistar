package com.quasistar.ui.rules

import androidx.compose.foundation.background
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

object RulesUI {
    @Composable
    fun RulesScreen(onBack: () -> Unit) {
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
                "Rules of Quasistar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "1. The goal is to get 3 of your pieces into the enemy's protected zone.",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "2. Pieces can move one block in any direction or jump over pieces diagonally.",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "3. After each move, there is a chance for a 'Game of Life' step to occur, where pieces can spawn or disappear based on their neighbors.",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "4. The probability of a Game of Life step starts at 10% after the first 6 rounds and increases by 2% each turn thereafter.",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F61))
            ) {
                Text("Back", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
