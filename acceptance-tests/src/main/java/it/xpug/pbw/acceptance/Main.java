// ABOUTME: Entry point for running acceptance tests directly via the JUnit Launcher API.
// ABOUTME: Prints each scenario name as it starts; exits with non-zero status on failure.
package it.xpug.pbw.acceptance;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class Main {
    public static void main(String[] args) {
        var request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(ScenarioRunnerTest.class))
                .build();

        var summary = new SummaryGeneratingListener();

        LauncherFactory.create().execute(request, summary, new TestExecutionListener() {
            @Override
            public void executionStarted(TestIdentifier id) {
                if (id.isTest()) {
                    System.out.println(">>> " + id.getDisplayName());
                }
            }

            @Override
            public void executionFinished(TestIdentifier id, TestExecutionResult result) {
                if (id.isTest() && result.getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
                    System.out.println("    FAILED: " + id.getDisplayName());
                    result.getThrowable().ifPresent(Throwable::printStackTrace);
                }
            }
        });

        var s = summary.getSummary();
        System.out.printf("%nTests run: %d, Failures: %d, Time: %.2fs%n",
                s.getTestsStartedCount(),
                s.getTestsFailedCount(),
                (s.getTimeFinished() - s.getTimeStarted()) / 1000.0);

        System.exit(s.getTestsFailedCount() > 0 ? 1 : 0);
    }
}
