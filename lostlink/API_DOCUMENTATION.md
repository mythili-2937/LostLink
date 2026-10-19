
# LostLink REST API Documentation

## Project Overview
LostLink is a campus Lost & Found application developed
using Java, Spring Boot, Spring Security, Spring Data JPA,
Thymeleaf, and PostgreSQL.

## Base URL
http://localhost:8080/api/items

## Authentication
All API endpoints require an authenticated LostLink
session. POST, PUT, and DELETE requests also require
a valid CSRF token.

## API Endpoints

### 1. Get All Items
Method: GET
Endpoint: /api/items
Success: 200 OK

### 2. Get Item By ID
Method: GET
Endpoint: /api/items/{id}
Success: 200 OK
Not Found: 404

### 3. Create Item
Method: POST
Endpoint: /api/items
Success: 201 Created
Validation Error: 400

### 4. Update Item
Method: PUT
Endpoint: /api/items/{id}
Success: 200 OK
Validation Error: 400
Forbidden: 403
Not Found: 404

### 5. Delete Item
Method: DELETE
Endpoint: /api/items/{id}
Success: 204 No Content
Forbidden: 403
Not Found: 404
Existing Claims: 409 Conflict

## Request JSON Example

{
  "title": "Student ID Card",
  "type": "FOUND",
  "category": "Others",
  "description": "Found near the college gate.",
  "location": "Main Gate",
  "eventDate": "2026-10-18"
}

## Validation
@NotBlank - Required text fields
@NotNull - Required non-text fields
@Size - Maximum text length
@PastOrPresent - Prevent future event dates

## Exception Handling
ItemNotFoundException handles missing items.
GlobalExceptionHandler provides JSON error responses
using @RestControllerAdvice.

## Architecture
REST Controller -> Service -> JPA Repository -> PostgreSQL

## Testing
REST APIs are tested using Postman.
Testing evidence is stored in evaluation-screenshots/.
