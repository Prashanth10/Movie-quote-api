package com.example.movie_quote_api.service

import com.example.movie_quote_api.exception.DuplicateQuoteException
import com.example.movie_quote_api.exception.QuoteNotFoundException
import com.example.movie_quote_api.model.Quote
import com.example.movie_quote_api.repository.QuoteRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.random.Random

@Service
class QuoteService(private val quoteRepository: QuoteRepository) {

    companion object {
        private const val MIN_SEARCH_LENGTH = 1
        private const val MAX_SEARCH_LENGTH = 100
    }

    init {
        println("DEBUG: QuoteService initialized with repository-managed ID generation")
    }

    /**
     * Add a new quote
     */
    fun addQuote(quote: Quote): Quote {
        println("DEBUG: Service.addQuote - Movie: '${quote.movieTitle}', incoming ID: ${quote.id}")

        // Ensure no ID is provided for new quotes, Repository will generate
        if (quote.id != null) {
            println("DEBUG: Rejecting quote creation with pre-existing ID: ${quote.id}")
            throw IllegalArgumentException("Cannot create quote with existing ID. Use update instead.")
        }

        validateQuoteData(quote)
        checkForDuplicateQuote(quote)

        val savedQuote = quoteRepository.save(quote)
        println("DEBUG: Quote created successfully with repository-generated ID: ${savedQuote.id}")

        return savedQuote
    }

    /**
     * Get all quotes sorted by creation time
     */
    fun getAllQuotes(): List<Quote> {
        println("DEBUG: Service.getAllQuotes called")
        val quotes = quoteRepository.findAll()
        println("DEBUG: Retrieved ${quotes.size} quotes from repository")
        return quotes
    }

    /**
     * Get a specific quote by ID
     */
    fun getQuoteById(id: String): Quote {
        println("DEBUG: Service.getQuoteById - ID: $id")

        val quote = quoteRepository.findById(id)
            ?: throw QuoteNotFoundException("Quote not found with id: $id")

        println("DEBUG: Found quote: '${quote.movieTitle}' by ${quote.character}")
        return quote
    }

    /**
     * Delete a quote by ID
     */
    fun deleteQuote(id: String) {
        println("DEBUG: Service.deleteQuote - ID: $id")

        val wasDeleted = quoteRepository.deleteById(id)
        if (!wasDeleted) {
            throw QuoteNotFoundException("Quote not found with id: $id")
        }

        println("DEBUG: Quote successfully deleted")
    }

    /**
     * Search quotes by character and/or movie title
     */
    fun searchQuotes(character: String?, movieTitle: String?): List<Quote> {
        println("DEBUG: Service.searchQuotes - character: '$character', movie: '$movieTitle'")

        return when {
            character != null && movieTitle != null -> {
                println("DEBUG: Performing combined search")
                validateSearchTerm(character, "character")
                validateSearchTerm(movieTitle, "movie title")
                searchByCharacterAndMovie(character, movieTitle)
            }

            character != null -> {
                println("DEBUG: Searching by character only")
                validateSearchTerm(character, "character")
                quoteRepository.findByCharacterContainingIgnoreCase(character)
            }

            movieTitle != null -> {
                println("DEBUG: Searching by movie title only")
                validateSearchTerm(movieTitle, "movie title")
                quoteRepository.findByMovieTitleContainingIgnoreCase(movieTitle)
            }

            else -> {
                println("DEBUG: No search criteria, returning all quotes")
                getAllQuotes()
            }
        }
    }

    /**
     * Get quote of the day using deterministic date-based selection
     */
    fun getQuoteOfTheDay(): Quote {
        println("DEBUG: Service.getQuoteOfTheDay called")

        val allQuotes = getAllQuotes()
        if (allQuotes.isEmpty()) {
            println("DEBUG: No quotes available for quote of the day")
            throw QuoteNotFoundException("No quotes available for quote of the day")
        }

        val today = LocalDate.now()
        val seed = today.toEpochDay()
        println("DEBUG: Using date $today (epoch day: $seed) as random seed")

        val random = Random(seed)
        val selectedQuote = allQuotes.shuffled(random).first()

        println("DEBUG: Selected quote of the day (ID: ${selectedQuote.id}) from '${selectedQuote.movieTitle}'")
        return selectedQuote
    }

    // Private helper methods
    private fun validateQuoteData(quote: Quote) {
        println("DEBUG: Validating quote data for '${quote.movieTitle}'")

        if (quote.text.isBlank()) {
            throw IllegalArgumentException("Quote text cannot be blank")
        }
        if (quote.character.isBlank()) {
            throw IllegalArgumentException("Character name cannot be blank")
        }
        if (quote.movieTitle.isBlank()) {
            throw IllegalArgumentException("Movie title cannot be blank")
        }
        if (quote.releaseYear <= 0) {
            throw IllegalArgumentException("Release year must be positive")
        }

        println("DEBUG: Quote data validation passed")
    }

    private fun checkForDuplicateQuote(quote: Quote) {
        println("DEBUG: Checking for duplicates in '${quote.movieTitle}'")

        val existingQuotes = quoteRepository.findByMovieTitleContainingIgnoreCase(quote.movieTitle)
        val isDuplicate = existingQuotes.any { existing ->
            existing.text.equals(quote.text, ignoreCase = true) &&
                    existing.character.equals(quote.character, ignoreCase = true)
        }

        if (isDuplicate) {
            println("DEBUG: Duplicate detected - same text and character in same movie")
            throw DuplicateQuoteException(
                "Quote '${quote.text.take(30)}...' by ${quote.character} already exists in ${quote.movieTitle}"
            )
        }

        println("DEBUG: No duplicate found, quote is unique")
    }

    private fun searchByCharacterAndMovie(character: String, movieTitle: String): List<Quote> {
        println("DEBUG: Performing intersection search")

        val characterResults = quoteRepository.findByCharacterContainingIgnoreCase(character).toSet()
        val movieResults = quoteRepository.findByMovieTitleContainingIgnoreCase(movieTitle).toSet()

        val intersection = characterResults.intersect(movieResults)
            .sortedByDescending { it.createdAt }

        println("DEBUG: Intersection search found ${intersection.size} quotes")
        return intersection
    }

    private fun validateSearchTerm(searchTerm: String, fieldName: String) {
        val trimmed = searchTerm.trim()
        println("DEBUG: Validating search term for $fieldName: '$trimmed'")

        if (trimmed.length < MIN_SEARCH_LENGTH) {
            throw IllegalArgumentException("$fieldName search term must be at least $MIN_SEARCH_LENGTH character(s)")
        }
        if (trimmed.length > MAX_SEARCH_LENGTH) {
            throw IllegalArgumentException("$fieldName search term cannot exceed $MAX_SEARCH_LENGTH characters")
        }

        println("DEBUG: Search term validation passed")
    }
}