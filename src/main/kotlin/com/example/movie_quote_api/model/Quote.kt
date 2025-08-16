package com.example.movie_quote_api.model

import java.time.LocalDateTime

data class Quote(
    val id: String? = null,
    val text: String,
    val character: String,
    val movieTitle: String,
    val releaseYear: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)