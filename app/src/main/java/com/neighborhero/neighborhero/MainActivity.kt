package com.neighborhero.neighborhero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.neighborhero.neighborhero.navigation.AppNavigation
import com.neighborhero.neighborhero.ui.theme.NeighborheroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeighborheroTheme {
                AppNavigation()
            }
        }
    }
}
