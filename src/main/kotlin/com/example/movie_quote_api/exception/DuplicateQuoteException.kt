package com.example.movie_quote_api.exception

/**
 * Exception thrown when attempting to create a duplicate quote
 */
class DuplicateQuoteException(message: String) : RuntimeException(message) {
    init {
        println("DEBUG: DuplicateQuoteException created - Message: $message")
    }
}