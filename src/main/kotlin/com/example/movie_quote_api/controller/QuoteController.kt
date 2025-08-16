package com.example.movie_quote_api.controller

import com.example.movie_quote_api.dto.QuoteRequest
import com.example.movie_quote_api.model.Quote
import com.example.movie_quote_api.service.QuoteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/quotes")
class QuoteController(private val quoteService: QuoteService) {

    init {
        println("DEBUG: QuoteController initialized with ID-aware endpoints")
    }

    /**
     * Create a new quote (ID will be generated)
     * POST /api/quotes
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createQuote(@Valid @RequestBody request: QuoteRequest): Quote {
        println("DEBUG: POST /api/quotes - Creating new quote for '${request.movieTitle}'")
        println("DEBUG: Character: '${request.character}', Year: ${request.releaseYear}")

        // Create quote without ID - repository will generate it
        val quote = Quote(
            id = null,
            text = request.text.trim(),
            character = request.character.trim(),
            movieTitle = request.movieTitle.trim(),
            releaseYear = request.releaseYear
        )

        val savedQuote = quoteService.addQuote(quote)
        println("DEBUG: Quote created with ID: ${savedQuote.id}")

        return savedQuote
    }

    /**
     * Get all quotes
     * GET /api/quotes
     */
    @GetMapping
    fun getAllQuotes(): List<Quote> {
        println("DEBUG: GET /api/quotes - Retrieving all quotes")

        val quotes = quoteService.getAllQuotes()
        println("DEBUG: Returning ${quotes.size} quotes")

        return quotes
    }

    /**
     * Get a specific quote by ID
     * GET /api/quotes/{id}
     */
    @GetMapping("/{id}")
    fun getQuote(@PathVariable id: String): Quote {
        println("DEBUG: GET /api/quotes/$id - Retrieving specific quote")

        val quote = quoteService.getQuoteById(id)
        println("DEBUG: Found quote from '${quote.movieTitle}' with ID: ${quote.id}")

        return quote
    }

    /**
     * Delete a quote by ID
     * DELETE /api/quotes/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteQuote(@PathVariable id: String) {
        println("DEBUG: DELETE /api/quotes/$id - Deleting quote")

        quoteService.deleteQuote(id)
        println("DEBUG: Quote deleted successfully, returning 204")
    }

    /**
     * Search quotes by character and/or movie title
     * GET /api/quotes/search?character={name}&movie={title}
     */
    @GetMapping("/search")
    fun searchQuotes(
        @RequestParam(required = false) character: String?,
        @RequestParam(required = false) movie: String?
    ): List<Quote> {
        println("DEBUG: GET /api/quotes/search - character: '$character', movie: '$movie'")

        val results = quoteService.searchQuotes(character, movie)
        println("DEBUG: Search returned ${results.size} results")

        return results
    }

    /**
     * Get the quote of the day
     * GET /api/quotes/today
     */
    @GetMapping("/today")
    fun getQuoteOfTheDay(): Quote {
        println("DEBUG: GET /api/quotes/today - Getting quote of the day")

        val quote = quoteService.getQuoteOfTheDay()
        println("DEBUG: Quote of the day (ID: ${quote.id}) from '${quote.movieTitle}'")

        return quote
    }
}