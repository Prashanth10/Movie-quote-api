package com.example.movie_quote_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MovieQuoteApiApplication

fun main(args: Array<String>) {
    println("DEBUG: Starting Movie Quote API application...")
    println("=".repeat(60))
    println("DEBUG: Available endpoints:")
    println("DEBUG: - POST   /api/quotes           (create new quote)")
    println("DEBUG: - GET    /api/quotes           (list all quotes)")
    println("DEBUG: - GET    /api/quotes/{id}      (get specific quote)")
    println("DEBUG: - PUT    /api/quotes/{id}      (update existing quote)")
    println("DEBUG: - DELETE /api/quotes/{id}      (delete quote)")
    println("DEBUG: - GET    /api/quotes/search    (search quotes)")
    println("DEBUG: - GET    /api/quotes/today     (quote of the day)")
    println("DEBUG: - GET    /api/quotes/stats     (system statistics)")
    println("=".repeat(60))
	runApplication<MovieQuoteApiApplication>(*args)
    println("DEBUG: Movie Quote API application started successfully")
    println("DEBUG: Server running on http://localhost:8080")
    println("=".repeat(60))
}
