package com.example.movie_quote_api.repository

import com.example.movie_quote_api.model.Quote
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong


@Repository
class QuoteRepository {

    private val quotes = ConcurrentHashMap<String, Quote>()
    private val idGenerator = AtomicLong(1L)

    init {
        println("DEBUG: QuoteRepository initialized with AtomicLong ID generator starting at 1")
    }

    fun save(quote: Quote): Quote {
        println("DEBUG: Repository.save called - incoming ID: ${quote.id}")

        val savedQuote = if (quote.id == null) {
            val newId = generateId()
            val quoteWithId = quote.copy(id = newId)
            quotes[newId] = quoteWithId
            println("DEBUG: New quote saved with generated ID: $newId")
            quoteWithId
        } else {
            // Update existing quote (keep existing ID)
            quotes[quote.id] = quote
            println("DEBUG: Existing quote updated with ID: ${quote.id}")
            quote
        }

        println("DEBUG: Repository now contains ${quotes.size} quotes")
        return savedQuote
    }

    fun findById(id: String): Quote? {
        println("DEBUG: Repository.findById - searching for ID: $id")
        val quote = quotes[id]
        if (quote != null) {
            println("DEBUG: Found quote: '${quote.movieTitle}' by ${quote.character}")
        } else {
            println("DEBUG: No quote found with ID: $id")
        }
        return quote
    }

    fun findAll(): List<Quote> {
        println("DEBUG: Repository.findAll - total quotes: ${quotes.size}")
        val sortedQuotes = quotes.values.sortedByDescending { it.createdAt }
        println("DEBUG: Quotes sorted by creation time (newest first)")
        return sortedQuotes
    }

    fun deleteById(id: String): Boolean {
        println("DEBUG: Repository.deleteById - attempting to delete ID: $id")
        val removedQuote = quotes.remove(id)
        val wasDeleted = removedQuote != null

        if (wasDeleted) {
            println("DEBUG: Successfully deleted quote from '${removedQuote!!.movieTitle}'")
            println("DEBUG: Remaining quotes: ${quotes.size}")
        } else {
            println("DEBUG: Quote not found for deletion: $id")
        }

        return wasDeleted
    }

    fun findByCharacterContainingIgnoreCase(character: String): List<Quote> {
        val searchTerm = character.lowercase().trim()
        println("DEBUG: Repository searching by character: '$searchTerm'")

        val results = quotes.values.filter { quote ->
            quote.character.lowercase().contains(searchTerm)
        }.sortedByDescending { it.createdAt }

        println("DEBUG: Character search found ${results.size} quotes")
        return results
    }

    fun findByMovieTitleContainingIgnoreCase(movieTitle: String): List<Quote> {
        val searchTerm = movieTitle.lowercase().trim()
        println("DEBUG: Repository searching by movie title: '$searchTerm'")

        val results = quotes.values.filter { quote ->
            quote.movieTitle.lowercase().contains(searchTerm)
        }.sortedByDescending { it.createdAt }

        println("DEBUG: Movie title search found ${results.size} quotes")
        return results
    }

    fun count(): Long {
        val totalCount = quotes.size.toLong()
        println("DEBUG: Repository count: $totalCount")
        return totalCount
    }

    fun existsById(id: String): Boolean {
        val exists = quotes.containsKey(id)
        println("DEBUG: Repository existence check for ID '$id': $exists")
        return exists
    }

    private fun generateId(): String {
        val id = idGenerator.getAndIncrement().toString()
        println("DEBUG: Generated new ID: $id")
        return id
    }

    fun clear() {
        val clearedCount = quotes.size
        quotes.clear()
        idGenerator.set(1L)
        println("DEBUG: Repository cleared $clearedCount quotes, ID generator reset to 1")
    }
}