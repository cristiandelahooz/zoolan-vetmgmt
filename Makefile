.PHONY: help run build package clean restart fully-restart test format db-up db-down db-clean db-shell db-logs db-migrate db-info db-validate db-clean-migrate create-schema

# ANSI Color Codes
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

# Project Variables
ifeq ($(OS),Windows_NT)
	MAVEN_CMD := .\mvnw.cmd
else
	MAVEN_CMD := ./mvnw
endif
DOCKER_COMPOSE_FILE := compose.yml
PROJECT_NAME := wornux-vet

export FLYWAY_URL := jdbc:postgresql://localhost:5432/petcare_db
export FLYWAY_USER := admin
export FLYWAY_PASSWORD := admin

help:
	@echo ""
	@echo "${GREEN}Wornux Vet Management - Makefile Help${NC}"
	@echo "${GREEN}------------------------------------${NC}"
	@echo ""
	@echo "${YELLOW}General Commands:${NC}"
	@echo "  ${BLUE}make run${NC}        : Runs the Spring Boot application with Hilla frontend in development mode (with hotswap)."
	@echo "  ${BLUE}make build${NC}      : Builds the entire project (frontend and backend)."
	@echo "  ${BLUE}make package${NC}    : Builds the project and packages it into a JAR file."
	@echo "  ${BLUE}make clean${NC}      : Cleans the project build artifacts."
	@echo "  ${BLUE}make restart${NC}    : Cleans project and database, then restarts the application."
	@echo "  ${BLUE}make fully-restart${NC} : Full cleanup (Maven cache, Vaadin bundler, database) and restart."
	@echo "  ${BLUE}make test${NC}       : Runs all unit and integration tests."
	@echo "  ${BLUE}make format${NC}     : Applies code formatting using Spotless (Java) and Biome (TypeScript)."
	@echo ""
	@echo "${YELLOW}Database (PostgreSQL) Commands:${NC}"
	@echo "  ${BLUE}make db-up${NC}      : Starts the PostgreSQL container."
	@echo "  ${BLUE}make db-down${NC}    : Stops and removes the PostgreSQL container."
	@echo "  ${BLUE}make db-clean${NC}   : Cleans PostgreSQL container volumes."
	@echo "  ${BLUE}make db-shell${NC}   : Connects to the PostgreSQL container's shell."
	@echo "  ${BLUE}make db-logs${NC}    : Views the PostgreSQL container logs."
	@echo ""
	@echo "${YELLOW}Flyway Migration Commands:${NC}"
	@echo "  ${BLUE}make db-migrate${NC} : Applies all pending Flyway migrations."
	@echo "  ${BLUE}make db-info${NC}    : Shows the status of all migrations."
	@echo "  ${BLUE}make db-validate${NC} : Validates applied migrations against available ones."
	@echo "  ${BLUE}make db-clean-migrate${NC} : Cleans database and applies all migrations (DESTRUCTIVE)."
	@echo ""

run: db-up
	@echo "${BLUE}Running Spring Boot application with Hilla frontend (hotswap enabled)...${NC}"
	@trap 'echo "${YELLOW}Stopping containers...${NC}"; $(MAKE) db-down' INT; \
	$(MAVEN_CMD) spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true -Dspring.devtools.livereload.enabled=true"

build:
	@echo "${BLUE}Building the project...${NC}"
	$(MAVEN_CMD) clean install -Pproduction

package:
	@echo "${BLUE}Packaging the project into a JAR file...${NC}"
	$(MAVEN_CMD) clean package

clean:
	@echo "${BLUE}Cleaning project build artifacts...${NC}"
	$(MAVEN_CMD) clean

restart: clean db-clean db-up
	@echo "${BLUE}Restarting the application...${NC}"
	$(MAVEN_CMD) spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true -Dspring.devtools.livereload.enabled=true"
	@echo "${BLUE}Application restarted successfully!${NC}"

fully-restart: db-clean
	@echo "${BLUE}Performing full cleanup (Maven build, Vaadin bundler, database)...${NC}"
	$(MAVEN_CMD) clean
	@echo "${YELLOW}Removing Vaadin bundler files...${NC}"
	rm -rf src/main/bundles/
	rm -rf target/
	rm -rf frontend/generated
	@echo "${BLUE}Starting database and application...${NC}"
	$(MAKE) db-up
	$(MAVEN_CMD) spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true -Dspring.devtools.livereload.enabled=true"
	@echo "${GREEN}Full restart completed successfully!${NC}"

test:
	@echo "${BLUE}Running tests...${NC}"
	$(MAVEN_CMD) test -Pintegration-test

format:
	@echo "${BLUE}Applying code formatting...${NC}"
	$(MAVEN_CMD) spotless:apply

db-up:
	@echo "${BLUE}Starting PostgreSQL container...${NC}"
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d

db-down:
	@echo "${BLUE}Stopping and removing PostgreSQL container...${NC}"
	docker-compose -f $(DOCKER_COMPOSE_FILE) down

db-clean:
	@echo "${BLUE}Cleaning PostgreSQL container volumes...${NC}"
	docker-compose -f $(DOCKER_COMPOSE_FILE) down -v

db-restart: db-clean db-up
	@echo "${BLUE}Restarting PostgreSQL container...${NC}"

db-shell:
	@echo "${BLUE}Connecting to PostgreSQL container shell...${NC}"
	docker-compose -f $(DOCKER_COMPOSE_FILE) exec db bash

db-logs:
	@echo "${BLUE}Viewing PostgreSQL container logs...${NC}"
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f db

db-migrate: db-up
	@echo "${BLUE}Applying Flyway migrations...${NC}"
	@sleep 3
	$(MAVEN_CMD) flyway:migrate

db-info: db-up
	@echo "${BLUE}Checking migration status...${NC}"
	@sleep 3
	$(MAVEN_CMD) flyway:info

db-validate: db-up
	@echo "${BLUE}Validating migrations...${NC}"
	@sleep 3
	$(MAVEN_CMD) flyway:validate

db-clean-migrate: db-clean db-up
	@echo "${YELLOW}WARNING: This will clean the database and apply all migrations!${NC}"
	@echo "${BLUE}Waiting for database to be ready...${NC}"
	@sleep 5
	$(MAVEN_CMD) flyway:clean flyway:migrate
	@echo "${GREEN}Database cleaned and migrations applied successfully!${NC}"

create-schema: db-restart
	@echo "${BLUE}Running Spring Boot with create-schema profile to generate database schema...${NC}"
	@sleep 3
	$(MAVEN_CMD) spring-boot:run -Dspring-boot.run.profiles=create-schema
