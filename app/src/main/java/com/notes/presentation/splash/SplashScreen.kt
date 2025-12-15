package com.notes.presentation.splash

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
internal fun SplashScreen(onNavigateHome: () -> Unit) {
    Scaffold {
        Button(onClick = onNavigateHome) {
            Text("Ir para Home")
        }
    }
}
