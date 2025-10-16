# CLAUDE.md

Plants by WebSphere - Legacy Java EE 6 plant store eCommerce demo with modernized Spring Boot version.

## Quick Start

**⚠️ CRITICAL: Use `make restart` to run BOTH applications together via Docker Compose**

- **Legacy App**: http://localhost:9080/promo.jsf
- **Spring Boot**: http://localhost:8080

## Running the Applications

**`make restart` is the REQUIRED command for testing the applications manually.**

- Rebuilds and restarts BOTH legacy (WebSphere Liberty) and Spring Boot apps via Docker Compose
- Run from project root directory
- Use this EVERY time you want to test changes manually in either application
- Both apps share the same MariaDB database container

## Key Directories

- `pbw-lib/`, `pbw-web/`, `pbw-ear/` - Legacy Java EE modules (JSF + EJB + JPA)
- `spring-boot-pbw/` - Modern Spring Boot version (REST + JDBC + Mustache)
- `docs/` - Analysis guides, logging documentation, user journeys
- `wlp/config/server.xml` - WebSphere Liberty configuration

## Database

- **Connection**: `plantsdb` / `pbwuser` / `pbwpass` on localhost:3306
- **Init script**: `docker/mariadb/init.sql` (auto-loaded via Docker Compose)
- **Legacy**: JNDI `jdbc/PlantsByWebSphereMySQLDataSource`
- **Spring Boot**: Direct JDBC connection

## Architecture Notes

**Legacy (Java EE 6)**:
- WebSphere Liberty runtime with JSP 2.3, EJB Lite 3.2, JSF 2.2, JPA 2.1
- Multi-module Maven: pbw-lib (utils) → pbw-web (WAR) → pbw-ear (EAR)
- Entry: JSF backing beans → EJB session beans → JPA entities

**Modern (Spring Boot)**:
- Spring Boot 3.5.6 with Java 21, JDBC templates, Mustache views
- Single module with REST controllers and direct database access
- Entry: REST controllers → JDBC services → domain objects

## Essential Commands

- **`make restart`** - Rebuild and restart BOTH applications via Docker Compose (run from project root)
- `docker-compose logs -f app` - View legacy application logs
- `docker-compose logs -f spring-boot-pbw` - View Spring Boot application logs
- `docker-compose down` - Stop all containers
- `cd spring-boot-pbw && ./mvnw test` - Run Spring Boot tests (for development/testing only)