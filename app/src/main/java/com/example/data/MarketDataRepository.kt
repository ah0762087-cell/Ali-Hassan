package com.example.data

import com.example.data.models.Candle
import com.example.data.models.MarketSymbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MarketDataRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val supportedSymbols = listOf(
        MarketSymbol("EURUSD=X", "EUR / USD", "Forex", 5),
        MarketSymbol("GBPUSD=X", "GBP / USD", "Forex", 5),
        MarketSymbol("USDJPY=X", "USD / JPY", "Forex", 3),
        MarketSymbol("BTC-USD", "Bitcoin", "Crypto", 2),
        MarketSymbol("ETH-USD", "Ethereum", "Crypto", 2),
        MarketSymbol("AAPL", "Apple Inc.", "Stock", 2),
        MarketSymbol("TSLA", "Tesla Inc.", "Stock", 2),
        MarketSymbol("SPY", "S&P 500 ETF", "Index", 2)
    )

    suspend fun getCandles(
        symbol: String,
        timeframe: String = "5m",
        period: String = "5d"
    ): List<Candle> = withContext(Dispatchers.IO) {
        val networkCandles = fetchFromYahooFinance(symbol, timeframe, period)
        if (networkCandles.isNotEmpty()) {
            networkCandles
        } else {
            generateSyntheticCandles(symbol, timeframe, period)
        }
    }

    private fun fetchFromYahooFinance(
        symbol: String,
        timeframe: String,
        period: String
    ): List<Candle> {
        return try {
            val rangeParam = when (period) {
                "1d" -> "1d"
                "5d" -> "5d"
                "1mo" -> "1mo"
                else -> "5d"
            }
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$symbol?range=$rangeParam&interval=$timeframe"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val bodyStr = response.body?.string() ?: return emptyList()
            val json = JSONObject(bodyStr)
            val chart = json.getJSONObject("chart")
            val resultArr = chart.getJSONArray("result")
            if (resultArr.length() == 0) return emptyList()

            val resultObj = resultArr.getJSONObject(0)
            val timestamps = resultObj.getJSONArray("timestamp")
            val indicators = resultObj.getJSONObject("indicators")
            val quoteArr = indicators.getJSONArray("quote")
            if (quoteArr.length() == 0) return emptyList()

            val quoteObj = quoteArr.getJSONObject(0)
            val opens = quoteObj.getJSONArray("open")
            val highs = quoteObj.getJSONArray("high")
            val lows = quoteObj.getJSONArray("low")
            val closes = quoteObj.getJSONArray("close")
            val volumes = if (quoteObj.has("volume")) quoteObj.getJSONArray("volume") else null

            val list = mutableListOf<Candle>()
            for (i in 0 until timestamps.length()) {
                if (opens.isNull(i) || closes.isNull(i) || highs.isNull(i) || lows.isNull(i)) continue
                val ts = timestamps.getLong(i) * 1000L
                val o = opens.getDouble(i)
                val h = highs.getDouble(i)
                val l = lows.getDouble(i)
                val c = closes.getDouble(i)
                val v = if (volumes != null && !volumes.isNull(i)) volumes.getLong(i) else 1000L

                list.add(Candle(timestamp = ts, open = o, high = h, low = l, close = c, volume = v))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun generateSyntheticCandles(
        symbol: String,
        timeframe: String,
        period: String
    ): List<Candle> {
        val (basePrice, volatility) = when {
            symbol.startsWith("EURUSD") -> 1.0850 to 0.0008
            symbol.startsWith("GBPUSD") -> 1.2720 to 0.0010
            symbol.startsWith("USDJPY") -> 152.40 to 0.15
            symbol.startsWith("BTC") -> 68500.0 to 450.0
            symbol.startsWith("ETH") -> 3450.0 to 35.0
            symbol.startsWith("AAPL") -> 225.0 to 1.5
            symbol.startsWith("TSLA") -> 210.0 to 2.5
            else -> 540.0 to 2.0
        }

        val totalCandles = when (period) {
            "1d" -> 72
            "5d" -> 200
            "1mo" -> 350
            else -> 150
        }

        val intervalMs = when (timeframe) {
            "1m" -> 60_000L
            "5m" -> 300_000L
            "15m" -> 900_000L
            "1h" -> 3_600_000L
            else -> 300_000L
        }

        val now = System.currentTimeMillis()
        val startTs = now - (totalCandles * intervalMs)

        val candles = mutableListOf<Candle>()
        var currentPrice = basePrice

        for (i in 0 until totalCandles) {
            val ts = startTs + (i * intervalMs)
            val change = (Random.nextDouble(-1.0, 1.0) * volatility) + (Random.nextDouble(-0.15, 0.2) * volatility * 0.5)
            val open = currentPrice
            val close = (open + change).coerceAtLeast(0.0001)
            val high = maxOf(open, close) + (Random.nextDouble(0.0, 0.8) * volatility)
            val low = minOf(open, close) - (Random.nextDouble(0.0, 0.8) * volatility)
            val volume = (Random.nextInt(500, 15000)).toLong()

            candles.add(
                Candle(
                    timestamp = ts,
                    open = open,
                    high = high,
                    low = low.coerceAtLeast(0.00001),
                    close = close,
                    volume = volume
                )
            )
            currentPrice = close
        }

        return candles
    }

    fun observeLiveTicker(symbol: String, intervalSeconds: Long = 3): Flow<Double> = flow {
        var base = when {
            symbol.startsWith("EURUSD") -> 1.0850
            symbol.startsWith("GBPUSD") -> 1.2720
            symbol.startsWith("USDJPY") -> 152.40
            symbol.startsWith("BTC") -> 68500.0
            symbol.startsWith("ETH") -> 3450.0
            symbol.startsWith("AAPL") -> 225.0
            symbol.startsWith("TSLA") -> 210.0
            else -> 540.0
        }

        val vol = base * 0.0004

        while (true) {
            val delta = (Random.nextDouble(-1.0, 1.0) * vol)
            base = (base + delta).coerceAtLeast(0.0001)
            emit(base)
            delay(intervalSeconds * 1000)
        }
    }.flowOn(Dispatchers.IO)
}
