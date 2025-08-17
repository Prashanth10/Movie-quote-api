package com.example.movie_quote_api.controller

import com.example.movie_quote_api.model.Quote
import com.example.movie_quote_api.dto.QuoteRequest
import com.example.movie_quote_api.repository.QuoteRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

/**
 * Integration tests for REST endpoints
 * Tests full HTTP request/response cycle with real Spring context
 */
@SpringBootTest
@AutoConfigureWebMvc
class QuoteControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var quoteRepository: QuoteRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    companion object {
        private val SAMPLE_REQUEST = QuoteRequest(
            text = "Why so serious?",
            character = "Joker",
            movieTitle = "The Dark Knight",
            releaseYear = 2008
        )

        private val INVALID_REQUEST = QuoteRequest(
            text = "",
            character = "Test Character",
            movieTitle = "Test Movie",
            releaseYear = 2000
        )
    }

    @BeforeEach
    fun setup() {
        println("DEBUG: Setting up integration test environment")
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        quoteRepository.clear() // Clean slate for each test
    }

    @AfterEach
    fun cleanup() {
        quoteRepository.clear()
        println("DEBUG: Cleaned up test data")
    }

    @Nested
    @DisplayName("POST /api/quotes - Create Quote")
    inner class CreateQuoteTests {

        @Test
        fun `should create quote successfully with valid request`() {
            println("DEBUG: Testing POST /api/quotes with valid request")

            mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(SAMPLE_REQUEST))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.text").value("Why so serious?"))
                .andExpect(jsonPath("$.character").value("Joker"))
                .andExpect(jsonPath("$.movieTitle").value("The Dark Knight"))
                .andExpect(jsonPath("$.releaseYear").value(2008))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())

            // Verify quote was actually saved
            assertEquals(1, quoteRepository.count())
            println("DEBUG: Create quote integration test passed")
        }

        @Test
        fun `should return 400 for invalid request`() {
            println("DEBUG: Testing POST /api/quotes with invalid request")

            mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(INVALID_REQUEST))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isArray)

            // Verify quote was not saved
            assertEquals(0, quoteRepository.count())
            println("DEBUG: Invalid request test passed")
        }

        @Test
        fun `should return 409 for duplicate quote`() {
            println("DEBUG: Testing POST /api/quotes with duplicate quote")

            // First request should succeed
            mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(SAMPLE_REQUEST))
            ).andExpect(status().isCreated)

            // Second identical request should fail
            mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(SAMPLE_REQUEST))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_QUOTE"))

            // Should still have only one quote
            assertEquals(1, quoteRepository.count())
            println("DEBUG: Duplicate quote test passed")
        }

        @Test
        fun `should trim whitespace in request fields`() {
            println("DEBUG: Testing whitespace trimming in request")

            val requestWithWhitespace = SAMPLE_REQUEST.copy(
                text = "Why so serious?",
                character = "Joker",
                movieTitle = "The Dark Knight",
            )

            mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithWhitespace))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.text").value("Why so serious?"))
                .andExpect(jsonPath("$.character").value("Joker"))
                .andExpect(jsonPath("$.movieTitle").value("The Dark Knight"))

            println("DEBUG: Whitespace trimming test passed")
        }
    }

    @Nested
    @DisplayName("GET /api/quotes - List All Quotes")
    inner class GetAllQuotesTests {

        @Test
        fun `should return empty list when no quotes exist`() {
            println("DEBUG: Testing GET /api/quotes with empty repository")

            mockMvc.perform(get("/api/quotes"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(0))

            println("DEBUG: Empty list test passed")
        }

        @Test
        fun `should return all quotes sorted by creation time`() {
            println("DEBUG: Testing GET /api/quotes with multiple quotes")

            // Create test quotes
            val quote1 = createTestQuote(SAMPLE_REQUEST)
            val quote2 = createTestQuote(
                SAMPLE_REQUEST.copy(
                    text = "I'll be back",
                    character = "Terminator",
                    movieTitle = "The Terminator",
                    releaseYear = 1984
                )
            )

            mockMvc.perform(get("/api/quotes"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists())

            println("DEBUG: Get all quotes test passed")
        }

        private fun createTestQuote(request: QuoteRequest): String {
            val response = mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated)
                .andReturn()

            val quote = objectMapper.readValue(response.response.contentAsString, Quote::class.java)
            return quote.id!!
        }
    }

    @Nested
    @DisplayName("GET /api/quotes/{id} - Get Quote by ID")
    inner class GetQuoteByIdTests {

        @Test
        fun `should return quote when ID exists`() {
            println("DEBUG: Testing GET /api/quotes/{id} with existing ID")

            val quoteId = createTestQuote(SAMPLE_REQUEST)

            mockMvc.perform(get("/api/quotes/$quoteId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(quoteId))
                .andExpect(jsonPath("$.text").value("Why so serious?"))
                .andExpect(jsonPath("$.character").value("Joker"))

            println("DEBUG: Get quote by ID test passed")
        }

        @Test
        fun `should return 404 when ID does not exist`() {
            println("DEBUG: Testing GET /api/quotes/{id} with non-existent ID")

            mockMvc.perform(get("/api/quotes/nonexistent"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errorCode").value("QUOTE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Quote not found with id: nonexistent"))

            println("DEBUG: Non-existent ID test passed")
        }

        private fun createTestQuote(request: QuoteRequest): String {
            val response = mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated)
                .andReturn()

            val quote = objectMapper.readValue(response.response.contentAsString, Quote::class.java)
            return quote.id!!
        }
    }

    @Nested
    @DisplayName("DELETE /api/quotes/{id} - Delete Quote")
    inner class DeleteQuoteTests {

        @Test
        fun `should delete quote successfully when ID exists`() {
            println("DEBUG: Testing DELETE /api/quotes/{id} with existing ID")

            val quoteId = createTestQuote(SAMPLE_REQUEST)
            assertEquals(1, quoteRepository.count())

            mockMvc.perform(delete("/api/quotes/$quoteId"))
                .andExpect(status().isNoContent)

            assertEquals(0, quoteRepository.count())
            println("DEBUG: Delete quote test passed")
        }

        @Test
        fun `should return 404 when deleting non-existent quote`() {
            println("DEBUG: Testing DELETE /api/quotes/{id} with non-existent ID")

            mockMvc.perform(delete("/api/quotes/nonexistent"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errorCode").value("QUOTE_NOT_FOUND"))

            println("DEBUG: Delete non-existent quote test passed")
        }

        private fun createTestQuote(request: QuoteRequest): String {
            val response = mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated)
                .andReturn()

            val quote = objectMapper.readValue(response.response.contentAsString, Quote::class.java)
            return quote.id!!
        }
    }

    @Nested
    @DisplayName("GET /api/quotes/search - Search Quotes")
    inner class SearchQuotesTests {

        @BeforeEach
        fun setupSearchData() {
            // Create test data for search tests
            createTestQuote(SAMPLE_REQUEST) // Star Wars quote
            createTestQuote(
                SAMPLE_REQUEST.copy(
                    text = "I'll be back",
                    character = "Terminator",
                    movieTitle = "The Terminator",
                    releaseYear = 1984
                )
            )
            createTestQuote(
                SAMPLE_REQUEST.copy(
                    text = "Another Dark Knight quote",
                    character = "Batman",
                    movieTitle = "The Dark Knight",
                    releaseYear = 2008
                )
            )
        }

        @Test
        fun `should search by character name`() {
            println("DEBUG: Testing search by character")

            mockMvc.perform(get("/api/quotes/search?character=Joker"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].character").value("Joker"))

            println("DEBUG: Search by character test passed")
        }

        @Test
        fun `should search by movie title`() {
            println("DEBUG: Testing search by movie title")

            mockMvc.perform(get("/api/quotes/search?movie=Dark Knight"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))

            println("DEBUG: Search by movie test passed")
        }

        @Test
        fun `should search by both character and movie`() {
            println("DEBUG: Testing search by both character and movie")

            mockMvc.perform(get("/api/quotes/search?character=Joker&movie=The Dark Knight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].character").value("Joker"))

            println("DEBUG: Combined search test passed")
        }

        @Test
        fun `should return all quotes when no search parameters`() {
            println("DEBUG: Testing search with no parameters")

            mockMvc.perform(get("/api/quotes/search"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(3))

            println("DEBUG: No parameters search test passed")
        }

        @Test
        fun `should return 400 for search term too long`() {
            println("DEBUG: Testing search with term too long")

            val longSearchTerm = "a".repeat(101)

            mockMvc.perform(get("/api/quotes/search?character=$longSearchTerm"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"))

            println("DEBUG: Long search term test passed")
        }

        private fun createTestQuote(request: QuoteRequest): String {
            val response = mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated)
                .andReturn()

            val quote = objectMapper.readValue(response.response.contentAsString, Quote::class.java)
            return quote.id!!
        }
    }

    @Nested
    @DisplayName("GET /api/quotes/today - Quote of the Day")
    inner class QuoteOfTheDayTests {

        @Test
        fun `should return quote of the day when quotes exist`() {
            println("DEBUG: Testing GET /api/quotes/today")

            createTestQuote(SAMPLE_REQUEST)

            mockMvc.perform(get("/api/quotes/today"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.character").exists())

            println("DEBUG: Quote of the day test passed")
        }

        @Test
        fun `should return 404 when no quotes exist`() {
            println("DEBUG: Testing GET /api/quotes/today with no quotes")

            mockMvc.perform(get("/api/quotes/today"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.errorCode").value("QUOTE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No quotes available for quote of the day"))

            println("DEBUG: No quotes for quote of the day test passed")
        }

        @Test
        fun `should return same quote of the day on multiple calls`() {
            println("DEBUG: Testing quote of the day consistency")

            createTestQuote(SAMPLE_REQUEST)
            createTestQuote(
                SAMPLE_REQUEST.copy(
                    text = "Different quote",
                    character = "Different character",
                    movieTitle = "Different movie",
                    releaseYear = 2000
                )
            )

            // First call
            val response1 = mockMvc.perform(get("/api/quotes/today"))
                .andExpect(status().isOk)
                .andReturn()

            // Second call
            val response2 = mockMvc.perform(get("/api/quotes/today"))
                .andExpect(status().isOk)
                .andReturn()

            // Should be the same quote
            assertEquals(response1.response.contentAsString, response2.response.contentAsString)
            println("DEBUG: Quote of the day consistency test passed")
        }

        private fun createTestQuote(request: QuoteRequest): String {
            val response = mockMvc.perform(
                post("/api/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated)
                .andReturn()

            val quote = objectMapper.readValue(response.response.contentAsString, Quote::class.java)
            return quote.id!!
        }
    }
}
