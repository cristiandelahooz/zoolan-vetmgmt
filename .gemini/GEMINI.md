# Project Conventions and Guidelines

This document outlines the coding conventions and architectural guidelines for the project, ensuring consistency, maintainability, and adherence to best practices.

## 1. Project Overview

This project is a modern web application built with the following stack:

- **Frontend:** React (TypeScript), using Vite as the build tool and Bun as the package manager/runtime.
- **Backend:** Spring Boot, integrated with Vaadin Hilla for seamless type-safe communication between frontend and backend.
- **Communication:** Vaadin Hilla endpoints and services, leveraging annotations for security and browser-callable APIs.

## 2. Project Details

### 2.1. Frontend
- **Framework:** React 18 with TypeScript
- **Build Tool:** Vite
- **Package Manager:** Bun
- **Linting/Formatting:** Biome
- **Styling:** Tailwind CSS
- **State Management:** Zustand
- **UI Components:**
    - Vaadin React Components
    - Radix UI (for specific components like dropdowns)
    - FullCalendar (for calendar views)
- **Routing:** Vaadin Hilla File-based routing

### 2.2. Backend
- **Framework:** Spring Boot 3
- **Language:** Java 21
- **Build Tool:** Maven
- **Database:** PostgreSQL
- **Database Migrations:** Flyway
- **Object Mapping:** MapStruct
- **Testing:**
    - JUnit 5
    - Testcontainers
    - ArchUnit

### 2.3. Integration
- **Frontend-Backend:** Vaadin Hilla

## 3. Code Formatting

### 3.1. Frontend (TypeScript/React) - Based on `biome.json`

- **Indent Style:** space
- **Indent Width:** 2
- **Line Ending:** lf
- **Line Width:** 120
- **Quote Style:** single
- **JSX Quote Style:** double
- **Trailing Commas:** all
- **Semicolons:** asNeeded
- **Arrow Parentheses:** always
- **Bracket Spacing:** true

### 3.2. Backend (Java) - Based on `eclipse-formatter.xml`

- **Indent Style:** space
- **Indent Width:** 4
- **Line Width:** 120
- **Brace Position:** End of line for type, method, and block declarations.

## 4. TypeScript Best Practices

- **Avoid `any`:** The use of the `any` type is strictly forbidden. It undermines the benefits of TypeScript and should be avoided at all costs. If a type is unknown, use `unknown` and perform the necessary type checking.

## 5. Key Libraries and Frameworks

- **Vaadin Hilla:** The core of the project, enabling seamless integration between the Spring Boot backend and the React frontend. It provides type-safe server communication, file-based routing, and UI components.
- **Zustand:** A lightweight state management library for React, used for managing global application state.
- **Tailwind CSS:** A utility-first CSS framework for rapidly building custom designs.
- **MapStruct:** A code generator that simplifies the implementation of mappings between Java bean types.
- **ArchUnit:** A Java library for checking the architecture of your application, ensuring that dependencies and coding rules are respected.
- **Testcontainers:** A Java library that provides lightweight, throwaway instances of common databases, Selenium web browsers, or anything else that can run in a Docker container.

## 6. Commands

- **Run development server:** `mvn spring-boot:run`
- **Run frontend tests:** (No dedicated frontend tests found yet)
- **Run backend tests:** `mvn test`
- **Format code:** `mvn spotless:apply`

## 7. Clean Code Principles (Robert C. Martin)

Adhere to the following principles to ensure clean, readable, and maintainable code:

- **Functions:**
  - Functions should be small and do one thing, and one thing only, well.
  - Functions should have a maximum of two parameters. If more are needed, consider creating a dedicated data transfer object (DTO) or a configuration object.
- **Meaningful Names:** Use descriptive and unambiguous names for variables, functions, classes, and files. Names should clearly convey their purpose and intent.
- **Comments:**
  - Avoid unnecessary comments. Code should be self-documenting.
  - If a piece of code requires a comment, it often indicates that the code itself can be made more readable or refactored.
  - Use Javadoc for public APIs and complex logic where necessary.
  - `// TODO:` comments are acceptable for pending tasks.

## 8. Vaadin Hilla and Spring Boot Integration

When interacting between the frontend (Hilla) and backend (Spring Boot), pay close attention to the following:

- **Service Calls:** Frontend calls to backend services must target classes annotated with `@BrowserCallable`.
- **Implementation vs. Interface:** While interfaces (e.g., `AppointmentService`) define contracts, the actual calls from the frontend must be directed to the implementation class (e.g., `AppointmentServiceImpl`) that carries the `@BrowserCallable` annotation. Always verify the presence of this annotation on the concrete service implementation before making frontend calls.
- **Type and Proxy Imports:** Before importing any proxy or type from the backend into the frontend, ensure that the corresponding Java class has the necessary Vaadin Hilla annotations (e.g., `@BrowserCallable`, `@Endpoint`, `@AnonymousAllowed`, `@PermitAll`, `@RolesAllowed`).
