package com.example.movie_quote_api.exception

/**
 * Exception thrown when a requested quote cannot be found
 */
class QuoteNotFoundException(message: String) : RuntimeException(message) {
    init {
        println("DEBUG: QuoteNotFoundException created - Message: $message")
    }
}