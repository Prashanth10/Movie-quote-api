package com.example.movie_quote_api.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class QuoteRequest (
    @field:NotBlank(message = "Quote text is required")
    @field:Size(max = 500, message = "Quote text cannot exceed 500 characters")
    val text: String,

    @field:NotBlank(message = "Character name is required")
    @field:Size(max = 100, message = "Character name cannot exceed 100 characters")
    val character: String,

    @field:NotBlank(message = "Movie title is required")
    @field:Size(max = 200, message = "Movie title cannot exceed 200 characters")
    val movieTitle: String,

    @field:Min(value = 1900, message = "Release year must be after 1900")
    @field:Max(value = 2030, message = "Release year cannot be in far future")
    val releaseYear: Int
) {
    init {
        println("DEBUG: QuoteRequest created - movie: $movieTitle, year: $releaseYear")
    }
}