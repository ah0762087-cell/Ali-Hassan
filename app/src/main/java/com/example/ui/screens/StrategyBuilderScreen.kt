package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.data.models.StrategyConfig
import com.example.ui.theme.FastEmaCyan
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.RsiPurple
import com.example.ui.theme.SlowEmaYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodels.TradingViewModel

@Composable
fun StrategyBuilderScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val activeStrategy by viewModel.activeStrategy.collectAsStateWithLifecycle(initialValue = StrategyConfig())
    val savedStrategies by viewModel.savedStrategies.collectAsStateWithLifecycle(initialValue = listOf(StrategyConfig()))

    var emaFastVal by remember(activeStrategy) { mutableStateOf(activeStrategy.emaFast.toFloat()) }
    var emaSlowVal by remember(activeStrategy) { mutableStateOf(activeStrategy.emaSlow.toFloat()) }
    var rsiPeriodVal by remember(activeStrategy) { mutableStateOf(activeStrategy.rsiPeriod.toFloat()) }
    var expiryCandlesVal by remember(activeStrategy) { mutableStateOf(activeStrategy.expiryCandles.toFloat()) }

    var newPresetName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "STRATEGY PARAMETERS & BUILDER",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Customize indicators, crossover logic and candle expiry",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // PARAMETER SLIDERS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACTIVE PARAMETERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // EMA FAST
                    ParameterSliderItem(
                        label = "EMA Fast Period",
                        valueStr = "${emaFastVal.toInt()}",
                        value = emaFastVal,
                        range = 2f..50f,
                        color = FastEmaCyan,
                        onValueChange = {
                            emaFastVal = it
                            if (emaFastVal < emaSlowVal) {
                                viewModel.updateStrategy(activeStrategy.copy(emaFast = emaFastVal.toInt()))
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // EMA SLOW
                    ParameterSliderItem(
                        label = "EMA Slow Period",
                        valueStr = "${emaSlowVal.toInt()}",
                        value = emaSlowVal,
                        range = 10f..200f,
                        color = SlowEmaYellow,
                        onValueChange = {
                            emaSlowVal = it
                            if (emaSlowVal > emaFastVal) {
                                viewModel.updateStrategy(activeStrategy.copy(emaSlow = emaSlowVal.toInt()))
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // RSI PERIOD
                    ParameterSliderItem(
                        label = "RSI Period",
                        valueStr = "${rsiPeriodVal.toInt()}",
                        value = rsiPeriodVal,
                        range = 5f..50f,
                        color = RsiPurple,
                        onValueChange = {
                            rsiPeriodVal = it
                            viewModel.updateStrategy(activeStrategy.copy(rsiPeriod = rsiPeriodVal.toInt()))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // EXPIRY CANDLES
                    ParameterSliderItem(
                        label = "Expiry Candles (1 = next candle)",
                        valueStr = "${expiryCandlesVal.toInt()} candle(s)",
                        value = expiryCandlesVal,
                        range = 1f..10f,
                        color = MaterialTheme.colorScheme.primary,
                        onValueChange = {
                            expiryCandlesVal = it
                            viewModel.updateStrategy(activeStrategy.copy(expiryCandles = expiryCandlesVal.toInt()))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateStrategy(
                                StrategyConfig(
                                    emaFast = 9,
                                    emaSlow = 21,
                                    rsiPeriod = 14,
                                    expiryCandles = 1
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Default (EMA 9/21, RSI 14)", color = TextPrimary)
                    }
                }
            }
        }

        // SAVE CUSTOM STRATEGY PRESET
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SAVE STRATEGY PRESET (ROOM DATABASE)",
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
                            value = newPresetName,
                            onValueChange = { newPresetName = it },
                            label = { Text("Preset Name") },
                            placeholder = { Text("e.g. Scalp EMA 5/13") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = NavySurfaceVariant,
                                unfocusedContainerColor = NavySurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                if (newPresetName.isNotBlank()) {
                                    viewModel.saveCurrentStrategy(newPresetName)
                                    newPresetName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SAVED PRESETS LIST
        item {
            Text(
                text = "SAVED PRESETS (${savedStrategies.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        items(savedStrategies) { config ->
            val isActive = activeStrategy.id == config.id || (config.emaFast == activeStrategy.emaFast && config.emaSlow == activeStrategy.emaSlow && config.rsiPeriod == activeStrategy.rsiPeriod)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isActive) MaterialTheme.colorScheme.primary else NavyCardBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = config.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "EMA Fast: ${config.emaFast} | EMA Slow: ${config.emaSlow} | RSI: ${config.rsiPeriod} | Expiry: ${config.expiryCandles} candle(s)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isActive) {
                            IconButton(onClick = { viewModel.updateStrategy(config) }) {
                                Icon(Icons.Default.Check, contentDescription = "Apply Preset", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (config.id > 1) {
                            IconButton(onClick = { viewModel.deleteStrategy(config.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Preset", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParameterSliderItem(
    label: String,
    valueStr: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
            Text(text = valueStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = NavySurfaceVariant
            )
        )
    }
}
