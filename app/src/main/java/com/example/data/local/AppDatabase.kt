package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StrategyEntity::class,
        BacktestRecordEntity::class,
        PaperTradeEntity::class,
        PortfolioEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun strategyDao(): StrategyDao
    abstract fun backtestDao(): BacktestDao
    abstract fun paperTradeDao(): PaperTradeDao
    abstract fun portfolioDao(): PortfolioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "binary_trend_bot_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
