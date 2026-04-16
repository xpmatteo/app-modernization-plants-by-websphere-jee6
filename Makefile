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
	@echo "  make restart         - Rebuild and restart the application (Docker Compose)"
	@echo "                         Use this EVERY time you want to test changes manually"
	@echo ""
	@echo "  make quick-restart  - Restart without rebuilding (preserves data)"
	@echo ""
	@echo "  make reset-db       - Reset BOTH databases (plantsdb + plantsdb_test)"
	@echo ""
	@echo "  make mysql-console  - Connect to MySQL console"
	@echo ""
	@echo "  make test           - Run all the tests"
	@echo ""
	@echo "  make logs           - View application logs"
	@echo ""
	@echo "  make stop           - Stop all containers"
	@echo ""
	@echo "  make approve-snapshots - Review and approve updated acceptance test snapshots"
	@echo ""
	@echo "  make clean          - Clean Maven build artifacts and Docker containers/volumes"
	@echo ""
	@echo "Application URL:"
	@echo "  - Legacy App:     http://localhost:9080/promo.jsf"
	@echo ""

.PHONY: restart
restart:
	mvn clean package
	docker-compose down --volumes
	docker-compose up -d --build

# Quick restart without rebuilding (preserves data)
.PHONY: quick-restart
quick-restart:
	docker-compose down
	docker-compose up -d

# Reset BOTH databases (plantsdb and plantsdb_test)
.PHONY: reset-db
reset-db:
	@echo "Resetting both plantsdb and plantsdb_test databases..."
	docker-compose stop mysql
	docker-compose rm -f mysql
	docker volume rm app-modernization-plants-by-websphere-jee6_mysql_data || true
	docker-compose up -d mysql --build
	@echo ""
	@echo "✅ Both databases reset successfully!"

.PHONY: mysql-console
mysql-console:
	mysql -h 127.0.0.1 -P 3306 -u pbwuser -ppbwpass plantsdb

.PHONY: logs
logs:
	docker-compose logs -f

.PHONY: test
test:
	cd acceptance-tests && mvn test

.PHONY: stop
stop:
	docker-compose down

.PHONY: approve-snapshots
approve-snapshots:
	@found=$$(find acceptance-tests/src/test/resources/scenarios -name "*.received.yaml" 2>/dev/null | wc -l | tr -d ' '); \
	if [ "$$found" -eq 0 ]; then \
		echo "No received snapshots to approve."; \
		exit 0; \
	fi; \
	for received in acceptance-tests/src/test/resources/scenarios/*.received.yaml; do \
		approved="$${received/.received/}"; \
		echo ""; \
		echo "=== $$(basename $$approved) ==="; \
		diff "$$approved" "$$received" || true; \
		cp "$$received" "$$approved"; \
		rm "$$received"; \
	done; \
	echo ""; \
	echo "✅ Approved $$found snapshot(s)."

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
