package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarketDataRepository
import com.example.data.TradingRepository
import com.example.data.local.AppDatabase
import com.example.data.models.BacktestResult
import com.example.data.models.Candle
import com.example.data.models.MarketSymbol
import com.example.data.models.PaperTrade
import com.example.data.models.PaperTradeStatus
import com.example.data.models.StrategyConfig
import com.example.data.models.TradeSignal
import com.example.util.IndicatorCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val marketRepo = MarketDataRepository()
    val repository = TradingRepository(db, marketRepo)

    val supportedSymbols = repository.supportedSymbols

    // UI States
    private val _selectedSymbol = MutableStateFlow(supportedSymbols.first())
    val selectedSymbol: StateFlow<MarketSymbol> = _selectedSymbol.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("5m")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("5d")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _activeStrategy = MutableStateFlow(StrategyConfig())
    val activeStrategy: StateFlow<StrategyConfig> = _activeStrategy.asStateFlow()

    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    private val _currentPrice = MutableStateFlow(1.0850)
    val currentPrice: StateFlow<Double> = _currentPrice.asStateFlow()

    private val _currentSignal = MutableStateFlow(TradeSignal.NO_TRADE)
    val currentSignal: StateFlow<TradeSignal> = _currentSignal.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _backtestResult = MutableStateFlow<BacktestResult?>(null)
    val backtestResult: StateFlow<BacktestResult?> = _backtestResult.asStateFlow()

    // Database flows
    val savedStrategies = repository.savedStrategies
    val backtestHistory = repository.backtestHistory
    val paperTrades = repository.paperTrades
    val portfolio = repository.portfolio

    private var liveTickerJob: Job? = null
    private var tradeResolutionTimerJob: Job? = null

    init {
        loadData()
        startLiveTicker()
        startPaperTradeMonitoring()
    }

    fun selectSymbol(symbol: MarketSymbol) {
        _selectedSymbol.value = symbol
        _currentPrice.value = when {
            symbol.symbol.startsWith("EURUSD") -> 1.0850
            symbol.symbol.startsWith("GBPUSD") -> 1.2720
            symbol.symbol.startsWith("USDJPY") -> 152.40
            symbol.symbol.startsWith("BTC") -> 68500.0
            symbol.symbol.startsWith("ETH") -> 3450.0
            symbol.symbol.startsWith("AAPL") -> 225.0
            symbol.symbol.startsWith("TSLA") -> 210.0
            else -> 540.0
        }
        loadData()
        startLiveTicker()
    }

    fun selectTimeframe(tf: String) {
        _selectedTimeframe.value = tf
        loadData()
    }

    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
        loadData()
    }

    fun updateStrategy(config: StrategyConfig) {
        _activeStrategy.value = config
        recalculateCurrentCandles()
    }

    fun saveCurrentStrategy(name: String) {
        viewModelScope.launch {
            val configToSave = _activeStrategy.value.copy(name = name)
            repository.saveStrategy(configToSave)
        }
    }

    fun deleteStrategy(id: Long) {
        viewModelScope.launch {
            repository.deleteStrategy(id)
        }
    }

    fun refreshData() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val fetched = repository.loadCandlesWithIndicators(
                symbol = _selectedSymbol.value.symbol,
                timeframe = _selectedTimeframe.value,
                period = _selectedPeriod.value,
                strategy = _activeStrategy.value
            )
            _candles.value = fetched
            if (fetched.isNotEmpty()) {
                val latest = fetched.last()
                _currentPrice.value = latest.close
                _currentSignal.value = latest.signal
            }
            _isRefreshing.value = false

            // Auto run backtest for active selection
            runBacktest()
        }
    }

    private fun recalculateCurrentCandles() {
        val currentList = _candles.value
        if (currentList.isNotEmpty()) {
            val recalculated = IndicatorCalculator.calculateIndicators(currentList, _activeStrategy.value)
            _candles.value = recalculated
            if (recalculated.isNotEmpty()) {
                _currentSignal.value = recalculated.last().signal
            }
        }
    }

    fun runBacktest() {
        viewModelScope.launch {
            val result = repository.runBacktestAndSave(
                symbol = _selectedSymbol.value.symbol,
                timeframe = _selectedTimeframe.value,
                period = _selectedPeriod.value,
                strategy = _activeStrategy.value
            )
            _backtestResult.value = result
        }
    }

    private fun startLiveTicker() {
        liveTickerJob?.cancel()
        liveTickerJob = viewModelScope.launch {
            repository.observeLiveTicker(_selectedSymbol.value.symbol, intervalSeconds = 3).collectLatest { tickPrice ->
                _currentPrice.value = tickPrice

                // Append/Update live candle
                val currentList = _candles.value.toMutableList()
                if (currentList.isNotEmpty()) {
                    val last = currentList.last()
                    val now = System.currentTimeMillis()

                    val updatedLast = last.copy(
                        high = maxOf(last.high, tickPrice),
                        low = minOf(last.low, tickPrice),
                        close = tickPrice
                    )
                    currentList[currentList.size - 1] = updatedLast

                    val recalculated = IndicatorCalculator.calculateIndicators(currentList, _activeStrategy.value)
                    _candles.value = recalculated
                    if (recalculated.isNotEmpty()) {
                        _currentSignal.value = recalculated.last().signal
                    }
                }
            }
        }
    }

    fun placePaperTrade(direction: TradeSignal, stake: Double, durationSeconds: Int = 60) {
        viewModelScope.launch {
            repository.placePaperTrade(
                symbol = _selectedSymbol.value.symbol,
                direction = direction,
                entryPrice = _currentPrice.value,
                stake = stake,
                durationSeconds = durationSeconds
            )
        }
    }

    private fun startPaperTradeMonitoring() {
        tradeResolutionTimerJob?.cancel()
        tradeResolutionTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                val currentTrades = repository.paperTrades
                viewModelScope.launch {
                    currentTrades.collectLatest { tradeList ->
                        tradeList.filter { it.status == PaperTradeStatus.OPEN }.forEach { openTrade ->
                            if (now >= openTrade.expiryTimeMs) {
                                repository.resolvePaperTrade(openTrade, _currentPrice.value)
                            }
                        }
                    }
                }
            }
        }
    }

    fun resetPaperTrading() {
        viewModelScope.launch {
            repository.resetPortfolioBalance()
        }
    }
}
