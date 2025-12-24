package com.notes.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.notes.presentation.navigation.NavRoutes
import kotlinx.coroutines.delay

@Composable
internal fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2500)
        navController.navigate(NavRoutes.Screens.HOME) {
            popUpTo(NavRoutes.Screens.SPLASH) { inclusive = true }
        }
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
        }
    }
}
