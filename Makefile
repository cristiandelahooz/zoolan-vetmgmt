\
.PHONY: help run build clean test format db-up db-down db-shell db-logs

# ANSI Color Codes
GREEN = \033[0;32m
YELLOW = \033[0;33m
BLUE = \033[0;34m
NC = \033[0m # No Color

# Project Variables
MAVEN_CMD = ./mvnw
DOCKER_COMPOSE_FILE = compose.yml

help:
	@echo ""
	@echo "${GREEN}Zoolan Vet Management - Makefile Help${NC}"
	@echo "${GREEN}------------------------------------${NC}"
	@echo ""
	@echo "${YELLOW}General Commands:${NC}"
	@echo "  ${BLUE}make run${NC}        : Runs the Spring Boot application with Hilla frontend in development mode (with hotswap)."
	@echo "  ${BLUE}make build${NC}      : Builds the entire project (frontend and backend)."
	@echo "  ${BLUE}make clean${NC}      : Cleans the project build artifacts."
	@echo "  ${BLUE}make test${NC}       : Runs all unit and integration tests."
	@echo "  ${BLUE}make format${NC}     : Applies code formatting using Spotless (Java) and Biome (TypeScript)."
	@echo ""
	@echo "${YELLOW}Database (PostgreSQL) Commands:${NC}"
	@echo "  ${BLUE}make db-up${NC}      : Starts the PostgreSQL container."
	@echo "  ${BLUE}make db-down${NC}    : Stops and removes the PostgreSQL container."
	@echo "  ${BLUE}make db-shell${NC}   : Connects to the PostgreSQL container's shell."
	@echo "  ${BLUE}make db-logs${NC}    : Views the PostgreSQL container logs."
	@echo ""

run:
	@echo "${BLUE}Running Spring Boot application with Hilla frontend (hotswap enabled)...${NC}"
	$(MAVEN_CMD) spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true -Dspring.devtools.livereload.enabled=true"

build:
	@echo "${BLUE}Building the project...${NC}"
	$(MAVEN_CMD) clean install -Pproduction

clean:
	@echo "${BLUE}Cleaning project build artifacts...${NC}"
	$(MAVEN_CMD) clean

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

db-shell:
	@echo "${BLUE}Connecting to PostgreSQL container shell...${NC}"
	docker-compose -f $(DOCKER_COMPOSE_FILE) exec db bash

db-logs:
	@echo "${BLUE}Viewing PostgreSQL container logs...${NC}"
	docker-compose -f $(DOCKER_COMPOSE_FILE) logs -f db
