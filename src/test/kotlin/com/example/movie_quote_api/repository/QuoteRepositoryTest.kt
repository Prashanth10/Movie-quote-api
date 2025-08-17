package com.example.movie_quote_api.repository


import com.example.movie_quote_api.model.Quote
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDateTime

/**
 * Unit tests for QuoteRepository
 * Tests isolated repository functionality and ID generation
 */
class QuoteRepositoryTest {

    private lateinit var quoteRepository: QuoteRepository

    companion object {
        private val TEST_QUOTE = Quote(
            id = null,
            text = "Why so serious?",
            character = "Joker",
            movieTitle = "The Dark Knight",
            releaseYear = 2008,
            createdAt = LocalDateTime.now()
        )
    }

    @BeforeEach
    fun setup() {
        println("DEBUG: Setting up QuoteRepositoryTest")
        quoteRepository = QuoteRepository()
    }

    @AfterEach
    fun cleanup() {
        quoteRepository.clear()
    }

    @Nested
    @DisplayName("Save and ID Generation Tests")
    inner class SaveTests {

        @Test
        fun `should generate ID when saving new quote`() {
            println("DEBUG: Testing ID generation on save")

            val savedQuote = quoteRepository.save(TEST_QUOTE)

            assertNotNull(savedQuote.id)
            assertEquals("1", savedQuote.id) // First quote should get ID "1"
            assertEquals(TEST_QUOTE.text, savedQuote.text)
            println("DEBUG: ID generation test passed - Generated ID: ${savedQuote.id}")
        }

        @Test
        fun `should generate sequential IDs for multiple quotes`() {
            println("DEBUG: Testing sequential ID generation")

            val quote1 = quoteRepository.save(TEST_QUOTE)
            val quote2 = quoteRepository.save(TEST_QUOTE.copy(text = "Different quote"))
            val quote3 = quoteRepository.save(TEST_QUOTE.copy(text = "Another quote"))

            assertEquals("1", quote1.id)
            assertEquals("2", quote2.id)
            assertEquals("3", quote3.id)
            println("DEBUG: Sequential ID test passed")
        }

        @Test
        fun `should preserve existing ID when updating`() {
            println("DEBUG: Testing ID preservation on update")

            val savedQuote = quoteRepository.save(TEST_QUOTE)
            val updatedQuote = savedQuote.copy(text = "Updated text")

            val result = quoteRepository.save(updatedQuote)

            assertEquals(savedQuote.id, result.id)
            assertEquals("Updated text", result.text)
            assertEquals(1, quoteRepository.count()) // Should still be only one quote
            println("DEBUG: ID preservation test passed")
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    inner class FindTests {

        @Test
        fun `should find quote by ID when exists`() {
            println("DEBUG: Testing find by ID")

            val savedQuote = quoteRepository.save(TEST_QUOTE)

            val foundQuote = quoteRepository.findById(savedQuote.id!!)

            assertNotNull(foundQuote)
            assertEquals(savedQuote, foundQuote)
            println("DEBUG: Find by ID test passed")
        }

        @Test
        fun `should return null when quote not found by ID`() {
            println("DEBUG: Testing find by non-existent ID")

            val foundQuote = quoteRepository.findById("nonexistent")

            assertNull(foundQuote)
            println("DEBUG: Non-existent ID test passed")
        }

        @Test
        fun `should return all quotes sorted by creation time`() {
            println("DEBUG: Testing find all with sorting")
            quoteRepository.clear()

            // Create quotes with explicit different timestamps
            val quote1 = quoteRepository.save(
                TEST_QUOTE.copy(
                    createdAt = LocalDateTime.now().minusMinutes(1)
                )
            )
            val quote2 = quoteRepository.save(
                TEST_QUOTE.copy(
                    text = "Second quote",
                    createdAt = LocalDateTime.now()
                )
            )

            val allQuotes = quoteRepository.findAll()

            assertEquals(2, allQuotes.size)

            assertEquals("Second quote", allQuotes[0].text)
            assertEquals(TEST_QUOTE.text, allQuotes[1].text)

            println("DEBUG: Find all sorting test passed")
        }

        @Test
        fun `should find quotes by character name case insensitive`() {
            println("DEBUG: Testing find by character case insensitive")

            quoteRepository.save(TEST_QUOTE)
            quoteRepository.save(
                TEST_QUOTE.copy(
                    character = "Batman",
                    text = "Different quote"
                )
            )

            val results = quoteRepository.findByCharacterContainingIgnoreCase("joker")

            assertEquals(1, results.size)
            assertEquals("Joker", results[0].character)
            println("DEBUG: Character search test passed")
        }

        @Test
        fun `should find quotes by movie title case insensitive`() {
            println("DEBUG: Testing find by movie title case insensitive")

            quoteRepository.save(TEST_QUOTE)
            quoteRepository.save(
                TEST_QUOTE.copy(
                    movieTitle = "The Terminator",
                    text = "Different quote"
                )
            )

            val results = quoteRepository.findByMovieTitleContainingIgnoreCase("dark knight")

            assertEquals(1, results.size)
            assertEquals("The Dark Knight", results[0].movieTitle)
            println("DEBUG: Movie title search test passed")
        }

        @Test
        fun `should find quotes by partial character name`() {
            println("DEBUG: Testing find by partial character name")

            quoteRepository.save(TEST_QUOTE)

            val results = quoteRepository.findByCharacterContainingIgnoreCase("Jok")

            assertEquals(1, results.size)
            assertEquals("Joker", results[0].character)
            println("DEBUG: Partial character search test passed")
        }
    }

    @Nested
    @DisplayName("Delete and Count Tests")
    inner class DeleteTests {

        @Test
        fun `should delete quote when exists`() {
            println("DEBUG: Testing delete existing quote")

            val savedQuote = quoteRepository.save(TEST_QUOTE)
            assertEquals(1, quoteRepository.count())

            val deleted = quoteRepository.deleteById(savedQuote.id!!)

            assertTrue(deleted)
            assertEquals(0, quoteRepository.count())
            assertNull(quoteRepository.findById(savedQuote.id!!))
            println("DEBUG: Delete existing quote test passed")
        }

        @Test
        fun `should return false when deleting non-existent quote`() {
            println("DEBUG: Testing delete non-existent quote")

            val deleted = quoteRepository.deleteById("nonexistent")

            assertFalse(deleted)
            assertEquals(0, quoteRepository.count())
            println("DEBUG: Delete non-existent quote test passed")
        }

        @Test
        fun `should count quotes correctly`() {
            println("DEBUG: Testing quote counting")

            assertEquals(0, quoteRepository.count())

            quoteRepository.save(TEST_QUOTE)
            assertEquals(1, quoteRepository.count())

            quoteRepository.save(TEST_QUOTE.copy(text = "Second quote"))
            assertEquals(2, quoteRepository.count())

            quoteRepository.deleteById("1")
            assertEquals(1, quoteRepository.count())

            println("DEBUG: Count test passed")
        }

        @Test
        fun `should check existence correctly`() {
            println("DEBUG: Testing existence check")

            val savedQuote = quoteRepository.save(TEST_QUOTE)

            assertTrue(quoteRepository.existsById(savedQuote.id!!))
            assertFalse(quoteRepository.existsById("nonexistent"))

            println("DEBUG: Existence check test passed")
        }
    }

    @Nested
    @DisplayName("Clear and Reset Tests")
    inner class ClearTests {

        @Test
        fun `should clear all quotes and reset ID generator`() {
            println("DEBUG: Testing clear and reset functionality")

            quoteRepository.save(TEST_QUOTE)
            quoteRepository.save(TEST_QUOTE.copy(text = "Second quote"))
            assertEquals(2, quoteRepository.count())

            quoteRepository.clear()

            assertEquals(0, quoteRepository.count())

            val newQuote = quoteRepository.save(TEST_QUOTE)
            assertEquals("1", newQuote.id)

            println("DEBUG: Clear and reset test passed")
        }
    }
}
