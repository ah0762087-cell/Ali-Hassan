package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.BacktestRecordEntity
import com.example.data.local.PaperTradeEntity
import com.example.data.local.PortfolioEntity
import com.example.data.local.StrategyEntity
import com.example.data.models.BacktestResult
import com.example.data.models.Candle
import com.example.data.models.PaperTrade
import com.example.data.models.PaperTradeStatus
import com.example.data.models.StrategyConfig
import com.example.data.models.TradeSignal
import com.example.util.IndicatorCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class TradingRepository(
    private val db: AppDatabase,
    private val marketRepo: MarketDataRepository
) {
    val supportedSymbols = marketRepo.supportedSymbols

    fun observeLiveTicker(symbol: String, intervalSeconds: Long = 3) = marketRepo.observeLiveTicker(symbol, intervalSeconds)

    // Flow of saved strategies
    val savedStrategies: Flow<List<StrategyConfig>> = db.strategyDao().getAllStrategies().map { list ->
        if (list.isEmpty()) {
            listOf(StrategyConfig())
        } else {
            list.map { entity ->
                StrategyConfig(
                    id = entity.id,
                    name = entity.name,
                    emaFast = entity.emaFast,
                    emaSlow = entity.emaSlow,
                    rsiPeriod = entity.rsiPeriod,
                    rsiUpThreshold = entity.rsiUpThreshold,
                    rsiDownThreshold = entity.rsiDownThreshold,
                    expiryCandles = entity.expiryCandles
                )
            }
        }
    }

    // Flow of backtest history
    val backtestHistory: Flow<List<BacktestRecordEntity>> = db.backtestDao().getAllBacktests()

    // Flow of paper trades
    val paperTrades: Flow<List<PaperTrade>> = db.paperTradeDao().getAllPaperTrades().map { list ->
        list.map { e ->
            PaperTrade(
                id = e.id,
                symbol = e.symbol,
                direction = TradeSignal.valueOf(e.direction),
                entryPrice = e.entryPrice,
                exitPrice = e.exitPrice,
                stake = e.stake,
                expiryTimeMs = e.expiryTimeMs,
                startTimeMs = e.startTimeMs,
                status = PaperTradeStatus.valueOf(e.status),
                pnl = e.pnl
            )
        }
    }

    // Flow of portfolio balance
    val portfolio: Flow<PortfolioEntity> = db.portfolioDao().getPortfolio().map {
        it ?: PortfolioEntity()
    }

    suspend fun loadCandlesWithIndicators(
        symbol: String,
        timeframe: String,
        period: String,
        strategy: StrategyConfig
    ): List<Candle> {
        val rawCandles = marketRepo.getCandles(symbol, timeframe, period)
        return IndicatorCalculator.calculateIndicators(rawCandles, strategy)
    }

    suspend fun runBacktestAndSave(
        symbol: String,
        timeframe: String,
        period: String,
        strategy: StrategyConfig
    ): BacktestResult {
        val rawCandles = marketRepo.getCandles(symbol, timeframe, period)
        val result = IndicatorCalculator.runBacktest(rawCandles, strategy, symbol, timeframe, period)

        db.backtestDao().insertBacktest(
            BacktestRecordEntity(
                symbol = symbol,
                timeframe = timeframe,
                period = period,
                totalTrades = result.totalTrades,
                wins = result.wins,
                losses = result.losses,
                winRate = result.winRate,
                strategyName = strategy.name
            )
        )

        return result
    }

    suspend fun saveStrategy(strategy: StrategyConfig) {
        db.strategyDao().insertStrategy(
            StrategyEntity(
                id = if (strategy.id > 1) strategy.id else 0,
                name = strategy.name,
                emaFast = strategy.emaFast,
                emaSlow = strategy.emaSlow,
                rsiPeriod = strategy.rsiPeriod,
                rsiUpThreshold = strategy.rsiUpThreshold,
                rsiDownThreshold = strategy.rsiDownThreshold,
                expiryCandles = strategy.expiryCandles
            )
        )
    }

    suspend fun deleteStrategy(id: Long) {
        if (id > 1) {
            db.strategyDao().deleteStrategy(id)
        }
    }

    suspend fun placePaperTrade(
        symbol: String,
        direction: TradeSignal,
        entryPrice: Double,
        stake: Double,
        durationSeconds: Int = 60
    ): PaperTrade {
        val now = System.currentTimeMillis()
        val expiry = now + (durationSeconds * 1000L)
        val tradeId = "TRADE_${now}_${(1000..9999).random()}"

        val entity = PaperTradeEntity(
            id = tradeId,
            symbol = symbol,
            direction = direction.name,
            entryPrice = entryPrice,
            exitPrice = null,
            stake = stake,
            startTimeMs = now,
            expiryTimeMs = expiry,
            status = PaperTradeStatus.OPEN.name,
            pnl = 0.0
        )

        db.paperTradeDao().insertTrade(entity)

        return PaperTrade(
            id = tradeId,
            symbol = symbol,
            direction = direction,
            entryPrice = entryPrice,
            exitPrice = null,
            stake = stake,
            expiryTimeMs = expiry,
            startTimeMs = now,
            status = PaperTradeStatus.OPEN,
            pnl = 0.0
        )
    }

    suspend fun resolvePaperTrade(trade: PaperTrade, currentPrice: Double) {
        val isWin = when (trade.direction) {
            TradeSignal.UP -> currentPrice > trade.entryPrice
            TradeSignal.DOWN -> currentPrice < trade.entryPrice
            TradeSignal.NO_TRADE -> false
        }

        val payoutMultiplier = 0.85 // 85% payout on winning binary option
        val pnl = if (isWin) trade.stake * payoutMultiplier else -trade.stake
        val newStatus = if (isWin) PaperTradeStatus.WIN else PaperTradeStatus.LOSS

        db.paperTradeDao().updateTrade(
            PaperTradeEntity(
                id = trade.id,
                symbol = trade.symbol,
                direction = trade.direction.name,
                entryPrice = trade.entryPrice,
                exitPrice = currentPrice,
                stake = trade.stake,
                startTimeMs = trade.startTimeMs,
                expiryTimeMs = trade.expiryTimeMs,
                status = newStatus.name,
                pnl = pnl
            )
        )

        // Update portfolio balance
        val currentPortfolio = db.portfolioDao().getPortfolio()
            .map { it ?: PortfolioEntity() }
            .firstOrNull() ?: PortfolioEntity()

        val updatedBalance = (currentPortfolio.balance + pnl).coerceAtLeast(0.0)
        val updatedProfitLoss = currentPortfolio.totalProfitLoss + pnl
        val updatedWins = currentPortfolio.winCount + (if (isWin) 1 else 0)
        val updatedLosses = currentPortfolio.lossCount + (if (!isWin) 1 else 0)

        db.portfolioDao().setPortfolio(
            PortfolioEntity(
                id = 1,
                balance = updatedBalance,
                totalProfitLoss = updatedProfitLoss,
                winCount = updatedWins,
                lossCount = updatedLosses
            )
        )
    }

    suspend fun resetPortfolioBalance() {
        db.portfolioDao().setPortfolio(PortfolioEntity(id = 1, balance = 10000.0, totalProfitLoss = 0.0, winCount = 0, lossCount = 0))
        db.paperTradeDao().clearTrades()
    }
}
