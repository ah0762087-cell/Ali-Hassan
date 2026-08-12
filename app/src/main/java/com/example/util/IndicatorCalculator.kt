package com.example.util

import com.example.data.models.BacktestResult
import com.example.data.models.BacktestTradeLog
import com.example.data.models.Candle
import com.example.data.models.StrategyConfig
import com.example.data.models.TradeSignal
import kotlin.math.max

object IndicatorCalculator {

    fun calculateIndicators(
        candles: List<Candle>,
        config: StrategyConfig
    ): List<Candle> {
        if (candles.isEmpty()) return emptyList()

        val fastSpan = config.emaFast
        val slowSpan = config.emaSlow
        val rsiPeriod = config.rsiPeriod

        val fastAlpha = 2.0 / (fastSpan + 1.0)
        val slowAlpha = 2.0 / (slowSpan + 1.0)
        val rsiAlpha = 1.0 / rsiPeriod.toDouble()

        val emaFastList = MutableList<Double?>(candles.size) { null }
        val emaSlowList = MutableList<Double?>(candles.size) { null }
        val rsiList = MutableList<Double?>(candles.size) { null }

        // 1. EMA Fast
        var currentEmaFast = candles[0].close
        emaFastList[0] = currentEmaFast
        for (i in 1 until candles.size) {
            currentEmaFast = (candles[i].close * fastAlpha) + (currentEmaFast * (1.0 - fastAlpha))
            emaFastList[i] = currentEmaFast
        }

        // 2. EMA Slow
        var currentEmaSlow = candles[0].close
        emaSlowList[0] = currentEmaSlow
        for (i in 1 until candles.size) {
            currentEmaSlow = (candles[i].close * slowAlpha) + (currentEmaSlow * (1.0 - slowAlpha))
            emaSlowList[i] = currentEmaSlow
        }

        // 3. RSI
        var avgGain = 0.0
        var avgLoss = 0.0

        for (i in 1 until candles.size) {
            val delta = candles[i].close - candles[i - 1].close
            val gain = if (delta > 0) delta else 0.0
            val loss = if (delta < 0) -delta else 0.0

            if (i == 1) {
                avgGain = gain
                avgLoss = loss
            } else {
                avgGain = (gain * rsiAlpha) + (avgGain * (1.0 - rsiAlpha))
                avgLoss = (loss * rsiAlpha) + (avgLoss * (1.0 - rsiAlpha))
            }

            val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
            val rsi = 100.0 - (100.0 / (1.0 + rs))
            rsiList[i] = rsi.coerceIn(0.0, 100.0)
        }

        // Combine into enriched Candles with Signal
        return candles.mapIndexed { index, candle ->
            val fastVal = emaFastList[index]
            val slowVal = emaSlowList[index]
            val rsiVal = rsiList[index]

            val signal = if (fastVal != null && slowVal != null && rsiVal != null) {
                evaluateSignal(candle.close, fastVal, slowVal, rsiVal, config)
            } else {
                TradeSignal.NO_TRADE
            }

            candle.copy(
                emaFast = fastVal,
                emaSlow = slowVal,
                rsi = rsiVal,
                signal = signal
            )
        }
    }

    fun evaluateSignal(
        price: Double,
        emaFast: Double,
        emaSlow: Double,
        rsi: Double,
        config: StrategyConfig
    ): TradeSignal {
        return when {
            price > emaFast && emaFast > emaSlow && rsi > config.rsiUpThreshold -> TradeSignal.UP
            price < emaFast && emaFast < emaSlow && rsi < config.rsiDownThreshold -> TradeSignal.DOWN
            else -> TradeSignal.NO_TRADE
        }
    }

    fun runBacktest(
        candles: List<Candle>,
        config: StrategyConfig,
        symbol: String,
        timeframe: String,
        period: String
    ): BacktestResult {
        val enriched = calculateIndicators(candles, config)
        val expiry = config.expiryCandles.coerceAtLeast(1)

        val startIdx = max(config.emaSlow, config.rsiPeriod)
        var totalTrades = 0
        var wins = 0
        var losses = 0
        var currentStreak = 0
        var maxConsecutiveWins = 0

        val logs = mutableListOf<BacktestTradeLog>()

        for (i in startIdx until (enriched.size - expiry)) {
            val current = enriched[i]
            val signal = current.signal

            if (signal == TradeSignal.NO_TRADE) continue

            val entryPrice = current.close
            val future = enriched[i + expiry]
            val exitPrice = future.close

            val isWin = when (signal) {
                TradeSignal.UP -> exitPrice > entryPrice
                TradeSignal.DOWN -> exitPrice < entryPrice
                TradeSignal.NO_TRADE -> false
            }

            totalTrades++
            if (isWin) {
                wins++
                currentStreak++
                if (currentStreak > maxConsecutiveWins) {
                    maxConsecutiveWins = currentStreak
                }
            } else {
                losses++
                currentStreak = 0
            }

            logs.add(
                BacktestTradeLog(
                    index = totalTrades,
                    timestamp = current.timestamp,
                    signal = signal,
                    entryPrice = entryPrice,
                    exitPrice = exitPrice,
                    isWin = isWin
                )
            )
        }

        val winRate = if (totalTrades > 0) (wins.toDouble() / totalTrades.toDouble()) * 100.0 else 0.0

        return BacktestResult(
            symbol = symbol,
            timeframe = timeframe,
            period = period,
            totalTrades = totalTrades,
            wins = wins,
            losses = losses,
            winRate = winRate,
            consecutiveWins = maxConsecutiveWins,
            maxDrawdown = if (totalTrades > 0) (losses.toDouble() / totalTrades.toDouble()) * 100.0 else 0.0,
            logs = logs
        )
    }
}
