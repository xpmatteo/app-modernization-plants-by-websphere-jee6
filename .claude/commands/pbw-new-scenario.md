Help me create a new acceptance test scenario for the Plants by WebSphere project.

Start by reading the existing scenario files in
`acceptance-tests/src/test/resources/scenarios/` so you follow the same patterns.

---

## Step format

Scenario files are YAML with a `steps` list. The supported step types are:

```yaml
# Navigate to a URL
- navigate: http://localhost:9080/promo.jsf

# Click an element by ARIA role and accessible name
- click:
    role: link          # AriaRole in lowercase: link, button, menuitem, checkbox, etc.
    name: Bonsai Tree $30.00 each

# Click an element by CSS locator (use when the element has no accessible name)
- click:
    locator: '[id="orderinfo:shipisbill"]'

# Fill a form field by CSS locator
- fill:
    locator: '[id="login:email"]'
    value: plants@plantsbywebsphere.ibm.com

# Fill a form field by label text
- fill:
    label: Full Name
    value: David Grover

# Assert the current URL contains a substring
- assert_url: orderinfo.jsf

# Assert the ARIA snapshot of a CSS-locator target
- assert_snapshot:
    target: '[data-testid=''main-content'']'
    # Omit `contents` — the test runner captures it on first run
```

**Important YAML quoting:** single quotes inside a single-quoted YAML string must be
doubled. So `[data-testid='main-content']` becomes `'[data-testid=''main-content'']'`.

The conventional snapshot target in this project is `[data-testid='main-content']`.
If the target page lacks that attribute, check the page HTML to find which elements
carry `data-testid` attributes.

**Note on JSF URLs:** JSF uses server-side forwards for form navigation, so the URL
does not change after form submissions. `assert_url` reflects the URL of the page
that submitted the form, not the rendered destination. Use it to verify you are on
the expected page before interacting with it.

---

## Information to gather

If $ARGUMENTS already answers some of these, skip those questions. Otherwise ask
one question at a time:

1. What user journey should the test cover?
2. What is the entry URL?
3. What clicks happen along the way?
4. Which part of the page should the snapshot cover?

---

## Delivering the scenario

Save the file to `acceptance-tests/src/test/resources/scenarios/<descriptive-name>.yaml`.
Do NOT include `contents` in any `assert_snapshot` step.

After saving, tell me to capture the initial snapshots by running:
```
cd acceptance-tests && mvn test
```
Then review the received snapshots with `make approve-snapshots`.

$ARGUMENTS
