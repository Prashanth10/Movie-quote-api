package com.example.movie_quote_api.model

import java.time.LocalDateTime
import java.util.UUID

data class Quote(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val character: String,
    val movieTitle: String,
    val releaseYear: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)