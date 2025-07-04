# Project Conventions and Guidelines

This document outlines the coding conventions and architectural guidelines for the project, ensuring consistency, maintainability, and adherence to best practices.

## 1. Project Overview

This project is a modern web application built with the following stack:

- **Frontend:** React (TypeScript), using Vite as the build tool and Bun as the package manager/runtime.
- **Backend:** Spring Boot, integrated with Vaadin Hilla for seamless type-safe communication between frontend and backend.
- **Communication:** Vaadin Hilla endpoints and services, leveraging annotations for security and browser-callable APIs.

## 2. Code Formatting

### 2.1. Frontend (TypeScript/React) - Based on `biome.json`

- **Indentation Style:** Spaces
- **Indentation Width:** 3 spaces
- **Line Ending:** LF (Line Feed)
- **Line Width:** 121 characters
- **Quote Style:** Single quotes
- **Semicolons:** As needed
- **Trailing Commas:** All
- **Bracket Spacing:** True

### 2.2. Backend (Java) - Based on `eclipse-formatter.xml`

- **Indentation Style:** Spaces
- **Indentation Width:** 5 spaces
- **Line Width:** 121 characters
- **Blank Lines:**
  - 2 blank line after package declaration.
  - 2 blank line before imports.
  - 2 blank line after imports.
  - 2 blank line before each method declaration.
- **Brace Position:** End of line for type and method declarations.

## 3. Clean Code Principles (Robert C. Martin)

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

## 4. Vaadin Hilla and Spring Boot Integration

When interacting between the frontend (Hilla) and backend (Spring Boot), pay close attention to the following:

- **Service Calls:** Frontend calls to backend services must target classes annotated with `@BrowserCallable`.
- **Implementation vs. Interface:** While interfaces (e.g., `AppointmentService`) define contracts, the actual calls from the frontend must be directed to the implementation class (e.g., `AppointmentServiceImpl`) that carries the `@BrowserCallable` annotation. Always verify the presence of this annotation on the concrete service implementation before making frontend calls.
- **Type and Proxy Imports:** Before importing any proxy or type from the backend into the frontend, ensure that the corresponding Java class has the necessary Vaadin Hilla annotations (e.g., `@BrowserCallable`, `@Endpoint`, `@AnonymousAllowed`, `@PermitAll`, `@RolesAllowed`).

