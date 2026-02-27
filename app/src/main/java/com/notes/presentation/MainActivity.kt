package com.notes.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.notes.design_system.theme.AppTheme
import com.notes.presentation.navigation.NavigationGraph

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                NavigationGraph()
            }
        }
    }
}
