# CLAUDE.md

Plants by WebSphere - Legacy Java EE 6 plant store eCommerce demo with modernized Spring Boot version.

## Quick Start

**Legacy App**: `make restart` → http://localhost:9080/promo.jsf
**Spring Boot**: `cd spring-boot-pbw && ./mvnw spring-boot:run` → http://localhost:8080

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

- `make restart` - Rebuild and restart legacy app with Docker Compose
- `docker-compose logs -f app` - View application logs
- `cd spring-boot-pbw && ./mvnw test` - Run Spring Boot tests