# Plants by WebSphere - Comprehensive Logging Guide

This document explains the comprehensive logging system implemented for application modernization analysis.

## Overview

The application now logs detailed information about every aspect of the system:
- HTTP requests and responses
- Controller (servlet) invocations
- EJB method calls with parameters and return values
- Precise SQL statements with parameter values via JPA/EclipseLink
- Template (JSP) rendering with data context
- Database operation timing

## Logging Components

### 1. RequestLogger Utility (`pbw-lib`)
- **Location**: `pbw-lib/src/main/java/com/ibm/websphere/samples/pbw/utils/RequestLogger.java`
- **Purpose**: Centralized logging utility for all application events
- **Features**:
  - Automatic password masking
  - Object summarization
  - Consistent log format with `[PBW-LOG]` prefix

### 2. RequestLoggingFilter (`pbw-web`)
- **Location**: `pbw-web/src/main/java/com/ibm/websphere/samples/pbw/war/RequestLoggingFilter.java`
- **Purpose**: Intercepts all HTTP requests/responses
- **Features**:
  - Captures request path, method, parameters
  - Tracks response status codes
  - Filters out static resources (CSS, JS, images)

### 3. JPA/EclipseLink SQL Logging
- **Configuration**: Enhanced `persistence.xml` and `server.xml`
- **Purpose**: Logs actual SQL statements executed against the database
- **Features**:
  - Complete SQL statements with parameter values
  - Connection and session information
  - Timing information

## Log Format Examples

### HTTP Request/Response
```
[PBW-LOG] REQUEST: Path=/servlet/AccountServlet, Method=POST, QueryString=null, Params={action=login, userid=test@example.com, passwd=***MASKED***}
[PBW-LOG] RESPONSE: Path=/servlet/AccountServlet, Status=200
```

### Controller Invocation
```
[PBW-LOG] CONTROLLER: AccountServlet.performTask()
[PBW-LOG] CONTROLLER: AdminServlet.performTask()
```

### EJB Method Calls
```
[PBW-LOG] EJB: CustomerMgr.verifyUserAndPassword(test@example.com, ***MASKED***)
[PBW-LOG] EJB_RESULT: CustomerMgr.verifyUserAndPassword returned SUCCESS
[PBW-LOG] EJB: CatalogMgr.getItemsByCategory(1)
[PBW-LOG] EJB_RESULT: CatalogMgr.getItemsByCategory returned Vector[size=12]
```

### Database Operations
```
[PBW-LOG] DB_OP: FIND - Customer with params: [test@example.com] (SQL details in EclipseLink logs)
[PBW-LOG] DB_OP: NAMED_QUERY - getItemsByCategory with params: [1] (SQL details in EclipseLink logs)
[PBW-LOG] DB_OP: PERSIST - Customer with params: [newuser@example.com] (SQL details in EclipseLink logs)
```

### EclipseLink SQL Logs
```
[EL Fine]: sql: SELECT CUSTOMER_ID, ADDR1, ADDR2, ADDR_CITY, ADDR_STATE, ADDR_ZIP, FNAME, LNAME, PASSWD, PHONE FROM CUSTOMER WHERE (CUSTOMER_ID = ?)
	bind => [test@example.com]
[EL Fine]: sql: SELECT INVENTORY_ID, CATEGORY, COST, DESCRIPTION, IMGBYTES, IS_PUBLIC, NAME, NOTES, PKG_INFO, QUANTITY FROM INVENTORY WHERE (CATEGORY = ?)
	bind => [1]
```

### Template Rendering
```
[PBW-LOG] TEMPLATE: forward shopping.jsp
[PBW-LOG] TEMPLATE_DATA: RequestAttrs={invitems=Vector[size=12], results=null}, SessionAttrs={CustomerInfo=Customer@abc123, Category=1}
[PBW-LOG] TEMPLATE: include login.jsp
[PBW-LOG] TEMPLATE_DATA: RequestAttrs={updating="true", results="Login required"}, SessionAttrs={}
```

## Configuration

### Enable Debug Logging
To enable logging, set the debug flag via the application's SetLogging action:
```
http://localhost:9080/pbw-web/servlet/AccountServlet?action=SetLogging&logging=debug
```

Or in development mode (set in `web.xml`):
```xml
<param-name>javax.faces.PROJECT_STAGE</param-name>
<param-value>Development</param-value>
```

### JPA Logging Configuration
The `persistence.xml` includes comprehensive EclipseLink logging:
- SQL statement logging at FINE level
- Parameter value logging
- Session and connection information
- Timestamp inclusion

### WebSphere Liberty Logging
The `server.xml` includes trace specifications for EclipseLink:
```xml
<logging traceSpecification="*=info:eclipselink.sql=fine:eclipselink.logging.sql=fine"/>
```

## Logged Components

### EJB Session Beans
- **CustomerMgr**: User authentication, registration, updates
- **CatalogMgr**: Inventory management, item retrieval
- **BackOrderMgr**: Back order processing
- Additional EJBs: SuppliersBean, MailerBean, etc.

### Web Controllers
- **AdminServlet**: Administration operations
- **AccountServlet**: User account management
- **ImageServlet**: Image serving
- Additional servlets automatically logged via filter

### Database Entities
- Customer operations (login, registration, updates)
- Inventory operations (category queries, item lookups)
- BackOrder operations (creation, updates, deletions)
- All JPA named queries and dynamic queries

## Usage for Modernization

This comprehensive logging provides visibility into:

1. **API Usage Patterns**: Which endpoints are called and how often
2. **Data Access Patterns**: What queries are executed and their frequency
3. **User Flows**: Complete request/response cycles through the application
4. **Performance**: Database query timing and EJB method execution
5. **Data Dependencies**: What data is passed between layers
6. **Template Usage**: Which views are rendered and with what context

This information is invaluable for:
- Identifying microservice boundaries
- Understanding data access patterns for database modernization
- Analyzing user workflows for UI modernization
- Performance optimization opportunities
- Dependency mapping for migration planning