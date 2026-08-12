package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StrategyDao {
    @Query("SELECT * FROM strategies ORDER BY id ASC")
    fun getAllStrategies(): Flow<List<StrategyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: StrategyEntity): Long

    @Query("DELETE FROM strategies WHERE id = :id")
    suspend fun deleteStrategy(id: Long)
}

@Dao
interface BacktestDao {
    @Query("SELECT * FROM backtest_history ORDER BY timestamp DESC")
    fun getAllBacktests(): Flow<List<BacktestRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBacktest(record: BacktestRecordEntity): Long

    @Query("DELETE FROM backtest_history")
    suspend fun clearHistory()
}

@Dao
interface PaperTradeDao {
    @Query("SELECT * FROM paper_trades ORDER BY startTimeMs DESC")
    fun getAllPaperTrades(): Flow<List<PaperTradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: PaperTradeEntity)

    @Update
    suspend fun updateTrade(trade: PaperTradeEntity)

    @Query("DELETE FROM paper_trades")
    suspend fun clearTrades()
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio WHERE id = 1")
    fun getPortfolio(): Flow<PortfolioEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPortfolio(portfolio: PortfolioEntity)
}
