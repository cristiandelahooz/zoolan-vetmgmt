# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Zoolan Vetmgmt is a comprehensive veterinary management system built with Spring Boot 3.5.0, Vaadin 24.8.3, and React.
It manages veterinary appointments, clients, pets, medical histories, invoices, and more.

## Architecture

### Technology Stack

- **Backend**: Spring Boot 3.5.0 (Java 21), Spring Security, JPA/Hibernate 6.6.4, PostgreSQL
- **Frontend**: Vaadin 24.8.3 with React 18.3.1, TypeScript, Vite 6.3.5
- **Database**: PostgreSQL with Flyway migrations
- **UI Framework**: Vaadin with LineAwesome icons and Lumo design system
- **Build Tools**: Maven (backend), Vite (frontend)
- **Code Quality**: Biome (TypeScript), Spotless (Java), MapStruct for mapping

### Key Application Structure

- `src/main/java/com/wornux/` - Java backend code
    - `data/entity/` - JPA entities (Client, Pet, Appointment, etc.)
    - `data/repository/` - Spring Data repositories
    - `services/` - Business logic layer with interfaces and implementations
    - `views/` - Vaadin UI views and forms
    - `security/` - Spring Security and Vaadin Navigation Access Control
    - `mapper/` - MapStruct entity-DTO mappers
- `src/main/frontend/` - TypeScript/React frontend code
- `src/main/resources/` - Configuration, database migrations, reports

## Development Commands

### Primary Development Commands

```bash
# Start development server with database
make run                    # Starts PostgreSQL and runs app in dev mode with hot reload

# Build and packaging
make build                  # Build for production
make package               # Package into JAR
make clean                 # Clean build artifacts

# Testing and quality
make test                  # Run tests with integration profile
make format               # Format code (Spotless for Java, Biome for TypeScript)

# Database management
make db-up                # Start PostgreSQL container
make db-down              # Stop PostgreSQL container
make db-clean             # Clean database volumes
make db-shell             # Connect to database shell
```

### Maven Commands

```bash
./mvnw spring-boot:run     # Run in development mode
./mvnw clean install      # Full build
./mvnw test               # Run tests
./mvnw spotless:apply     # Format Java code
```

### TypeScript/Frontend

- Managed by Vaadin with Vite
- Uses Biome for formatting and linting
- Configuration in `biome.json`, `tsconfig.json`, `vite.config.ts`

## Security Architecture

Uses Vaadin's Navigation Access Control with Spring Security:

### Role System

- **System Roles**: `SYSTEM_ADMIN`, `MANAGER`, `USER`, `GUEST` (with ROLE\_ prefix)
- **Employee Roles**: Maps business roles to system roles
- **View Security**: Uses `@RolesAllowed`, `@PermitAll`, `@AnonymousAllowed`

### Key Security Files

- `security/SecurityConfig.java` - Spring Security configuration
- `security/config/NavigationAccessConfig.java` - Navigation access setup
- `security/navigation/` - Custom access checkers

## Database

### Configuration

- PostgreSQL 16 via Docker Compose
- Default credentials: admin/admin, database: petcare_db
- Port: 5432

### Schema

- The database schema is defined in the @src/main/resources/db/migration/prod/V0.0.1\_\_init.sql file. Reference it
  anytime you need to understand the structure of data stored in the database.

### Migrations

- Uses Flyway with locations: `classpath:db/migration/prod`, `classpath:db/migration/dev`
- Baseline enabled for existing databases

### Auditing

- Hibernate Envers enabled for audit logging
- Audit tables use `_log` suffix
- Custom `AuditorAwareImpl` for tracking changes

## UI Development Guidelines

### Design System

- **Icons**: LineAwesome (use `LineAwesomeIcon.PAW_SOLID.create()`)
- **Styling**: Lumo utility classes (`LumoUtility.Padding.MEDIUM`)
- **Theme**: Custom theme `zoolan-vetmgmt` in `src/main/frontend/themes/`

### Common Patterns

```java
// Navigation items with icons
Icon icon = LineAwesomeIcon.STETHOSCOPE_SOLID.create();
icon.addClassNames(LumoUtility.IconSize.MEDIUM);

// Utility class styling
component.addClassNames(
    LumoUtility.Padding.MEDIUM,
    LumoUtility.BorderRadius.MEDIUM
);
```

## Data Layer Patterns

### Entity Design

- Extends `AbstractEntity` for common fields (ID, timestamps)
- Uses `@Audited` for change tracking
- Enums for status fields (AppointmentStatus, PaymentMethod, etc.)

### Service Layer

- Interface/implementation pattern in `services/interfaces/` and `services/implementations/`
- Uses MapStruct for DTO mapping
- Transactional offering methods

### Repository Layer

- Extends `AbstractRepository<T>` for common operations
- Spring Data JPA repositories in `data/repository/`

## Testing

### Test Structure

- Unit tests: Standard JUnit 5 with Spring Boot Test
- Integration tests: Uses Testcontainers for PostgreSQL
- Architecture tests: ArchUnit for enforcing patterns

### Running Tests

```bash
make test                          # All tests with integration profile
./mvnw test                       # Unit tests only
./mvnw test -Pintegration-test    # Include integration tests
```

## Reporting

### JasperReports Integration

- Reports in `src/main/resources/reports/`
- Invoice generation with `JasperReportFactory`
- PDF output support

## Code Quality

### Formatting Standards

- **Java**: Google Java Format via Spotless
- **TypeScript**: Biome with 2-space indentation, single quotes
- **Line width**: 120 characters

### Code Organization

- Package-by-feature structure
- Consistent naming conventions
- MapStruct for object mapping
- Lombok for boilerplate reduction

## Environment Configuration

### Profiles

- **dev**: Development with hot reload, detailed SQL logging
- **production**: Optimized Vaadin bundle, production database

### Key Properties

```yaml
# Database connection
spring.datasource.url: jdbc:postgresql://localhost:5432/petcare_db

# Vaadin configuration
vaadin.launch-browser: true
vaadin.allowed-packages: com.vaadin,org.vaadin,com.flowingcode,com.wornux
```

## Common Development Tasks

### Adding New Entity

1. Create entity in `data/entity/` extending `AbstractEntity`
2. Add repository in `data/repository/`
3. Create offering interface and implementation
4. Add MapStruct mapper with DTOs
5. Create Vaadin view and form components

### Database Changes

1. Create new migration in `src/main/resources/db/migration/prod/`
2. Test with `make db-clean db-up`
3. Update entities and repositories as needed

### UI Components

1. Use LineAwesome icons for consistency
2. Apply Lumo utility classes for styling
3. Follow existing form and grid patterns
4. Ensure responsive design with Vaadin components

- guarda las reglas nagivation acces control, y una tabla de acceso por role, a cuales rutas tiene acceso
- after any edits in the code, use jetbrains mcp to get the diagnostics of the file edited to see if there is any
  problem