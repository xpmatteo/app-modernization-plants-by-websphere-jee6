# Phoenix Architecture

**The application that rises from its own ashes.**

## Philosophy

Phoenix is an approach to legacy application modernization based on a simple premise: if you can fully specify *what* an application does, you can rebuild *how* it does it in any technology.

The goal is to create specifications complete enough that:

1. You can throw away all source code
2. Keep: database schema, specs, traces, and durable evaluations
3. An AI agent (or human team) can reconstruct the application in any technology

```
┌────────────────────────────────────────────────┐
│            DURABLE ARTIFACTS                   │
│                                                │
│   Schema    Specs    Traces    Evaluations     │
│      │        │         │           │          │
│      └────────┴────┬────┴───────────┘          │
│                    │                           │
│                    ▼                           │
│           ┌────────────────┐                   │
│           │ Reconstruction │                   │
│           └────────────────┘                   │
│                   │                            │
│        ┌──────────┼──────────┐                 │
│        ▼          ▼          ▼                 │
│   Spring Boot    Go       Next.js              │
│     (Java)     (Go)    (TypeScript)            │
└────────────────────────────────────────────────┘
```

## Core Principle

**Separate the WHAT from the HOW.**

- **WHAT**: The application's behavior, data model, user journeys, business rules
- **HOW**: The technology choices, framework patterns, implementation details

The WHAT is durable. The HOW is ephemeral.

## Durable vs. Ephemeral Artifacts

| Durable (Keep) | Ephemeral (Discard) |
|----------------|---------------------|
| Database schema | Source code |
| UI screen templates | Framework configuration |
| Reference screenshots | Unit tests |
| Screen graph & action specs | Integration tests (white-box) |
| Execution traces | Internal class structure |
| E2E tests (black-box) | CSS/styling details |
| HTTP API contracts | |

**Why are unit tests ephemeral?** Because they test implementation. When you rewrite the app in Go, there are no Java classes to unit test. But the E2E tests that verify "user can log in and see their orders" remain valid regardless of implementation language.

## The Three Pillars

### 1. Specifications

Complete documentation of application behavior:

- **Screen Graph**: Every screen/page, the navigation paths between them
- **Screen Templates**: The actual HTML that users see (see below)
- **Action Specs**: For every user action:
  - What input it receives
  - What data it reads from the database
  - What the possible outcomes are
  - What data it writes to the database
- **Business Rules**: Invariants, validation rules, calculations

Specifications come from static analysis of templates, code, and configuration.

### UI as a Durable Artifact

The user interface is part of "what" the application does, not "how". The specification must include the actual HTML screens users see, with variable parts appropriately marked.

**Example: Order Confirmation Screen**

```html
<div class="order-confirmation">
  <h1>Thank you for your order!</h1>
  <p>Order #{{ORDER_ID}} placed on {{DATE:today}}</p>

  <h2>Items Ordered</h2>
  <table>
    {{#each ORDER_ITEMS}}
    <tr>
      <td>{{ITEM_NAME}}</td>
      <td>{{QUANTITY}}</td>
      <td>{{PRICE:currency}}</td>
    </tr>
    {{/each}}
  </table>

  <p class="total">Total: {{ORDER_TOTAL:currency}}</p>

  <p>A confirmation email has been sent to {{CUSTOMER_EMAIL}}</p>
</div>
```

**Variable Markers:**

| Marker | Meaning |
|--------|---------|
| `{{FIELD_NAME}}` | Dynamic value from data |
| `{{FIELD:format}}` | Dynamic value with format hint |
| `{{DATE:today}}` | Current date (varies by execution) |
| `{{DATE:order_date}}` | Date from data |
| `{{#each COLLECTION}}...{{/each}}` | Repeated section |
| `{{#if CONDITION}}...{{/if}}` | Conditional section |

**Why HTML templates are durable:**

1. They define the contract between application and user
2. A reconstructed app must render equivalent HTML
3. CSS styling may change, but structure and content must match
4. E2E tests can validate against these templates

**Reference Screenshots:**

Every screen includes a reference screenshot showing the expected visual appearance. Screenshots are captured during trace scenarios and serve as:

1. **Visual documentation**: Humans can quickly understand what each screen looks like
2. **Regression baseline**: Visual diff testing can detect unintended UI changes
3. **Reconstruction guidance**: AI agents can use screenshots to understand layout intent

Screenshots are stored alongside templates:

```
phoenix/specs/templates/
├── login.html
├── login.png              # Reference screenshot
├── catalog.html
├── catalog.png
└── ...
```

**Note**: Screenshots capture a moment in time with specific data. Variable regions (dates, user-specific content, dynamic lists) should be annotated or accepted as variable in visual comparisons.

**Capturing screen templates:**

During trace capture, the HTTP response layer records the actual HTML rendered. These are then abstracted into templates by:

1. Identifying dynamic values (from trace data)
2. Replacing them with variable markers
3. Annotating format hints where relevant
4. Documenting which data source populates each variable
5. Capturing a reference screenshot for each unique screen state

### 2. Execution Traces

Runtime behavior captured at three layers:

```
HTTP ──────────► Entrypoint ──────────► Database
(request/       (semantic action:      (actual SQL
 response,       "performLogin",        queries and
 session)        "addToCart")           results)
```

Traces serve two purposes:

1. **Discovery**: Fill gaps that static analysis missed
2. **Validation**: Reconstructed app must produce equivalent traces

### 3. Evaluations

Tests that validate behavior without depending on implementation:

- **E2E browser tests**: Playwright tests that interact via UI
- **API contract tests**: Verify HTTP request/response shapes
- **Database state assertions**: Verify data mutations
- **Trace equivalence tests**: Compare traces between original and reconstructed app

## Trace Design

Traces are captured as JSONL files, one per scenario:

```jsonl
{"_meta": {"scenario": "login-success", "description": "User logs in with valid credentials"}}
{"traceId": "t1", "seq": 1, "layer": "http", "type": "request", "method": "POST", "url": "/login", "params": {"email": "user@example.com", "password": "[REDACTED]"}, "session": {}}
{"traceId": "t1", "seq": 2, "layer": "entrypoint", "class": "AccountBean", "method": "performLogin"}
{"traceId": "t1", "seq": 3, "layer": "jdbc", "type": "query", "sql": "SELECT * FROM CUSTOMER WHERE CUSTOMERID = ?", "params": ["user@example.com"], "results": [{"CUSTOMERID": "user@example.com", "FIRSTNAME": "John", ...}]}
{"traceId": "t1", "seq": 4, "layer": "http", "type": "response", "status": 302, "redirect": "/home", "session": {"customerId": "user@example.com"}}
```

Key design choices:

- **Sensitive data is redacted**: Passwords, credit cards become `[REDACTED]`
- **Binary data is hashed**: BLOBs stored as `{"_binary": true, "hash": "sha256:...", "size": 1024}`
- **Full query results captured**: Not just row counts, but actual data (with redaction)
- **Session state tracked**: Before and after each request

## What Makes a Good Specification?

A specification is complete when:

1. **Coverage**: Every user journey is documented
2. **Precision**: Edge cases and error conditions are captured
3. **Independence**: No references to implementation details
4. **Verifiability**: Can be validated against the running application

## The Reconstruction Test

The ultimate validation of Phoenix specifications:

1. Delete all source code
2. Give an AI agent (or team) only: schema, specs, traces, evaluations
3. They build a new implementation (potentially in different technology)
4. The new implementation:
   - Passes all E2E evaluations
   - Produces equivalent traces
   - Renders HTML matching the screen templates (structure and content, not styling)

If this succeeds, the specifications were complete.

**UI Validation**: The reconstructed application's HTML output is compared against screen templates. Dynamic values are extracted and verified against trace data. Structure must match; styling may differ.

**Visual Validation**: Reference screenshots enable visual regression testing. The reconstructed app's screens are compared against reference screenshots, with allowances for dynamic content regions. This catches layout issues that HTML comparison might miss.

## Directory Structure

```
phoenix/
├── README.md              # This document
├── schema/
│   └── plantsdb.sql       # Database schema (DDL)
├── specs/
│   ├── screens/           # Screen graph and navigation
│   │   └── graph.md       # Screen flow diagram
│   ├── templates/         # HTML templates + reference screenshots
│   │   ├── login.html
│   │   ├── login.png      # Reference screenshot
│   │   ├── register.html
│   │   ├── register.png
│   │   ├── catalog.html
│   │   ├── catalog.png
│   │   ├── product-detail.html
│   │   ├── product-detail.png
│   │   ├── cart.html
│   │   ├── cart.png
│   │   ├── checkout.html
│   │   ├── checkout.png
│   │   ├── order-confirmation.html
│   │   ├── order-confirmation.png
│   │   └── ...
│   ├── actions/           # Action specifications
│   └── domain/            # Entity and business rule specs
├── traces/
│   ├── login-success.jsonl
│   ├── login-failure.jsonl
│   ├── registration-happy-path.jsonl
│   ├── browse-catalog.jsonl
│   ├── add-to-cart.jsonl
│   ├── checkout-happy-path.jsonl
│   └── ...
└── evaluations/
    └── e2e/               # Durable E2E tests
```

## Getting Started

### Capturing Traces

1. Start the application with tracing enabled
2. Start a scenario: `POST /trace/start?scenario=login-success&description=User+logs+in+successfully`
3. Execute the user journey (manually or via automation)
4. Stop the scenario: `POST /trace/stop`
5. Find the trace in `phoenix/traces/login-success.jsonl`

### Extracting Schema

```bash
mysqldump -u dbuser -p --no-data plantsdb > phoenix/schema/plantsdb.sql
```

## FAQ

**Q: Why not just keep the tests and throw away the specs?**

Tests verify behavior but don't explain intent. Specifications capture the "why" and provide context that helps reconstruction. They're complementary.

**Q: What about non-functional requirements (performance, security)?**

These are important but often implementation-dependent. Phoenix focuses on functional behavior. Non-functional requirements can be documented separately as constraints for reconstruction.

**Q: Can this really work for complex applications?**

The more complex the application, the more valuable Phoenix becomes. Complex apps are hard to understand and risky to rewrite. Complete specifications reduce that risk.

**Q: What if the original application has bugs?**

Traces and specs capture actual behavior, bugs included. During reconstruction, you decide whether to preserve bug-compatibility or fix them. The specs make this decision explicit.

---

*Phoenix: Because the best documentation is the one that lets you rebuild from scratch.*
