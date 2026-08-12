package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.TradeSignal
import com.example.ui.components.InteractiveCandlestickChart
import com.example.ui.components.LiveSignalStatusCard
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodels.TradingViewModel

@Composable
fun LiveSignalScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle(initialValue = viewModel.supportedSymbols.first())
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle(initialValue = "5m")
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle(initialValue = "5d")
    val candles by viewModel.candles.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentPrice by viewModel.currentPrice.collectAsStateWithLifecycle(initialValue = 1.0850)
    val currentSignal by viewModel.currentSignal.collectAsStateWithLifecycle(initialValue = TradeSignal.NO_TRADE)
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle(initialValue = false)

    val timeframes = listOf("1m", "5m", "15m", "1h")
    val periods = listOf("1d", "5d", "1mo")

    var stakeText by remember { mutableStateOf("100") }
    var selectedExpirySecs by remember { mutableStateOf(60) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // TOP ASSET SELECTOR LAZY ROW
        Text("SELECT ASSET", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.supportedSymbols) { marketSymbol ->
                val isSelected = marketSymbol.symbol == selectedSymbol.symbol
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else NavySurface,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectSymbol(marketSymbol) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = marketSymbol.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TIMEFRAME & PERIOD BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                timeframes.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) NavySurfaceVariant else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.selectTimeframe(tf) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tf,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                periods.forEach { p ->
                    val isSelected = p == selectedPeriod
                    Text(
                        text = p,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                        modifier = Modifier
                            .clickable { viewModel.selectPeriod(p) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = { viewModel.refreshData() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Data",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LIVE SIGNAL HERO CARD
        val latestCandle = candles.lastOrNull()
        LiveSignalStatusCard(
            symbol = selectedSymbol.displayName,
            signal = currentSignal,
            price = currentPrice,
            decimals = selectedSymbol.decimals,
            emaFast = latestCandle?.emaFast,
            emaSlow = latestCandle?.emaSlow,
            rsi = latestCandle?.rsi
        )

        Spacer(modifier = Modifier.height(16.dp))

        // INTERACTIVE CANDLESTICK CHART
        InteractiveCandlestickChart(
            candles = candles,
            decimals = selectedSymbol.decimals
        )

        Spacer(modifier = Modifier.height(16.dp))

        // QUICK PAPER TRADE EXECUTION BOX
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "QUICK TRADE SIMULATOR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = stakeText,
                        onValueChange = { stakeText = it },
                        label = { Text("Stake ($)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = NavySurfaceVariant,
                            unfocusedContainerColor = NavySurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text("Expiry", fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(60 to "1m", 300 to "5m").forEach { (secs, label) ->
                                val isSelected = secs == selectedExpirySecs
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else NavySurfaceVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedExpirySecs = secs }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val stake = stakeText.toDoubleOrNull() ?: 100.0
                            viewModel.placePaperTrade(TradeSignal.UP, stake, selectedExpirySecs)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CALL / BUY UP", fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Button(
                        onClick = {
                            val stake = stakeText.toDoubleOrNull() ?: 100.0
                            viewModel.placePaperTrade(TradeSignal.DOWN, stake, selectedExpirySecs)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BearishRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PUT / SELL DOWN", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
