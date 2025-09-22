# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is Plants by WebSphere, a legacy Java EE 6 sample application that simulates an eCommerce plant store. The application was originally designed for WebSphere Application Server and has been modified for IBM Cloud Transformation Advisor recommendations to run on WebSphere Liberty and containerized environments.

## Architecture

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

## Common Development Commands

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

## Configuration

**WebSphere Liberty configuration:** `wlp/config/server.xml`
- Defines required Java EE 6 features
- Configures MySQL JDBC driver and data sources
- Application deployment configuration

**Database connection:** Uses environment variables:
- `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD` for database connection
- JNDI names: `jdbc/PlantsByWebSphereMySQLDataSource` (JTA) and mail session `mail/PlantsByWebSphere`

## Deployment

**Kubernetes/OpenShift:**
- Helm charts in `chart/pbw-liberty-mariadb/`
- Deployment scripts in `scripts/` directory
- OpenShift-specific configurations in `openshift/` directory

**Build output:** The final EAR file is `target/plants-by-websphere-jee6-mysql.ear`

## Local Development

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

**Database details:**
- Database: `plantsdb`
- User: `pbwuser` / Password: `pbwpass`
- Root password: `password`
- Port: 3306 (exposed for debugging)

## Legacy Considerations

- Uses older Java EE 6 APIs and patterns
- Originally designed for traditional WebSphere Application Server
- Contains transformation advisor modifications for Liberty migration
- MySQL JDBC driver version 5.1.38 is embedded in the container