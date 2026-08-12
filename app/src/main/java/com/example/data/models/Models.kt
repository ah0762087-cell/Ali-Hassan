package com.example.data.models

enum class TradeSignal {
    UP,
    DOWN,
    NO_TRADE
}

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long = 0,
    val emaFast: Double? = null,
    val emaSlow: Double? = null,
    val rsi: Double? = null,
    val signal: TradeSignal = TradeSignal.NO_TRADE
)

data class StrategyConfig(
    val id: Long = 1L,
    val name: String = "Binary Trend Default",
    val emaFast: Int = 9,
    val emaSlow: Int = 21,
    val rsiPeriod: Int = 14,
    val rsiUpThreshold: Double = 50.0,
    val rsiDownThreshold: Double = 50.0,
    val expiryCandles: Int = 1
)

data class BacktestTradeLog(
    val index: Int,
    val timestamp: Long,
    val signal: TradeSignal,
    val entryPrice: Double,
    val exitPrice: Double,
    val isWin: Boolean
)

data class BacktestResult(
    val symbol: String,
    val timeframe: String,
    val period: String,
    val totalTrades: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double,
    val consecutiveWins: Int,
    val maxDrawdown: Double,
    val logs: List<BacktestTradeLog>
)

enum class PaperTradeStatus {
    OPEN,
    WIN,
    LOSS
}

data class PaperTrade(
    val id: String,
    val symbol: String,
    val direction: TradeSignal,
    val entryPrice: Double,
    val exitPrice: Double? = null,
    val stake: Double,
    val expiryTimeMs: Long,
    val startTimeMs: Long,
    val status: PaperTradeStatus,
    val pnl: Double = 0.0
)

data class MarketSymbol(
    val symbol: String,
    val displayName: String,
    val category: String, // Forex, Crypto, Stock
    val decimals: Int = 5
)
