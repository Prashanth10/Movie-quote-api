# Movie Quote API

A RESTful API for managing memorable movie quotes built with Kotlin and Spring Boot.

## Overview

This application allows users to create, retrieve, search, and manage movie quotes. It features duplicate detection, case-insensitive search, and a "Quote of the Day" functionality.

## Requirements

- **Java 17** or higher
- **Gradle 7+** (wrapper included)

## API Endpoints
**API available at:** `http://localhost:8080`

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/api/quotes` | Create a new quote | 201, 400, 409 |
| GET | `/api/quotes` | Get all quotes | 200 |
| GET | `/api/quotes/{id}` | Get quote by ID | 200, 404 |
| GET | `/api/quotes/search` | Search quotes | 200, 400 |
| GET | `/api/quotes/today` | Get quote of the day | 200, 404 |
| DELETE | `/api/quotes/{id}` | Delete a quote | 204, 404 |

### Search Parameters
- `character`: Search by character name (case-insensitive, partial match)
- `movie`: Search by movie title (case-insensitive, partial match)


## Data Validation

- **Text, Character, Movie Title**: Cannot be blank, required
- **Release Year**: Must be positive number, required
- **Search Terms**: 1-100 characters when provided
- **Duplicates**: Same text + character + movie = rejected with 409
- **New Quotes**: ID must be null (auto-generated)

## Testing Instructions

### Run All Tests
./gradlew test

### Generate Coverage Report
./gradlew jacocoTestReport

### View Coverage Report
Open the generated HTML report in your browser:

**macOS/Linux:**
open build/reports/jacoco/test/html/index.html


**Windows:**
start build\reports\jacoco\test\html\index.html
