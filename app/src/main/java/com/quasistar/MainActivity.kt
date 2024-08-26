package com.quasistar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.quasistar.ui.MainScreen
import com.quasistar.ui.theme.QuasistarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuasistarTheme {
                MainScreen()
            }
        }
    }
}
