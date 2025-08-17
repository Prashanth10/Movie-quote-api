package com.example.movie_quote_api.service


import com.example.movie_quote_api.model.Quote
import com.example.movie_quote_api.repository.QuoteRepository
import com.example.movie_quote_api.exception.QuoteNotFoundException
import com.example.movie_quote_api.exception.DuplicateQuoteException
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.verify
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDateTime

/**
 * Unit tests for QuoteService business logic
 * Tests isolated service functionality with mocked repository
 */
class QuoteServiceTest {

    @MockK
    private lateinit var mockRepository: QuoteRepository

    private lateinit var quoteService: QuoteService

    companion object {
        private val TEST_QUOTE = Quote(
            id = "1",
            text = "Why so serious?",
            character = "Joker",
            movieTitle = "The Dark Knight",
            releaseYear = 2008,
            createdAt = LocalDateTime.now()
        )

        private val NEW_QUOTE = Quote(
            id = null,
            text = "I'll be back",
            character = "Terminator",
            movieTitle = "The Terminator",
            releaseYear = 1984
        )
    }

    @BeforeEach
    fun setup() {
        println("DEBUG: Setting up QuoteServiceTest")
        MockKAnnotations.init(this)
        quoteService = QuoteService(mockRepository)
    }

    @AfterEach
    fun cleanup() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("Add Quote Tests")
    inner class AddQuoteTests {

        @Test
        fun `should add quote successfully when valid`() {
            println("DEBUG: Testing successful quote addition")

            every { mockRepository.findByMovieTitleContainingIgnoreCase(any()) } returns emptyList()

            every { mockRepository.save(any()) } answers {
                val inputQuote = firstArg<Quote>()
                inputQuote.copy(id = "1")
            }

            val result = quoteService.addQuote(NEW_QUOTE)

            assertNotNull(result.id)
            assertEquals("I'll be back", result.text)
            assertEquals("Terminator", result.character)
            verify { mockRepository.save(NEW_QUOTE) }
            println("DEBUG: Quote addition test passed")
        }

        @Test
        fun `should throw exception when quote has existing ID`() {
            println("DEBUG: Testing quote addition with existing ID")

            val quoteWithId = NEW_QUOTE.copy(id = "existing-id")

            val exception = assertThrows<IllegalArgumentException> {
                quoteService.addQuote(quoteWithId)
            }
            assertEquals("Cannot create quote with existing ID. Use update instead.", exception.message)
            println("DEBUG: Existing ID rejection test passed")
        }

        @Test
        fun `should throw exception for duplicate quote in same movie`() {
            println("DEBUG: Testing duplicate quote detection")

            val existingQuote = TEST_QUOTE.copy(
                text = "I'll be back",
                character = "Terminator"
            )
            every { mockRepository.findByMovieTitleContainingIgnoreCase("The Terminator") } returns listOf(existingQuote)

            assertThrows<DuplicateQuoteException> {
                quoteService.addQuote(NEW_QUOTE)
            }
            println("DEBUG: Duplicate detection test passed")
        }

        @Test
        fun `should throw exception for blank quote text`() {
            println("DEBUG: Testing blank quote text validation")

            val blankQuote = NEW_QUOTE.copy(text = "   ")

            val exception = assertThrows<IllegalArgumentException> {
                quoteService.addQuote(blankQuote)
            }
            assertEquals("Quote text cannot be blank", exception.message)
            println("DEBUG: Blank text validation test passed")
        }

        @Test
        fun `should throw exception for blank character name`() {
            println("DEBUG: Testing blank character validation")

            val blankCharacterQuote = NEW_QUOTE.copy(character = "")

            val exception = assertThrows<IllegalArgumentException> {
                quoteService.addQuote(blankCharacterQuote)
            }
            assertEquals("Character name cannot be blank", exception.message)
            println("DEBUG: Blank character validation test passed")
        }

        @Test
        fun `should throw exception for invalid release year`() {
            println("DEBUG: Testing invalid release year validation")

            val invalidYearQuote = NEW_QUOTE.copy(releaseYear = -1)

            val exception = assertThrows<IllegalArgumentException> {
                quoteService.addQuote(invalidYearQuote)
            }
            assertEquals("Release year must be positive", exception.message)
            println("DEBUG: Invalid year validation test passed")
        }
    }

    @Nested
    @DisplayName("Get Quote Tests")
    inner class GetQuoteTests {

        @Test
        fun `should return all quotes from repository`() {
            println("DEBUG: Testing get all quotes")

            val quotes = listOf(TEST_QUOTE, NEW_QUOTE.copy(id = "2"))
            every { mockRepository.findAll() } returns quotes

            val result = quoteService.getAllQuotes()

            assertEquals(2, result.size)
            verify { mockRepository.findAll() }
            println("DEBUG: Get all quotes test passed")
        }

        @Test
        fun `should return quote by ID when exists`() {
            println("DEBUG: Testing get quote by ID")

            every { mockRepository.findById("1") } returns TEST_QUOTE

            val result = quoteService.getQuoteById("1")

            assertEquals(TEST_QUOTE, result)
            verify { mockRepository.findById("1") }
            println("DEBUG: Get quote by ID test passed")
        }

        @Test
        fun `should throw exception when quote not found by ID`() {
            println("DEBUG: Testing quote not found by ID")

            every { mockRepository.findById("nonexistent") } returns null

            val exception = assertThrows<QuoteNotFoundException> {
                quoteService.getQuoteById("nonexistent")
            }
            assertEquals("Quote not found with id: nonexistent", exception.message)
            println("DEBUG: Quote not found test passed")
        }
    }

    @Nested
    @DisplayName("Delete Quote Tests")
    inner class DeleteQuoteTests {

        @Test
        fun `should delete quote successfully when exists`() {
            println("DEBUG: Testing successful quote deletion")

            every { mockRepository.deleteById("1") } returns true

            assertDoesNotThrow {
                quoteService.deleteQuote("1")
            }

            verify { mockRepository.deleteById("1") }
            println("DEBUG: Delete quote test passed")
        }

        @Test
        fun `should throw exception when deleting non-existent quote`() {
            println("DEBUG: Testing deletion of non-existent quote")

            every { mockRepository.deleteById("nonexistent") } returns false

            val exception = assertThrows<QuoteNotFoundException> {
                quoteService.deleteQuote("nonexistent")
            }
            assertEquals("Quote not found with id: nonexistent", exception.message)
            println("DEBUG: Delete non-existent quote test passed")
        }
    }

    @Nested
    @DisplayName("Search Quote Tests")
    inner class SearchQuoteTests {

        @Test
        fun `should search by character only`() {
            println("DEBUG: Testing search by character only")

            val quotes = listOf(TEST_QUOTE)
            every { mockRepository.findByCharacterContainingIgnoreCase("Obi-Wan") } returns quotes

            val result = quoteService.searchQuotes("Obi-Wan", null)

            assertEquals(1, result.size)
            assertEquals(TEST_QUOTE, result[0])
            verify { mockRepository.findByCharacterContainingIgnoreCase("Obi-Wan") }
            println("DEBUG: Search by character test passed")
        }

        @Test
        fun `should search by movie title only`() {
            println("DEBUG: Testing search by movie title only")

            val quotes = listOf(TEST_QUOTE)
            every { mockRepository.findByMovieTitleContainingIgnoreCase("Star Wars") } returns quotes

            val result = quoteService.searchQuotes(null, "Star Wars")

            assertEquals(1, result.size)
            verify { mockRepository.findByMovieTitleContainingIgnoreCase("Star Wars") }
            println("DEBUG: Search by movie test passed")
        }

        @Test
        fun `should search by both character and movie with intersection`() {
            println("DEBUG: Testing search by both character and movie")

            val characterResults = listOf(TEST_QUOTE, NEW_QUOTE.copy(id = "2"))
            val movieResults = listOf(TEST_QUOTE)

            every { mockRepository.findByCharacterContainingIgnoreCase("Obi") } returns characterResults
            every { mockRepository.findByMovieTitleContainingIgnoreCase("Star") } returns movieResults

            val result = quoteService.searchQuotes("Obi", "Star")

            assertEquals(1, result.size)
            assertEquals(TEST_QUOTE, result[0])
            println("DEBUG: Combined search test passed")
        }

        @Test
        fun `should return all quotes when no search criteria`() {
            println("DEBUG: Testing search with no criteria")

            val allQuotes = listOf(TEST_QUOTE, NEW_QUOTE.copy(id = "2"))
            every { mockRepository.findAll() } returns allQuotes

            val result = quoteService.searchQuotes(null, null)

            assertEquals(2, result.size)
            verify { mockRepository.findAll() }
            println("DEBUG: No criteria search test passed")
        }

        @Test
        fun `should throw exception for search term too short`() {
            println("DEBUG: Testing search term too short")

            val exception = assertThrows<IllegalArgumentException> {
                quoteService.searchQuotes("", null)
            }
            assertTrue(exception.message!!.contains("must be at least 1 character"))
            println("DEBUG: Short search term test passed")
        }

        @Test
        fun `should throw exception for search term too long`() {
            println("DEBUG: Testing search term too long")

            val longSearchTerm = "a".repeat(101)

            val exception = assertThrows<IllegalArgumentException> {
                quoteService.searchQuotes(longSearchTerm, null)
            }
            assertTrue(exception.message!!.contains("cannot exceed 100 characters"))
            println("DEBUG: Long search term test passed")
        }
    }

    @Nested
    @DisplayName("Quote of the Day Tests")
    inner class QuoteOfTheDayTests {

        @Test
        fun `should return quote of the day when quotes exist`() {
            println("DEBUG: Testing quote of the day with available quotes")

            val quotes = listOf(TEST_QUOTE, NEW_QUOTE.copy(id = "2"))
            every { mockRepository.findAll() } returns quotes

            val result = quoteService.getQuoteOfTheDay()

            assertNotNull(result)
            assertTrue(quotes.contains(result))
            verify { mockRepository.findAll() }
            println("DEBUG: Quote of the day test passed")
        }

        @Test
        fun `should return same quote for same date`() {
            println("DEBUG: Testing quote of the day consistency")

            // Given
            val quotes = listOf(TEST_QUOTE, NEW_QUOTE.copy(id = "2"))
            every { mockRepository.findAll() } returns quotes

            val result1 = quoteService.getQuoteOfTheDay()
            val result2 = quoteService.getQuoteOfTheDay()

            assertEquals(result1, result2)
            println("DEBUG: Quote of the day consistency test passed")
        }

        @Test
        fun `should throw exception when no quotes available`() {
            println("DEBUG: Testing quote of the day with no quotes")

            every { mockRepository.findAll() } returns emptyList()

            val exception = assertThrows<QuoteNotFoundException> {
                quoteService.getQuoteOfTheDay()
            }
            assertEquals("No quotes available for quote of the day", exception.message)
            println("DEBUG: No quotes available test passed")
        }
    }
}
