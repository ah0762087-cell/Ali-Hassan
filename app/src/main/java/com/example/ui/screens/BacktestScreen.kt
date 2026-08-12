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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.KpiCard
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodels.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BacktestScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle(initialValue = viewModel.supportedSymbols.first())
    val backtestResult by viewModel.backtestResult.collectAsStateWithLifecycle(initialValue = null)
    val savedHistory by viewModel.backtestHistory.collectAsStateWithLifecycle(initialValue = emptyList())

    var filterResult by remember { mutableStateOf<String>("ALL") } // ALL, WIN, LOSS

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STRATEGY BACKTESTER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Historical evaluation for ${selectedSymbol.displayName}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.runBacktest() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RUN BACKTEST", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        backtestResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BACKTEST PERFORMANCE SUMMARY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )

                            val winRateColor = if (result.winRate >= 55.0) BullishGreen else if (result.winRate >= 45.0) Color(0xFFF59E0B) else BearishRed
                            Text(
                                text = "WIN RATE: ${String.format(Locale.US, "%.2f", result.winRate)}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = winRateColor
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCard("Total Trades", "${result.totalTrades}", modifier = Modifier.weight(1f))
                            KpiCard("Wins", "${result.wins}", valueColor = BullishGreen, modifier = Modifier.weight(1f))
                            KpiCard("Losses", "${result.losses}", valueColor = BearishRed, modifier = Modifier.weight(1f))
                            KpiCard("Max Streak", "${result.consecutiveWins}", valueColor = BullishGreen, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // TRADE LOG TABLE HEADER & FILTER
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRADE LOG (" + (result.logs.size) + ")",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("ALL", "WIN", "LOSS").forEach { opt ->
                                val isSel = opt == filterResult
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSel) NavySurfaceVariant else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { filterResult = opt }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) TextPrimary else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySurfaceVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("# / Time", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1.2f))
                        Text("Signal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(0.8f))
                        Text("Entry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1f))
                        Text("Exit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1f))
                        Text("Result", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(0.8f))
                    }
                }
            }

            val filteredLogs = when (filterResult) {
                "WIN" -> result.logs.filter { it.isWin }
                "LOSS" -> result.logs.filter { !it.isWin }
                else -> result.logs
            }

            items(filteredLogs.takeLast(30).reversed()) { log ->
                val decimals = selectedSymbol.decimals
                val resultText = if (log.isWin) "WIN" else "LOSS"
                val resultColor = if (log.isWin) BullishGreen else BearishRed

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${log.index} ${dateFormat.format(Date(log.timestamp))}",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1.2f)
                            )

                            val signalStr = if (log.signal == TradeSignal.UP) "🟢 UP" else "🔴 DOWN"
                            Text(
                                text = signalStr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (log.signal == TradeSignal.UP) BullishGreen else BearishRed,
                                modifier = Modifier.weight(0.8f)
                            )

                            Text(
                                text = String.format(Locale.US, "%.${decimals}f", log.entryPrice),
                                fontSize = 11.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = String.format(Locale.US, "%.${decimals}f", log.exitPrice),
                                fontSize = 11.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = resultText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = resultColor,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                        HorizontalDivider(color = NavyCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }

        // SAVED BACKTEST HISTORY FROM ROOM DB
        if (savedHistory.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SAVED BACKTEST HISTORY (ROOM DATABASE)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            items(savedHistory.take(10)) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${record.symbol} (${record.timeframe})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text(text = "Strategy: ${record.strategyName}", color = TextSecondary, fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val winRateColor = if (record.winRate >= 50.0) BullishGreen else BearishRed
                            Text(text = "Win Rate: ${String.format(Locale.US, "%.2f", record.winRate)}%", fontWeight = FontWeight.Bold, color = winRateColor, fontSize = 13.sp)
                            Text(text = "${record.wins}W / ${record.losses}L (${record.totalTrades} trades)", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
