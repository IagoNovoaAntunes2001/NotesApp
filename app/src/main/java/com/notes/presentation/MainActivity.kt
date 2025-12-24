package com.notes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.notes.design_system.theme.AppTheme
import com.notes.presentation.navigation.AppNavHost

internal class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                AppNavHost(navController = rememberNavController())
            }
        }
    }
}
