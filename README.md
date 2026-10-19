# LostLink — Campus Lost & Found Management System

**A secure, centralized web application for reporting, discovering, matching, and recovering lost belongings within a campus.**

## 1. Project Overview

LostLink is a full-stack web application developed using Java Spring Boot to simplify the management of lost and found items in educational institutions.

In a traditional campus environment, students and staff often depend on notice boards, social media groups, or word of mouth to locate missing belongings. These approaches can be inefficient, difficult to track, and unreliable.

LostLink provides a centralized platform where authorized campus users can report lost or found items, search existing reports, identify potential matches, submit ownership claims, and coordinate item recovery.

The application combines a server-rendered web interface, secure authentication, database persistence, business logic, and RESTful APIs.

## 2. Problem Statement

Educational institutions frequently encounter problems involving misplaced belongings such as identification cards, electronic devices, books, wallets, keys, and personal accessories.

Existing informal reporting methods create several challenges:

- No centralized system for managing lost and found reports.
- Difficulty identifying potential matches between lost and found items.
- Limited visibility into the status of reported belongings.
- Lack of a structured process for verifying ownership.
- Difficulty tracking claims and completed recoveries.

LostLink addresses these challenges through a centralized and structured lost and found management system.

## 3. Project Objectives

The main objectives of LostLink are:

1. Provide a secure platform for campus users.
2. Enable users to report lost and found items.
3. Store and retrieve item information using a relational database.
4. Allow searching and filtering of reported items.
5. Recommend potential matches between lost and found reports.
6. Provide a structured ownership claim and approval workflow.
7. Allow users to track the status of their reports and claims.
8. Expose RESTful APIs for item management.
9. Implement input validation, exception handling, and access control.
10. Demonstrate layered Spring Boot application development.

## 4. Key Features

### 4.1 User Authentication

LostLink uses Spring Security for authentication and access control.

Features include:

- Campus ID-based login.
- Secure password storage using password hashing.
- First-login password change.
- Session-based authentication.
- Logout functionality.
- Protection of authenticated application routes.
- CSRF protection for state-changing requests.

### 4.2 Dashboard

The dashboard provides authenticated users with access to the application's main functions.

Users can navigate to:

- Report Lost Item.
- Report Found Item.
- Browse Items.
- My Reports.
- Potential Matches.
- Claim Management.

### 4.3 Lost and Found Reporting

Users can create reports containing:

- Item title.
- Item type: LOST or FOUND.
- Category.
- Description.
- Location.
- Date of loss or discovery.
- Optional item photograph.

Reports are stored in PostgreSQL and associated with the authenticated user.

### 4.4 Browse and Search Items

Users can browse available reports and use search and filtering features to locate relevant items.

This reduces the effort required to find reported belongings.

### 4.5 Report Management

Users can manage reports they created.

Supported operations include:

- View reports.
- Edit report details.
- Mark reports as resolved.
- Delete eligible reports.

Ownership checks restrict unauthorized modification of another user's reports.

### 4.6 Smart Item Matching

LostLink includes a rule-based matching system that recommends potential matches between LOST and FOUND reports.

The matching process considers:

- Item category.
- Keyword similarity.
- Location similarity.
- Date proximity.

The matching system uses weighted scoring to rank potentially related reports.

This is a rule-based recommendation mechanism, not a trained machine learning model.

### 4.7 Ownership Claim Management

Users can submit ownership claims for reported items.

The claim workflow supports:

1. Claim submission.
2. Claim review by the authorized report owner.
3. Claim approval or rejection.
4. Claim cancellation where permitted.
5. Completion of the item handover process.

Claim status is maintained in the database.

**Current scope:** Claim decisions are based on report ownership and existing authorization rules. A separate administrator-wide claim management dashboard is not included in the current implementation.

### 4.8 RESTful Item Management

LostLink exposes REST APIs for managing item reports.

The API supports:

- Retrieve all items.
- Retrieve an item by ID.
- Create an item.
- Update an existing item.
- Delete an eligible item.

The APIs use JSON request and response representations.

### 4.9 Input Validation

Jakarta Bean Validation is used to validate incoming API requests.

Implemented validation annotations include:

- `@NotBlank`
- `@NotNull`
- `@Size`
- `@PastOrPresent`
- `@Valid`

Invalid requests return appropriate HTTP error responses.

### 4.10 Exception Handling

The application uses custom exceptions and centralized REST exception handling.

Examples include:

- `400 Bad Request` — Invalid input.
- `404 Not Found` — Requested item does not exist.
- `409 Conflict` — Operation conflicts with existing data or business rules.

Authorization failures are also handled through the application's security and service layers.

## 5. Technology Stack

| Layer | Technology |
|---|---|
| Programming Language | Java |
| Backend Framework | Spring Boot 4.1.1 |
| Web Framework | Spring MVC |
| Frontend | HTML, CSS, Thymeleaf |
| Authentication | Spring Security |
| Database | PostgreSQL |
| Database Hosting | Supabase |
| Persistence | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| API Architecture | REST |
| API Testing | Postman |
| Build Tool | Maven |
| IDE | Visual Studio Code |
| Version Control | Git |

**Development environment:** Java 25 with the project configured for Java 21 compatibility.

## 6. System Architecture

LostLink follows a layered architecture.

```text
              CAMPUS USER
                   |
                   v
            WEB BROWSER
                   |
                   v
        SPRING SECURITY LAYER
                   |
                   v
       SPRING MVC / REST API
                   |
                   v
             SERVICE LAYER
                   |
                   v
          JPA REPOSITORY LAYER
                   |
                   v
         SUPABASE POSTGRESQL
```

### Architecture Components

**Presentation Layer**

Uses Thymeleaf templates and HTML/CSS to display application pages.

**Controller Layer**

Handles incoming browser requests and REST API requests.

**Service Layer**

Contains business rules, authorization checks, item management, and matching logic.

**Repository Layer**

Uses Spring Data JPA to communicate with PostgreSQL.

**Database Layer**

Stores user information, lost and found reports, claims, and related application data.

**Security Layer**

Provides authentication, session management, and request protection.

## 7. Project Structure

The following shows the main logical structure of the application. Individual Java filenames and template filenames may vary.

```text
lostlink/
|
|-- src/
|   |-- main/
|   |   |-- java/com/lostlink/
|   |   |   |-- controllers/
|   |   |   |-- services/
|   |   |   |-- repositories/
|   |   |   |-- entities/
|   |   |   |-- dto/
|   |   |   |-- security/
|   |   |   `-- exception/
|   |   |
|   |   `-- resources/
|   |       |-- templates/
|   |       |-- static/
|   |       `-- application.properties
|   |
|   `-- test/
|
|-- postman/
|-- evaluation-screenshots/
|-- uploads/
|-- API_DOCUMENTATION.md
|-- README.md
|-- pom.xml
|-- mvnw
`-- mvnw.cmd
```

## 8. Database Design

LostLink uses Supabase-hosted PostgreSQL as its relational database.

The application uses the `app` database schema.

### Main Entities

**User**

Stores authenticated campus user information.

**LostFoundItem**

Stores reports of lost and found belongings.

**Claim**

Stores ownership claims associated with reported items.

### Main Relationships

- One user can create multiple lost and found reports.
- One user can submit multiple ownership claims.
- One item can have multiple associated claims.
- Each claim is associated with a reported item and a claimant.

Database relationships are managed through JPA entity mappings.

## 9. REST API Documentation

### Base URL

```text
http://localhost:8080
```

### Item Endpoints

| Method | Endpoint | Description | Success Status |
|---|---|---|---|
| GET | `/api/items` | Retrieve all items | 200 |
| GET | `/api/items/{id}` | Retrieve an item by ID | 200 |
| POST | `/api/items` | Create an item | 201 |
| PUT | `/api/items/{id}` | Update an item | 200 |
| DELETE | `/api/items/{id}` | Delete an eligible item | 204 |

### Example JSON Request

```json
{
  "title": "Blue Water Bottle",
  "type": "FOUND",
  "category": "Others",
  "description": "Found a blue water bottle near the library.",
  "location": "Library Entrance",
  "eventDate": "2026-10-18"
}
```

### Example API Responses

| HTTP Status | Meaning |
|---|---|
| 200 | Successful retrieval or update |
| 201 | Resource created successfully |
| 204 | Resource deleted successfully |
| 400 | Invalid request data |
| 404 | Requested resource not found |
| 409 | Operation conflicts with business rules |

The item REST endpoints are protected by Spring Security. Authenticated sessions and valid CSRF tokens are required for protected state-changing requests.

For additional details, refer to `API_DOCUMENTATION.md`.

## 10. Installation and Setup

### Prerequisites

Install or configure:

- Java Development Kit.
- Visual Studio Code or another Java IDE.
- Maven, or use the included Maven Wrapper.
- PostgreSQL database hosted on Supabase.
- Postman for API testing.
- Git, if using version control.

### Step 1 — Open the Project

Open the project directory in VS Code:

```text
D:\LostLink\lostlink
```

### Step 2 — Configure the Database

Configure the application's database connection using the existing Spring Boot configuration.

Typical configuration properties include:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

These properties illustrate environment-variable-based configuration. Use the property names and configuration mechanism actually supported by your project.

The database connection must point to the correct Supabase PostgreSQL instance and schema.

**Security note:** Do not commit actual database passwords, API keys, or session tokens to version control.

### Step 3 — Build the Project

Open the terminal in the project root.

On Windows:

```powershell
.\mvnw.cmd clean package
```

### Step 4 — Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

### Step 5 — Open the Website

Visit:

```text
http://localhost:8080/login
```

Sign in using a valid campus account configured for the application.

## 11. API Testing Using Postman

REST API testing was performed using Postman.

The saved Postman request set contains seven requests:

1. GET Login Page.
2. POST Login.
3. GET All Items.
4. POST Create Item.
5. PUT Update Item.
6. DELETE Item.
7. GET Nonexistent Item — 404 Test.

Additional validation behavior is supported by the API.

### Testing Procedure

1. Start the Spring Boot application.
2. Open Postman.
3. Establish an authenticated session.
4. Obtain the CSRF token required for protected requests.
5. Send the required API request.
6. Verify the response status and JSON data.
7. Capture screenshots of the results.

### Testing Evidence

Supporting materials are organized in:

```text
postman/
evaluation-screenshots/
API_DOCUMENTATION.md
```

The Postman collection file can be imported into Postman for reviewing the saved request configurations.

## 12. Security Features

LostLink incorporates the following security mechanisms:

- Spring Security authentication.
- Password hashing.
- Session-based login.
- CSRF protection.
- Protected application routes.
- Owner-based authorization for report modifications.
- Validation of incoming API requests.
- Business-rule restrictions on deleting reports with existing claims.

These measures help protect user information and prevent unauthorized modifications.

## 13. Important Spring Boot Concepts Demonstrated

The project demonstrates several core Java and Spring Boot concepts.

| Concept | Implementation |
|---|---|
| Dependency Injection | Constructor injection of services and repositories |
| Spring MVC | Controllers for web requests |
| REST API | JSON-based CRUD endpoints |
| Spring Data JPA | Database persistence |
| Entity Mapping | JPA entity classes |
| Bean Validation | Validation annotations on request DTOs |
| Exception Handling | Centralized REST exception handling |
| Spring Security | Authentication and authorization |
| Service Layer | Business logic and ownership checks |
| DTO Pattern | Separate API request and response representations |

### Important Annotations

- `@SpringBootApplication`
- `@Controller`
- `@RestController`
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@DeleteMapping`
- `@Service`
- `@Repository`
- `@Entity`
- `@Id`
- `@Valid`
- `@NotBlank`
- `@NotNull`
- `@RestControllerAdvice`

## 14. Testing and Validation

The application has been developed and tested incrementally.

Completed API testing includes:

- Successful retrieval of item records.
- Successful creation of an item.
- Successful updating of an item.
- Successful deletion of an eligible item.
- Handling of nonexistent item IDs.

The API also includes input validation, ownership restrictions, and claim-related deletion safeguards.

A final regression test of the complete website and all negative API scenarios is recommended before evaluation.

## 15. Current Limitations

The current implementation has the following limitations:

- Smart matching is rule-based rather than machine-learning-based.
- A separate admin-wide claim management dashboard is not included.
- Automatic email or push notifications are not implemented.
- Item handover requires coordination between the relevant users.
- The application is configured primarily for local development and testing.

These limitations do not prevent the core lost and found workflow from functioning.

## 16. Future Enhancements

Potential future improvements include:

1. Email notifications for item matches and claim decisions.
2. Administrator moderation dashboard.
3. Secure communication between claimants and finders.
4. Image-assisted item matching.
5. Advanced matching using machine learning.
6. QR-code-based item identification.
7. Analytics and recovery statistics.
8. Mobile-responsive enhancements.
9. Cloud deployment.
10. Audit logs for sensitive actions.

## 17. Project Outcomes

LostLink demonstrates the practical application of full-stack Java development to a real-world campus problem.

The project provides:

- A centralized lost and found platform.
- Secure campus authentication.
- Structured item reporting.
- Search and matching functionality.
- Ownership claim management.
- RESTful CRUD operations.
- Persistent database storage.
- Input validation and exception handling.
- Layered application architecture.

The system illustrates how Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, and PostgreSQL can be integrated into a functional web application.

## 18. Conclusion

LostLink provides a structured and accessible solution for managing lost and found belongings within an educational institution.

By combining item reporting, search, rule-based matching, ownership claims, authentication, database persistence, and RESTful APIs, the application reduces the limitations of informal lost and found management.

The project demonstrates essential software engineering and Spring Boot development concepts while providing a foundation for future enhancements.

---

**Project:** LostLink — Campus Lost & Found Management System  
**Backend:** Java Spring Boot  
**Frontend:** Thymeleaf  
**Database:** Supabase PostgreSQL  
**API Testing:** Postman  
**Project Type:** Full-Stack Web Application