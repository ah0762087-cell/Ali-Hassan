package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.models.TradeSignal
import com.example.ui.screens.BacktestScreen
import com.example.ui.screens.LiveSignalScreen
import com.example.ui.screens.PaperTradingScreen
import com.example.ui.screens.StrategyBuilderScreen
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodels.TradingViewModel

enum class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LIVE("Live Signals", Icons.Default.ShowChart),
    BACKTEST("Backtest", Icons.Default.QueryStats),
    PAPER_TRADING("Paper Trading", Icons.Default.AccountBalanceWallet),
    STRATEGY("Strategy", Icons.Default.Tune)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.LIVE) }

    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle(initialValue = viewModel.supportedSymbols.first())
    val currentPrice by viewModel.currentPrice.collectAsStateWithLifecycle(initialValue = 1.0850)
    val currentSignal by viewModel.currentSignal.collectAsStateWithLifecycle(initialValue = TradeSignal.NO_TRADE)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.trend_bot_logo_1786492080733),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BINARY TREND BOT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${selectedSymbol.displayName} • ${String.format("%.${selectedSymbol.decimals}f", currentPrice)}",
                                fontSize = 11.sp,
                                color = when (currentSignal) {
                                    TradeSignal.UP -> BullishGreen
                                    TradeSignal.DOWN -> BearishRed
                                    TradeSignal.NO_TRADE -> TextSecondary
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Live pulsing green dot
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(NavySurfaceVariant, RoundedCornerShape(20.dp))
                            .border(1.dp, NavyCardBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(BullishGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE MARKET",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavySurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NavySurface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = NavySurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.LIVE -> LiveSignalScreen(viewModel = viewModel)
                NavigationTab.BACKTEST -> BacktestScreen(viewModel = viewModel)
                NavigationTab.PAPER_TRADING -> PaperTradingScreen(viewModel = viewModel)
                NavigationTab.STRATEGY -> StrategyBuilderScreen(viewModel = viewModel)
            }
        }
    }
}
