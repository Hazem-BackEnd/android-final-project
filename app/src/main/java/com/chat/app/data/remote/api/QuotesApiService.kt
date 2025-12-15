package com.chat.app.data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Data class representing a quote from ZenQuotes API
 * Response format: [{"q":"quote text","a":"author","h":"html formatted"}]
 */
data class Quote(
    val q: String,  // Quote text
    val a: String,  // Author
    val h: String = ""  // HTML formatted (optional)
)

/**
 * Retrofit API interface for ZenQuotes
 */
interface QuotesApi {
    @GET("random")
    suspend fun getRandomQuote(): List<Quote>
}

/**
 * Service for fetching random quotes from ZenQuotes API
 * API: https://zenquotes.io/api/random
 * Uses Retrofit for HTTP requests
 */
class QuotesApiService {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(QuotesApi::class.java)
    
    /**
     * Fetch a random quote from ZenQuotes API
     * @return Quote object containing the quote text and author
     */
    suspend fun getRandomQuote(): Result<Quote> {
        return try {
            val response = api.getRandomQuote()
            if (response.isNotEmpty()) {
                Result.success(response.first())
            } else {
                Result.failure(Exception("No quote received"))
            }
        } catch (e: Exception) {
            println("❌ Error fetching quote: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Close resources (not needed for Retrofit, kept for interface compatibility)
     */
    fun close() {
        // Retrofit handles connection pooling automatically
    }
}
