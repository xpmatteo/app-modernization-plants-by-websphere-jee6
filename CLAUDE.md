# CLAUDE.md

Plants by WebSphere - Legacy Java EE 6 plant store eCommerce demo.

## Quick Start

```bash
make          # Show all available commands
make restart  # Rebuild and start the application
```

**Application URL:**
- **Legacy App**: http://localhost:9080/promo.jsf

## ⚠️ CRITICAL: Manual Testing

**ALWAYS use `make restart` for testing the application manually.**

This command:
- Rebuilds the legacy (WebSphere Liberty) application
- Starts it via Docker Compose with MariaDB database
- Ensures you're testing the correct, consistent environment
- Must be run from the project root directory

## Key Directories

- `pbw-lib/`, `pbw-web/`, `pbw-ear/` - Legacy Java EE modules (JSF + EJB + JPA)
- `docs/` - Analysis guides, logging documentation, user journeys
- `wlp/config/server.xml` - WebSphere Liberty configuration

## Database

- **Connection**: `plantsdb` / `pbwuser` / `pbwpass` @ localhost:3306
- **Init script**: `docker/mariadb/init.sql` (auto-loaded via Docker Compose)
- **Console**: `make mysql-console` (direct MySQL CLI access)

**How the application connects:**
- **Legacy**: JNDI `jdbc/PlantsByWebSphereMySQLDataSource`

## Architecture

**Legacy (Java EE 6)**:
- WebSphere Liberty runtime with JSP 2.3, EJB Lite 3.2, JSF 2.2, JPA 2.1
- Multi-module Maven: pbw-lib (utils) → pbw-web (WAR) → pbw-ear (EAR)
- Entry: JSF backing beans → EJB session beans → JPA entities

## Available Commands

Run `make` (without arguments) to see all available commands with descriptions.

**Most used:**
```bash
make restart        # Rebuild and restart the application
make logs          # View application logs
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

1. **Make code changes** in the legacy modules
2. **Manual testing**: `make restart` to rebuild and test in Docker
3. **View logs**: `make logs` to debug issues
4. **Commit** when manual testing confirms the changes work
