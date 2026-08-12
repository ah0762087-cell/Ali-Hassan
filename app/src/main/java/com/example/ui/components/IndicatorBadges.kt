package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.TradeSignal
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedContainer
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenContainer
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveSignalStatusCard(
    symbol: String,
    signal: TradeSignal,
    price: Double,
    decimals: Int = 5,
    emaFast: Double?,
    emaSlow: Double?,
    rsi: Double?,
    modifier: Modifier = Modifier
) {
    val (signalText, signalColor, signalBg, icon) = when (signal) {
        TradeSignal.UP -> Quadruple("📈 BUY / CALL (UP)", BullishGreen, BullishGreenContainer, Icons.Default.ArrowUpward)
        TradeSignal.DOWN -> Quadruple("📉 SELL / PUT (DOWN)", BearishRed, BearishRedContainer, Icons.Default.ArrowDownward)
        TradeSignal.NO_TRADE -> Quadruple("⚪ NO TRADE SIGNAL", TextMuted, NavySurfaceVariant, Icons.Default.Remove)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, signalColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Price: ${String.format("%.${decimals}f", price)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = signalColor
                        )
                    )
                }

                // Live Glowing Signal Badge
                Box(
                    modifier = Modifier
                        .background(signalBg, RoundedCornerShape(20.dp))
                        .border(1.dp, signalColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(signalColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = signalText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricItem("EMA Fast", emaFast?.let { String.format("%.${decimals}f", it) } ?: "--")
                MetricItem("EMA Slow", emaSlow?.let { String.format("%.${decimals}f", it) } ?: "--")
                MetricItem("RSI", rsi?.let { String.format("%.1f", it) } ?: "--")
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String? = null,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = it, fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
