# Project Conventions and Guidelines

This document outlines the coding conventions and architectural guidelines for the project, ensuring consistency, maintainability, and adherence to best practices.

## 1. Project Overview

This project is a modern veterinary management web application named "Zoolan VetMgmt". It is built with a robust, type-safe stack designed for scalability and maintainability.

-   **Frontend:** React (TypeScript), using Vite as the build tool and Bun as the package manager/runtime.
-   **Backend:** Spring Boot (Java 21), utilizing a classic layered architecture (Controller/Service/Repository).
-   **Integration:** Vaadin Hilla provides seamless, type-safe communication between the frontend and backend, enabling direct calls from TypeScript to Java services.
-   **Database:** PostgreSQL with migrations managed by Flyway.

## 2. Architecture and Layering

### 2.1. Backend (Java)

The backend follows a standard layered architecture located in `src/main/java/com/wornux/`:

-   **`config`**: Contains Spring Boot configuration classes, such as `SecurityConfig`.
-   **`constants`**: Defines application-wide constants for validation, appointments, etc.
-   **`data`**: The core data layer.
    -   **`entity`**: JPA entities representing the database schema (e.g., `Client`, `Pet`, `Employee`, `Appointment`).
    -   **`enums`**: Enumerations that define key business concepts and states (e.g., `ServiceType`, `AppointmentStatus`, `EmployeeRole`).
    -   **`repository`**: Spring Data JPA repositories for database access.
-   **`dto`**: Data Transfer Objects used for communication with the frontend. Separated into `request` and `response` packages.
-   **`exception`**: Custom exception classes for handling specific error conditions.
-   **`mapper`**: MapStruct mappers for converting between DTOs and JPA entities.
-   **`service`**: Contains the business logic.
    -   **`interfaces`**: Defines the contracts for the services.
    -   **`implementations`**: Concrete implementations of the service interfaces. These classes are annotated with `@BrowserCallable` to be exposed to the Hilla frontend.
-   **`validation`**: Custom validation annotations and logic.

### 2.2. Frontend (TypeScript/React)

The frontend, located in `src/main/frontend/`, is a modern React application structured for clarity and scalability.

-   **Hilla Integration**: The frontend heavily relies on Vaadin Hilla. Java services annotated with `@BrowserCallable` are made available as asynchronous functions in TypeScript. Type definitions for DTOs and Entities are automatically generated in the `src/main/frontend/generated` directory, ensuring end-to-end type safety.
-   **Folder Structure**:
    -   **`views`**: Contains the application's pages, using Hilla's file-based routing (e.g., `clients/@index.tsx`, `pets/new.tsx`).
    -   **`components`**: Reusable React components, including custom UI elements (`ui/`) built with Radix UI and Vaadin components.
    -   **`stores`**: Global state management using Zustand (e.g., `useAppointments`, `useClients`).
    -   **`lib`**: Utility functions and application-level constants (`constants/`).
-   **UI & Styling**: The UI is built using a combination of Vaadin React Components, Radix UI (for primitives like dropdowns), and FullCalendar. Styling is handled by Tailwind CSS.

### 2.3. Configuration and Database

-   **Spring Profiles**: The `application.yml` file in `src/main/resources/` is configured to handle different environments (e.g., `dev`, `prod`).
-   **Database Migrations**: Flyway is used for managing database schema changes. Migration scripts are located in `src/main/resources/db/migration/`. The structure includes profile-specific subdirectories (`dev/`, `prod/`), allowing for different data seeding and schema adjustments based on the active Spring profile.

## 3. Core Business Logic & Rules

The application models the operations of a veterinary clinic.

### 3.1. Clients & Identification
-   A `Client` can be an individual or a company.
-   Each client **must** have exactly one unique identification document: `cedula` (for Dominican individuals), `passport` (for foreigners), or `RNC` (for companies). This is enforced by a validation constraint in the `Client` entity.
-   Clients have a rating (`ClientRating`), contact preferences, and can have credit limits.

### 3.2. Employees & Roles
-   The system defines multiple employee roles via the `EmployeeRole` enum: `VETERINARIAN`, `RECEPTIONIST`, `GROOMER`, `CLINIC_MANAGER`, etc.
-   Each `EmployeeRole` is mapped to a `SystemRole` (`MANAGER`, `USER`), which can be used for high-level access control.

### 3.3. Pets
-   Pets must be associated with one or more `Client` owners.
-   Each pet has a `PetType` (e.g., `DOG`, `CAT`) and a `breed`. The application includes validation to ensure the breed is valid for the selected type.
-   The system supports a feature to **merge duplicate pet records**, transferring all owners from a duplicate pet to a primary one.
-   Each pet automatically gets a `MedicalHistory` record upon creation.

### 3.4. Appointments
-   Appointments have a status (`AppointmentStatus` enum: `PROGRAMADA`, `EN_PROGRESO`, `COMPLETADA`, `CANCELADA`, `NO_ASISTIO`, etc.).
-   Services offered are defined in the `ServiceType` enum, which distinguishes between clinical services (requiring a vet) and non-clinical services (like grooming).
-   Appointments can be created for registered clients or for "guest" clients (walk-ins), where client info is stored directly in the appointment.

### 3.5. Consultations & Medical History
-   A `Consultation` is a record of a specific visit, linked to a `Pet` and a `Veterinarian` (`Employee`).
-   All consultations for a pet are added to its `MedicalHistory`, creating a comprehensive record of diagnoses, treatments, and notes over time.

### 3.6. Waiting Room
-   The application features a sophisticated waiting room management system.
-   Entries in the waiting room have a `WaitingRoomStatus` (`WAITING`, `IN_CONSULTATION`, `COMPLETED`, `CANCELLED`).
-   A `Priority` system (`NORMAL`, `URGENT`, `EMERGENCY`) is used to manage the queue.
-   The system tracks arrival time, consultation start time, and completion time to calculate wait times.

## 4. Key Libraries and Frameworks

-   **Vaadin Hilla:** Core integration framework.
-   **Spring Boot:** Backend framework.
-   **React:** Frontend library.
-   **Zustand:** Frontend state management.
-   **Tailwind CSS:** Utility-first CSS framework.
-   **MapStruct:** Java bean mapping.
-   **Flyway:** Database migrations.
-   **Vaadin React Components:** Core UI components.
-   **Radix UI:** Headless UI components for accessibility.
-   **FullCalendar:** For calendar-based appointment views.
-   **ArchUnit & Testcontainers:** For backend testing.

## 5. Commands

All common development tasks are managed via the `Makefile`.

-   **Run development server:** `make run`
-   **Build project:** `make build`
-   **Package project (JAR):** `make package`
-   **Clean build artifacts:** `make clean`
-   **Restart application (clean & run):** `make restart`
-   **Run all tests:** `make test`
-   **Format code:** `make format`
-   **Start PostgreSQL container:** `make db-up`
-   **Stop PostgreSQL container:** `make db-down`
-   **Clean PostgreSQL container volumes:** `make db-clean`
-   **Connect to PostgreSQL shell:** `make db-shell`
-   **View PostgreSQL logs:** `make db-logs`

## 6. Security Note

Many backend services (`@BrowserCallable`) are currently annotated with `@AnonymousAllowed`. **This is a significant security risk and must be replaced with role-based authorization (e.g., `@RolesAllowed({...})`, `@PermitAll`) before deploying to a production environment.**

## 7. Future Integrations

Based on the current architecture and business logic, the following areas have been identified for potential future enhancements and integrations:

-   **Guest Client Conversion:** Implement a clear business flow for converting `guestClientInfo` from appointments into full `Client` records, including considerations for deduplication and linking with existing clients.
-   **Advanced Pet Ownership Management:** Define a concept of "primary owner" for pets for communication and billing purposes. Develop robust mechanisms for managing changes in pet ownership, such as transferring pets to new owners or adding/removing co-owners.
-   **Structured Medical History Updates:** Introduce dedicated forms or processes for structured updates to `MedicalHistory` fields like `allergies`, `medications`, and `vaccinations`, rather than relying solely on free-form notes.
-   **Appointment Conflict Resolution:** Implement logic to prevent or resolve scheduling conflicts (e.g., double-booking veterinarians, rooms, or equipment) and provide clear notifications to users when potential conflicts arise during appointment scheduling.
-   **Data Retention Policies:** Establish and implement clear data retention policies for historical data, such as completed/cancelled appointments, consultations, and waiting room entries. This may involve archiving or automated deletion processes for older data.
-   **Granular Permissions:** Once Spring Security is fully configured, replace `@AnonymousAllowed` with fine-grained, role-based authorization (e.g., `@RolesAllowed`, `@PreAuthorize`) for all backend services to enhance security.