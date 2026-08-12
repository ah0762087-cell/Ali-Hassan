package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.PaperTradeStatus
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
fun PaperTradingScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val portfolio by viewModel.portfolio.collectAsStateWithLifecycle(initialValue = com.example.data.local.PortfolioEntity())
    val trades by viewModel.paperTrades.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentPrice by viewModel.currentPrice.collectAsStateWithLifecycle(initialValue = 1.0850)

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
                        text = "PAPER TRADING WORKSTATION",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Risk-free virtual execution environment",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { viewModel.resetPaperTrading() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = BearishRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Balance", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        // PORTFOLIO HEADER CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("VIRTUAL ACCOUNT BALANCE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", portfolio.balance)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalTrades = portfolio.winCount + portfolio.lossCount
                    val winRate = if (totalTrades > 0) (portfolio.winCount.toDouble() / totalTrades.toDouble()) * 100.0 else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pnlColor = if (portfolio.totalProfitLoss >= 0) BullishGreen else BearishRed
                        KpiCard(
                            title = "Total P&L",
                            value = "${if (portfolio.totalProfitLoss >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", portfolio.totalProfitLoss)}",
                            valueColor = pnlColor,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Win Rate",
                            value = "${String.format(Locale.US, "%.1f", winRate)}%",
                            valueColor = if (winRate >= 50) BullishGreen else BearishRed,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Trades W/L",
                            value = "${portfolio.winCount} / ${portfolio.lossCount}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ACTIVE OPEN CONTRACTS
        val openTrades = trades.filter { it.status == PaperTradeStatus.OPEN }
        if (openTrades.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE OPEN CONTRACTS (${openTrades.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            items(openTrades) { trade ->
                val now = System.currentTimeMillis()
                val remainingSecs = ((trade.expiryTimeMs - now) / 1000).coerceAtLeast(0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = trade.symbol, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(
                                    text = "Direction: ${if (trade.direction == TradeSignal.UP) "🟢 CALL (UP)" else "🔴 PUT (DOWN)"}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (trade.direction == TradeSignal.UP) BullishGreen else BearishRed,
                                    fontSize = 12.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Expiry in: ${remainingSecs}s", fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), fontSize = 14.sp)
                                Text(text = "Stake: $${trade.stake}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Entry Price: $${trade.entryPrice}", fontSize = 11.sp, color = TextMuted)
                            Text(text = "Live Price: $${currentPrice}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // COMPLETED CONTRACTS HISTORY
        val completedTrades = trades.filter { it.status != PaperTradeStatus.OPEN }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "EXECUTION HISTORY (${completedTrades.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        if (completedTrades.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Text(
                        text = "No completed trades yet. Execute a paper trade on the Live Signals screen!",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(completedTrades) { trade ->
                val isWin = trade.status == PaperTradeStatus.WIN
                val statusColor = if (isWin) BullishGreen else BearishRed

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
                            Text(text = "${trade.symbol} (${dateFormat.format(Date(trade.startTimeMs))})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text(
                                text = "${if (trade.direction == TradeSignal.UP) "🟢 CALL" else "🔴 PUT"} | Entry: ${trade.entryPrice} -> Exit: ${trade.exitPrice ?: "--"}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = trade.status.name,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${if (trade.pnl >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", trade.pnl)}",
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
