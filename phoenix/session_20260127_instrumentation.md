# Session Journal: Phoenix Instrumentation Implementation

**Date:** 2026-01-27
**Branch:** phoenix
**Commit:** 586a337

## Objective

Build application-level instrumentation to capture execution traces at three layers (HTTP, Java Entrypoint, JDBC) for use in application modernization. Traces serve as durable artifacts that a reconstructed app must be able to reproduce.

## What Was Built

### Components

1. **TraceContext** - ThreadLocal storage for per-request state (traceId, sequence counter)
2. **ScenarioState** - Global state for active scenario management (name, description, start time)
3. **TraceWriter** - JSONL file writer with thread-safe writing and sensitive field redaction
4. **ScenarioController** - REST servlet for start/stop/status operations
5. **HttpTraceFilter** - Servlet filter capturing request/response + session state
6. **EntrypointTraceInterceptor** - CDI interceptor for semantic action names
7. **@Traced annotation** - Marker for entrypoint methods
8. **JPA/JDBC tracing** - EclipseLink SessionCustomizer + JDBC wrapper classes

### Trace Format (JSONL)

```jsonl
{"_meta": {"scenario": "name", "description": "..."}}
{"traceId": "abc123", "seq": 1, "layer": "http", "type": "request", "method": "GET", "url": "/...", "params": {...}, "session": {...}}
{"traceId": "abc123", "seq": 2, "layer": "http", "type": "response", "status": 200, "session": {...}}
```

## Learnings & Gotchas

### Liberty Servlet URL Mapping

**Problem:** `@WebServlet("/trace/*")` annotation wasn't being picked up - requests returned 404 "File not found".

**Solution:** Had to register the servlet explicitly in `web.xml` AND use the `/servlet/` prefix pattern that works with other servlets in the app:
```xml
<servlet-mapping>
    <servlet-name>ScenarioController</servlet-name>
    <url-pattern>/servlet/trace/*</url-pattern>
</servlet-mapping>
```

**Lesson:** Don't rely solely on servlet annotations in Liberty with JSF apps. The default servlet or JSF may catch requests before annotated servlets. Use explicit web.xml mappings when in doubt.

### Sensitive Field Redaction

**Problem:** Field names like `loginForm:passwd` weren't being redacted because we checked for exact match on "passwd".

**Solution:** Changed `isSensitiveField()` to check if the field name *contains* any sensitive keyword:
```java
for (String sensitive : SENSITIVE_FIELDS) {
    if (lowerName.contains(sensitive)) {
        return true;
    }
}
```

**Lesson:** Form frameworks often prefix field names. Redaction logic must handle compound names.

### EclipseLink Dependency

**Problem:** JpaTraceSessionCustomizer and TracingSessionLog required EclipseLink classes that aren't in the Jakarta EE API.

**Solution:** Added EclipseLink as a `provided` dependency in pom.xml:
```xml
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>eclipselink</artifactId>
    <version>4.0.2</version>
    <scope>provided</scope>
</dependency>
```

### CDI Interceptor Registration

**Lesson:** CDI interceptors must be registered in `beans.xml` even with `bean-discovery-mode="all"`:
```xml
<interceptors>
    <class>com.ibm.websphere.samples.pbw.trace.EntrypointTraceInterceptor</class>
</interceptors>
```

### Docker Image Rebuild Required

**Lesson:** When testing in Docker, changes to the WAR require:
1. `mvn clean package`
2. `docker-compose build --no-cache liberty` (just `restart` won't pick up WAR changes)
3. `docker-compose up -d`

## Testing the Instrumentation

```bash
# Start a scenario
curl -X POST "http://localhost:9080/servlet/trace/start?scenario=test&description=Test"

# Do some actions...
curl http://localhost:9080/promo.jsf

# Stop scenario
curl -X POST http://localhost:9080/servlet/trace/stop

# Check trace file (inside container)
docker exec pbw-liberty cat /opt/ibm/wlp/output/defaultServer/phoenix/traces/test.jsonl
```

## Known Limitations

1. **Entrypoint tracing** only fires when JSF invokes `@Traced` methods via action bindings. Direct method calls within beans aren't intercepted.

2. **JDBC result capture** via EclipseLink logging is limited - we get SQL and params but not full result sets. The JDBC wrapper classes (TracingResultSet, etc.) are ready for direct JDBC usage but JPA uses its own connection management.

3. **Trace file location** defaults to `./phoenix/traces/` relative to server working directory. In Docker, this is `/opt/ibm/wlp/output/defaultServer/phoenix/traces/`.

## Next Steps

1. Add volume mount in docker-compose.yml to persist traces outside container
2. Test entrypoint tracing with actual JSF form submissions
3. Implement trace validation (comparing traces from original vs reconstructed app)
4. Build Playwright test scenarios that exercise key user journeys while tracing

## Files Changed

- `pbw-web/pom.xml` - Added EclipseLink dependency
- `pbw-web/src/main/java/com/ibm/websphere/samples/pbw/trace/*` - 15 new classes
- `pbw-web/src/main/java/com/ibm/websphere/samples/pbw/war/AccountBean.java` - Added @Traced
- `pbw-web/src/main/java/com/ibm/websphere/samples/pbw/war/ShoppingBean.java` - Added @Traced
- `pbw-web/src/main/resources/META-INF/persistence.xml` - EclipseLink SQL logging config
- `pbw-web/src/main/webapp/WEB-INF/beans.xml` - CDI interceptor registration
- `pbw-web/src/main/webapp/WEB-INF/web.xml` - ScenarioController servlet mapping
