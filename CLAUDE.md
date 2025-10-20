# CLAUDE.md

Plants by WebSphere - Legacy Java EE 6 plant store eCommerce demo with modernized Spring Boot version.

## Quick Start

```bash
make          # Show all available commands
make restart  # Rebuild and start both applications
make test     # Run all tests
```

**Application URLs:**
- **Legacy App**: http://localhost:9080/promo.jsf
- **Spring Boot**: http://localhost:8080

## ⚠️ CRITICAL: Manual Testing

**ALWAYS use `make restart` for testing the applications manually.**

This command:
- Rebuilds BOTH legacy (WebSphere Liberty) and Spring Boot applications
- Starts them via Docker Compose with shared MariaDB database
- Ensures you're testing the correct, consistent environment
- Must be run from the project root directory

**NEVER run `./mvnw spring-boot:run` for manual testing** - you won't know which version you're hitting (Docker vs local).

## Key Directories

- `pbw-lib/`, `pbw-web/`, `pbw-ear/` - Legacy Java EE modules (JSF + EJB + JPA)
- `spring-boot-pbw/` - Modern Spring Boot version (REST + JDBC + Mustache)
- `docs/` - Analysis guides, logging documentation, user journeys
- `wlp/config/server.xml` - WebSphere Liberty configuration

## Database

- **Connection**: `plantsdb` / `pbwuser` / `pbwpass` @ localhost:3306
- **Init script**: `docker/mariadb/init.sql` (auto-loaded via Docker Compose)
- **Console**: `make mysql-console` (direct MySQL CLI access)

**How applications connect:**
- **Legacy**: JNDI `jdbc/PlantsByWebSphereMySQLDataSource`
- **Spring Boot**: Direct JDBC via Spring Boot configuration

## Architecture

**Legacy (Java EE 6)**:
- WebSphere Liberty runtime with JSP 2.3, EJB Lite 3.2, JSF 2.2, JPA 2.1
- Multi-module Maven: pbw-lib (utils) → pbw-web (WAR) → pbw-ear (EAR)
- Entry: JSF backing beans → EJB session beans → JPA entities

**Modern (Spring Boot)**:
- Spring Boot 3.5.6 with Java 21, JDBC templates, Mustache views
- Single module with REST controllers and direct database access
- Package-by-feature structure: `catalog/`, `promo/`, `health/`, `domain/`
- Entry: REST controllers → repositories → domain objects

## Available Commands

Run `make` (without arguments) to see all available commands with descriptions.

**Most used:**
```bash
make restart        # Rebuild and restart both applications
make test          # Run all tests
make logs          # View logs from both applications
make clean         # Clean Maven artifacts and Docker resources
```

**Other commands:**
```bash
make quick-restart  # Restart without rebuilding
make reset-db      # Reset database only
make mysql-console # Connect to MySQL CLI
make stop          # Stop all containers
```

## Development Workflow

1. **Make code changes** in either legacy or Spring Boot modules
2. **Run tests**: `make test` (or `cd spring-boot-pbw && ./mvnw test` for Spring Boot only)
3. **Manual testing**: `make restart` to rebuild and test in Docker
4. **View logs**: `make logs` to debug issues
5. **Commit** when tests pass and manual testing confirms the changes work

## Spring Boot Package Structure

The Spring Boot application uses **package-by-feature** organization:

```
it.xpug.pbw/
├── catalog/          # Product browsing, images
│   ├── ProductController.java
│   ├── ProductRepository.java
│   ├── ImageController.java
│   └── ImageRepository.java
├── promo/            # Promotional landing page
│   └── PromoController.java
├── health/           # Health checks
│   └── HealthController.java
└── domain/           # Shared domain models
    └── Product.java
```
- use AssertJ for assertions, unless using MockMvc