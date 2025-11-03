# ABOUTME: Makefile for Plants By WebSphere development workflow
# ABOUTME: Provides commands for building, testing, and restarting the application

.PHONY: help build test clean restart up down logs populate

help:
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@echo '  build      Build the application WAR'
	@echo '  test       Run tests'
	@echo '  clean      Clean build artifacts'
	@echo '  restart    Rebuild and restart the application (full restart with docker-compose)'
	@echo '  up         Start all services with docker-compose'
	@echo '  down       Stop all services'
	@echo '  logs       Tail application logs'
	@echo '  populate   Populate database with sample data'

build:
	mvn clean package

test:
	mvn test

clean:
	mvn clean

restart:
	@echo "Stopping services..."
	docker-compose down
	@echo "Building application..."
	mvn clean package -DskipTests
	@echo "Rebuilding Docker image..."
	docker-compose build liberty
	@echo "Starting services..."
	docker-compose up -d
	@echo "Waiting for application to start..."
	@sleep 10
	@echo "Application restarted successfully!"
	@echo "Application URL: http://localhost:9080/"
	@echo "To populate database: make populate"

up:
	docker-compose up -d

down:
	docker-compose down

logs:
	docker-compose logs -f liberty

populate:
	@echo "Populating database with sample data..."
	curl -s "http://localhost:9080/servlet/AdminServlet?admintype=populate"
	@echo ""
	@echo "Database populated successfully!"
