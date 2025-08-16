package com.example.movie_quote_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MovieQuoteApiApplication

fun main(args: Array<String>) {
    println("DEBUG: Starting Movie Quote API application...")
	runApplication<MovieQuoteApiApplication>(*args)
    println("DEBUG: Movie Quote API application started successfully")
}
