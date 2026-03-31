package com.notes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.notes.design_system.theme.AppTheme
import com.notes.presentation.navigation.NavigationGraph
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint permite que o Hilt injete dependências nesta Activity
// e em qualquer Fragment/ViewModel que ela hospedar.
// Usamos ComponentActivity (não AppCompatActivity) pois o app é 100% Compose —
// AppCompatActivity adiciona overhead de Fragments/Views que não usamos aqui.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                NavigationGraph()
            }
        }
    }
}
