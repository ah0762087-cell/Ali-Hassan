package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainScreen
import com.example.ui.theme.BinaryTrendBotTheme
import com.example.ui.viewmodels.TradingViewModel

class MainActivity : ComponentActivity() {

    private val tradingViewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BinaryTrendBotTheme {
                MainScreen(viewModel = tradingViewModel)
            }
        }
    }
}
