package com.example.movie_quote_api.dto

import java.time.LocalDateTime

data class QuoteResponse(
    val id: String,
    val text: String,
    val character: String,
    val movieTitle: String,
    val releaseYear: Int,
    val createdAt: LocalDateTime
) {
    init {
        println("DEBUG: QuoteResponse created for ID: $id")
    }
}