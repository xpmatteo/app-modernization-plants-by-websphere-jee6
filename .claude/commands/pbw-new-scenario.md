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

There is no fill/type step — form input is not yet supported by the runner.

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
