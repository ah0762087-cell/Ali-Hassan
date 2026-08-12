package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Candle
import com.example.data.models.TradeSignal
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.FastEmaCyan
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.RsiPurple
import com.example.ui.theme.SlowEmaYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InteractiveCandlestickChart(
    candles: List<Candle>,
    decimals: Int = 5,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(NavySurface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading chart data...", color = TextSecondary)
        }
        return
    }

    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    var visibleCandleCount by remember { mutableStateOf(40) }

    val activeIndex = selectedCandleIndex ?: (candles.size - 1)
    val activeCandle = candles.getOrNull(activeIndex) ?: candles.last()

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Chart Legend & Selected Candle Info Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Time: ${dateFormat.format(Date(activeCandle.timestamp))}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "O: ${String.format(Locale.US, "%.${decimals}f", activeCandle.open)} ",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "H: ${String.format(Locale.US, "%.${decimals}f", activeCandle.high)} ",
                            fontSize = 11.sp,
                            color = BullishGreen,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "L: ${String.format(Locale.US, "%.${decimals}f", activeCandle.low)} ",
                            fontSize = 11.sp,
                            color = BearishRed,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "C: ${String.format(Locale.US, "%.${decimals}f", activeCandle.close)}",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Indicator Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeCandle.emaFast?.let {
                    Text(
                        text = "EMA Fast: ${String.format(Locale.US, "%.${decimals}f", it)}",
                        fontSize = 10.sp,
                        color = FastEmaCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                activeCandle.emaSlow?.let {
                    Text(
                        text = "EMA Slow: ${String.format(Locale.US, "%.${decimals}f", it)}",
                        fontSize = 10.sp,
                        color = SlowEmaYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                activeCandle.rsi?.let {
                    Text(
                        text = "RSI: ${String.format(Locale.US, "%.1f", it)}",
                        fontSize = 10.sp,
                        color = RsiPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MAIN PRICE & INDICATOR CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .pointerInput(candles) {
                        detectTapGestures(
                            onTap = { offset ->
                                val candleWidth = size.width / visibleCandleCount.toFloat()
                                val startIndex = (candles.size - visibleCandleCount).coerceAtLeast(0)
                                val tappedIndex = (startIndex + (offset.x / candleWidth).toInt()).coerceIn(0, candles.size - 1)
                                selectedCandleIndex = tappedIndex
                            }
                        )
                    }
                    .pointerInput(candles) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (zoom != 1f) {
                                val newCount = (visibleCandleCount / zoom).toInt()
                                visibleCandleCount = newCount.coerceIn(15, 100)
                            }
                            if (pan.x != 0f) {
                                val shift = (-pan.x / (size.width / visibleCandleCount)).toInt()
                                if (shift != 0) {
                                    val current = selectedCandleIndex ?: (candles.size - 1)
                                    selectedCandleIndex = (current + shift).coerceIn(0, candles.size - 1)
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val displayCandles = if (candles.size > visibleCandleCount) {
                        candles.takeLast(visibleCandleCount)
                    } else {
                        candles
                    }

                    if (displayCandles.isEmpty()) return@Canvas

                    val minPrice = displayCandles.minOf { it.low }
                    val maxPrice = displayCandles.maxOf { it.high }
                    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.00001)

                    val candleWidth = width / displayCandles.size.toFloat()
                    val bodyWidth = candleWidth * 0.65f

                    // Grid Lines & Price Labels
                    val gridLines = 4
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    for (g in 0..gridLines) {
                        val y = height * (g.toFloat() / gridLines.toFloat())
                        drawLine(
                            color = NavyCardBorder,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = dashedEffect
                        )
                    }

                    // Path for Fast EMA & Slow EMA
                    val fastEmaPath = Path()
                    val slowEmaPath = Path()
                    var fastStarted = false
                    var slowStarted = false

                    displayCandles.forEachIndexed { i, candle ->
                        val xCenter = (i * candleWidth) + (candleWidth / 2f)

                        // Y position calculation helper
                        fun priceToY(price: Double): Float {
                            return (height - ((price - minPrice) / priceRange * height)).toFloat()
                        }

                        val openY = priceToY(candle.open)
                        val closeY = priceToY(candle.close)
                        val highY = priceToY(candle.high)
                        val lowY = priceToY(candle.low)

                        val isBullish = candle.close >= candle.open
                        val candleColor = if (isBullish) BullishGreen else BearishRed

                        // Draw Wick
                        drawLine(
                            color = candleColor,
                            start = Offset(xCenter, highY),
                            end = Offset(xCenter, lowY),
                            strokeWidth = 2f
                        )

                        // Draw Candle Body
                        val topY = minOf(openY, closeY)
                        val bodyHeight = maxOf(Math.abs(openY - closeY), 2f)
                        drawRect(
                            color = candleColor,
                            topLeft = Offset(xCenter - (bodyWidth / 2f), topY),
                            size = Size(bodyWidth, bodyHeight)
                        )

                        // EMA Fast Line
                        candle.emaFast?.let { emaF ->
                            val emaY = priceToY(emaF)
                            if (!fastStarted) {
                                fastEmaPath.moveTo(xCenter, emaY)
                                fastStarted = true
                            } else {
                                fastEmaPath.lineTo(xCenter, emaY)
                            }
                        }

                        // EMA Slow Line
                        candle.emaSlow?.let { emaS ->
                            val emaY = priceToY(emaS)
                            if (!slowStarted) {
                                slowEmaPath.moveTo(xCenter, emaY)
                                slowStarted = true
                            } else {
                                slowEmaPath.lineTo(xCenter, emaY)
                            }
                        }

                        // Signal Arrow Marker
                        when (candle.signal) {
                            TradeSignal.UP -> {
                                val arrowY = lowY + 12f
                                val path = Path().apply {
                                    moveTo(xCenter, arrowY - 8f)
                                    lineTo(xCenter - 6f, arrowY + 4f)
                                    lineTo(xCenter + 6f, arrowY + 4f)
                                    close()
                                }
                                drawPath(path, BullishGreen)
                            }
                            TradeSignal.DOWN -> {
                                val arrowY = highY - 12f
                                val path = Path().apply {
                                    moveTo(xCenter, arrowY + 8f)
                                    lineTo(xCenter - 6f, arrowY - 4f)
                                    lineTo(xCenter + 6f, arrowY - 4f)
                                    close()
                                }
                                drawPath(path, BearishRed)
                            }
                            TradeSignal.NO_TRADE -> {}
                        }
                    }

                    // Draw EMA Overlay Lines
                    if (fastStarted) {
                        drawPath(fastEmaPath, FastEmaCyan, style = Stroke(width = 3f))
                    }
                    if (slowStarted) {
                        drawPath(slowEmaPath, SlowEmaYellow, style = Stroke(width = 3f))
                    }

                    // Highlight Active Selected Candle Line
                    selectedCandleIndex?.let { selIdx ->
                        val displayIdx = selIdx - (candles.size - displayCandles.size)
                        if (displayIdx in displayCandles.indices) {
                            val activeX = (displayIdx * candleWidth) + (candleWidth / 2f)
                            drawLine(
                                color = TextPrimary.copy(alpha = 0.5f),
                                start = Offset(activeX, 0f),
                                end = Offset(activeX, height),
                                strokeWidth = 2f,
                                pathEffect = dashedEffect
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // RSI SUBCHART
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val displayCandles = if (candles.size > visibleCandleCount) {
                        candles.takeLast(visibleCandleCount)
                    } else {
                        candles
                    }

                    if (displayCandles.isEmpty()) return@Canvas

                    val candleWidth = width / displayCandles.size.toFloat()

                    // RSI Threshold lines 70, 50, 30
                    fun rsiToY(rsiVal: Double): Float {
                        return (height - (rsiVal / 100.0 * height)).toFloat()
                    }

                    val y70 = rsiToY(70.0)
                    val y50 = rsiToY(50.0)
                    val y30 = rsiToY(30.0)

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    drawLine(TextMuted, Offset(0f, y70), Offset(width, y70), 1f, pathEffect = dashEffect)
                    drawLine(TextMuted.copy(alpha = 0.4f), Offset(0f, y50), Offset(width, y50), 1f, pathEffect = dashEffect)
                    drawLine(TextMuted, Offset(0f, y30), Offset(width, y30), 1f, pathEffect = dashEffect)

                    val rsiPath = Path()
                    var rsiStarted = false

                    displayCandles.forEachIndexed { i, candle ->
                        val xCenter = (i * candleWidth) + (candleWidth / 2f)
                        candle.rsi?.let { rsiVal ->
                            val y = rsiToY(rsiVal)
                            if (!rsiStarted) {
                                rsiPath.moveTo(xCenter, y)
                                rsiStarted = true
                            } else {
                                rsiPath.lineTo(xCenter, y)
                            }
                        }
                    }

                    if (rsiStarted) {
                        drawPath(rsiPath, RsiPurple, style = Stroke(width = 2.5f))
                    }
                }
            }
        }
    }
}
