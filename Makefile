.DEFAULT_GOAL := help

.PHONY: help
help:
	@echo "╔════════════════════════════════════════════════════════════════════╗"
	@echo "║         Plants by WebSphere - Legacy Modernization Demo          ║"
	@echo "╚════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "⚠️  MANUAL TESTING: Always use 'make restart' for testing changes!"
	@echo ""
	@echo "Available commands:"
	@echo ""
	@echo "  make restart         - Rebuild and restart BOTH applications (Docker Compose)"
	@echo "                         Use this EVERY time you want to test changes manually"
	@echo ""
	@echo "  make test           - Run all tests (legacy + Spring Boot)"
	@echo ""
	@echo "  make quick-restart  - Restart without rebuilding (preserves data)"
	@echo ""
	@echo "  make reset-db       - Reset database only (keeps app running)"
	@echo ""
	@echo "  make mysql-console  - Connect to MySQL console"
	@echo ""
	@echo "  make logs           - View logs for both applications"
	@echo ""
	@echo "  make stop           - Stop all containers"
	@echo ""
	@echo "  make clean          - Clean Maven build artifacts and Docker containers/volumes"
	@echo ""
	@echo "Application URLs:"
	@echo "  - Legacy App:     http://localhost:9080/promo.jsf"
	@echo "  - Spring Boot:    http://localhost:8080"
	@echo ""

.PHONY: restart
restart:
	mvn clean package
	docker-compose down --volumes
	docker-compose up -d --build

.PHONY: test
test:
	@echo "Running Spring Boot tests..."
	cd spring-boot-pbw && ./mvnw test
	@echo ""
	@echo "✅ All tests passed!"

# Quick restart without rebuilding (preserves data)
.PHONY: quick-restart
quick-restart:
	docker-compose down
	docker-compose up -d

# Reset database only (keeps app running)
.PHONY: reset-db
reset-db:
	docker-compose stop mysql
	docker-compose rm -f mysql
	docker volume rm app-modernization-plants-by-websphere-jee6_mysql_data || true
	docker-compose up -d mysql --build

.PHONY: mysql-console
mysql-console:
	mysql -h 127.0.0.1 -P 3306 -u pbwuser -ppbwpass plantsdb

.PHONY: logs
logs:
	docker-compose logs -f

.PHONY: stop
stop:
	docker-compose down

.PHONY: clean
clean:
	@echo "Cleaning Maven build artifacts..."
	mvn clean
	@echo ""
	@echo "Stopping Docker containers and removing volumes..."
	docker-compose down --volumes
	docker system prune -f
	@echo ""
	@echo "✅ Clean complete!"
