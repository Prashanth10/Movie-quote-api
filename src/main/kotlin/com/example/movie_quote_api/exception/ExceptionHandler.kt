package com.example.movie_quote_api.exception

import com.example.movie_quote_api.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    init {
        println("DEBUG: GlobalExceptionHandler initialized for centralized error handling")
    }

    @ExceptionHandler(QuoteNotFoundException::class)
    fun handleQuoteNotFound(ex: QuoteNotFoundException): ResponseEntity<ErrorResponse> {
        println("DEBUG: Handling QuoteNotFoundException: ${ex.message}")
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Quote not found",
            errorCode = "QUOTE_NOT_FOUND"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(DuplicateQuoteException::class)
    fun handleDuplicateQuote(ex: DuplicateQuoteException): ResponseEntity<ErrorResponse> {
        println("DEBUG: Handling DuplicateQuoteException: ${ex.message}")
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Duplicate quote",
            errorCode = "DUPLICATE_QUOTE"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        println("DEBUG: Handling validation errors. Field error count: ${ex.fieldErrors.size}")

        val validationErrors = ex.fieldErrors.map { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }

        validationErrors.forEach { error ->
            println("DEBUG: Validation error detail - $error")
        }

        val errorResponse = ErrorResponse(
            message = "Validation failed",
            errorCode = "VALIDATION_ERROR",
            details = validationErrors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        println("DEBUG: Handling IllegalArgumentException: ${ex.message}")
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Invalid argument",
            errorCode = "INVALID_ARGUMENT"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericError(ex: Exception): ResponseEntity<ErrorResponse> {
        println("DEBUG: Handling unexpected exception: ${ex.javaClass.simpleName} - ${ex.message}")
        ex.printStackTrace()

        val errorResponse = ErrorResponse(
            message = "An unexpected error occurred",
            errorCode = "INTERNAL_SERVER_ERROR"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
