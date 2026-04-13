// ABOUTME: YAML-driven acceptance test runner for the legacy JSF application.
// ABOUTME: Reads scenario files from src/test/resources/scenarios/ and drives Playwright.
package it.xpug.pbw.acceptance;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScenarioRunnerTest {

    private static final Path SCENARIOS_DIR = Paths.get("src/test/resources/scenarios");

    private Playwright playwright;
    private Browser browser;

    @BeforeAll
    void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    void teardown() {
        browser.close();
        playwright.close();
    }

    Stream<Arguments> scenarioFiles() throws IOException {
        return Files.list(SCENARIOS_DIR)
                .filter(p -> p.toString().endsWith(".yaml"))
                .sorted()
                .map(p -> arguments(named(p.getFileName().toString(), p)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarioFiles")
    @SuppressWarnings("unchecked")
    void runScenario(Path scenarioFile) throws Exception {
        Yaml yaml = createYaml();
        Map<String, Object> scenario = yaml.load(Files.readString(scenarioFile));
        List<Map<String, Object>> steps = (List<Map<String, Object>>) scenario.get("steps");

        Page page = browser.newPage();
        try {
            boolean updated = executeSteps(page, steps, scenarioFile);
            if (updated) {
                Files.writeString(scenarioFile, yaml.dump(scenario));
                fail("Snapshot(s) captured in " + scenarioFile.getFileName() + ". Review and re-run.");
            }
        } finally {
            page.close();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean executeSteps(Page page, List<Map<String, Object>> steps, Path scenarioFile) {
        boolean updated = false;
        for (Map<String, Object> step : steps) {
            if (step.containsKey("navigate")) {
                page.navigate((String) step.get("navigate"));
                page.waitForLoadState(LoadState.NETWORKIDLE);

            } else if (step.containsKey("click")) {
                Map<String, String> click = (Map<String, String>) step.get("click");
                AriaRole role = AriaRole.valueOf(click.get("role").toUpperCase());
                page.getByRole(role, new Page.GetByRoleOptions().setName(click.get("name"))).click();
                page.waitForLoadState(LoadState.NETWORKIDLE);

            } else if (step.containsKey("assert_snapshot")) {
                Map<String, Object> snapshot = (Map<String, Object>) step.get("assert_snapshot");
                String target = (String) snapshot.get("target");
                String actual = page.locator(target).ariaSnapshot();
                String expected = (String) snapshot.get("contents");

                if (expected == null) {
                    snapshot.put("contents", actual);
                    updated = true;
                } else {
                    assertThat(actual)
                            .as("Aria snapshot does not match %s. "
                                    + "To update, remove the 'contents' key and re-run.", scenarioFile.getFileName())
                            .isEqualTo(expected);
                }
            }
        }
        return updated;
    }

    private static Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);

        Representer representer = new Representer(options) {
            @Override
            protected Node representScalar(Tag tag, String value, DumperOptions.ScalarStyle style) {
                if (value.contains("\n")) {
                    style = DumperOptions.ScalarStyle.LITERAL;
                }
                return super.representScalar(tag, value, style);
            }
        };

        return new Yaml(representer, options);
    }
}
