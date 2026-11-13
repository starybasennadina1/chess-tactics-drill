package com.chesstactics.drill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chesstactics.drill.core.di.ServiceLocator
import com.chesstactics.drill.core.theme.AppTheme
import com.chesstactics.drill.presentation.app.AppRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        setContent {
            AppTheme { AppRoot() }
        }
    }
}

