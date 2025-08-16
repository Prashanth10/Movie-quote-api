package com.example.movie_quote_api.dto

import java.time.LocalDateTime

data class QuoteErrorResponse(
    val message: String,
    val errorCode: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val details: List<String> = emptyList()
) {
    init {
        println("DEBUG: ErrorResponse created - Code: '$errorCode', Message: '$message'")
    }
}