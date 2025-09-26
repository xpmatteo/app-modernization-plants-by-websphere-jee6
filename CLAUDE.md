# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is Plants by WebSphere, a legacy Java EE 6 sample application that simulates an eCommerce plant store. The application was originally designed for WebSphere Application Server and has been modified for IBM Cloud Transformation Advisor recommendations to run on WebSphere Liberty and containerized environments.

**Modernization Status:** This repository now contains both the original legacy application and a new Spring Boot refactored version (`spring-boot-pbw/`) that demonstrates modern Java development practices while maintaining the same core functionality.

## Architecture

### Legacy Java EE 6 Application

**Multi-module Maven structure:**
- `pbw-lib/` - Utility JAR library containing shared utilities
- `pbw-web/` - WAR module containing the web application (servlets, JSPs, EJBs, JPA entities)
- `pbw-ear/` - EAR assembly that packages the lib and web modules together

**Technology Stack:**
- Java EE 6 (using Java 1.6 source, targeting 1.8)
- WebSphere Liberty runtime with features: JSP 2.3, EJB Lite 3.2, Servlet 3.1, JSF 2.2, JPA 2.1, CDI 1.2
- MySQL 5.1.38 database (with commented MariaDB alternative)
- Maven for build management

**Key Components:**
- JPA entities in `pbw-web/src/main/java/com/ibm/websphere/samples/pbw/jpa/`
- EJB session beans in `pbw-web/src/main/java/com/ibm/websphere/samples/pbw/ejb/`
- Web layer (servlets/JSF beans) in `pbw-web/src/main/java/com/ibm/websphere/samples/pbw/war/`

### Spring Boot Refactored Application

**Single Maven module structure:**
- `spring-boot-pbw/` - Spring Boot application containing the modernized version

**Technology Stack:**
- Spring Boot 3.5.6 (Java 21)
- Spring JDBC for data access
- Mustache templates for web layer
- MySQL connector (modern version)
- Maven wrapper for build management

**Key Components:**
- Main application class in `src/main/java/it/xpug/pbw/PbwApplication.java`
- REST controllers in `src/main/java/it/xpug/pbw/controller/`
  - `HealthController.java` - Health check endpoints
  - `PromoController.java` - Promotion management
- Mustache templates in `src/main/resources/templates/`
- Static resources in `src/main/resources/static/`

## Common Development Commands

### Legacy Java EE 6 Application

**Build the entire application:**
```bash
mvn clean package
```

**Build specific modules:**
```bash
cd pbw-lib && mvn clean package    # Build utility library
cd pbw-web && mvn clean package    # Build web application
cd pbw-ear && mvn clean package    # Build EAR assembly
```

**Run locally with Docker Compose (Recommended):**
```bash
docker-compose up -d        # Start application and database
docker-compose logs -f      # View logs
docker-compose down         # Stop everything
```

**Manual Docker build:**
```bash
docker build -t plants-by-websphere .
```

### Spring Boot Refactored Application

**Build and run with Maven wrapper:**
```bash
cd spring-boot-pbw
./mvnw clean package          # Build the application
./mvnw spring-boot:run        # Run the application locally
```

**Build and run with Docker:**
```bash
cd spring-boot-pbw
docker build -t pbw-spring-boot .                    # Build Docker image
docker run -p 8080:8080 pbw-spring-boot              # Run container
```

**Run tests:**
```bash
cd spring-boot-pbw
./mvnw test                   # Run unit tests
```

## Configuration

### Legacy Java EE 6 Application

**WebSphere Liberty configuration:** `wlp/config/server.xml`
- Defines required Java EE 6 features
- Configures MySQL JDBC driver and data sources
- Application deployment configuration

**Database connection:** Uses environment variables:
- `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD` for database connection
- JNDI names: `jdbc/PlantsByWebSphereMySQLDataSource` (JTA) and mail session `mail/PlantsByWebSphere`

### Spring Boot Refactored Application

**Spring Boot configuration:** `spring-boot-pbw/src/main/resources/application.properties`
- Database connection configuration (same database as legacy app)
- Application name and Spring Boot settings
- Uses direct MySQL JDBC connection (no JNDI)

**Database connection properties:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/plantsdb
spring.datasource.username=pbwuser
spring.datasource.password=pbwpass
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## Deployment

**Kubernetes/OpenShift:**
- Helm charts in `chart/pbw-liberty-mariadb/`
- Deployment scripts in `scripts/` directory
- OpenShift-specific configurations in `openshift/` directory

**Build output:** The final EAR file is `target/plants-by-websphere-jee6-mysql.ear`

## Local Development

### Legacy Java EE 6 Application

**Running the application locally:**
1. Build the application: `mvn clean package`
2. Start with Docker Compose: `docker-compose up -d`
3. Access the application: http://localhost:9080/pbw-web
4. Access HTTPS: https://localhost:9443/pbw-web

**Docker Compose includes:**
- MySQL 5.7 database with automatic schema initialization from `docker/mariadb/init.sql`
- WebSphere Liberty application server
- Health checks and proper service dependencies
- Persistent data storage for MySQL

### Spring Boot Refactored Application

**Running the application locally:**

**Option 1: With Maven wrapper (requires MySQL running)**
```bash
cd spring-boot-pbw
./mvnw spring-boot:run
```
Access the application: http://localhost:8080

**Option 2: With Docker (self-contained)**
```bash
cd spring-boot-pbw
docker build -t pbw-spring-boot .
docker run -p 8080:8080 pbw-spring-boot
```

**Database details (shared with legacy app):**
- Database: `plantsdb`
- User: `pbwuser` / Password: `pbwpass`
- Root password: `password`
- Port: 3306 (exposed for debugging)

## Legacy Considerations

- Uses older Java EE 6 APIs and patterns
- Originally designed for traditional WebSphere Application Server
- Contains transformation advisor modifications for Liberty migration
- MySQL JDBC driver version 5.1.38 is embedded in the container
- the application landing page is at http://localhost:9080/promo.jsf
- ALWAYS use `make restart` to restart the application