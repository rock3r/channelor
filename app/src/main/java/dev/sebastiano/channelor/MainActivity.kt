package dev.sebastiano.channelor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.sebastiano.channelor.ui.DashboardScreen
import dev.sebastiano.channelor.ui.theme.ChannelorTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ChannelorTheme { DashboardScreen() } }
    }
}
