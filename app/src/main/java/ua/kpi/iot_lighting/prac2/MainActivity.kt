package ua.kpi.iot_lighting.prac2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ua.kpi.iot_lighting.prac2.navigation.AppRoot
import ua.kpi.iot_lighting.prac2.ui.theme.IoTLightingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IoTLightingTheme {
                AppRoot()
            }
        }
    }
}
