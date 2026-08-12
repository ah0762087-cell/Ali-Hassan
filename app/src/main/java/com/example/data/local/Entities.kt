package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strategies")
data class StrategyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emaFast: Int,
    val emaSlow: Int,
    val rsiPeriod: Int,
    val rsiUpThreshold: Double,
    val rsiDownThreshold: Double,
    val expiryCandles: Int,
    val isDefault: Boolean = false
)

@Entity(tableName = "backtest_history")
data class BacktestRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val timeframe: String,
    val period: String,
    val totalTrades: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double,
    val strategyName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "paper_trades")
data class PaperTradeEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val direction: String, // "UP" or "DOWN"
    val entryPrice: Double,
    val exitPrice: Double?,
    val stake: Double,
    val startTimeMs: Long,
    val expiryTimeMs: Long,
    val status: String, // "OPEN", "WIN", "LOSS"
    val pnl: Double
)

@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 10000.0,
    val totalProfitLoss: Double = 0.0,
    val winCount: Int = 0,
    val lossCount: Int = 0
)
