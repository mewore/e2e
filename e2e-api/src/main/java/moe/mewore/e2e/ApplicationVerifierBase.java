package moe.mewore.e2e;

import moe.mewore.e2e.tracking.ApplicationOutputTracker;
import moe.mewore.e2e.tracking.event.AppStartEvent;
import moe.mewore.e2e.tracking.event.AppSuccessfulStartEvent;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ApplicationVerifierBase implements ApplicationVerifier {

    private static final int SECONDS_TO_WAIT = 30;
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(SECONDS_TO_WAIT);
    private static final Pattern ENV_EXPECTATION_URL_PATTERN = Pattern.compile("E2E:(.+)", Pattern.CASE_INSENSITIVE);

    @Override
    public void testApplication(final ApplicationOutputTracker appTracker) throws IOException, InterruptedException {
        final AppStartEvent appStartEvent = Optional.ofNullable(appTracker.waitForAppToStart(WAIT_TIMEOUT))
                .orElseThrow(() -> new IllegalStateException(
                        "The application was not initialized in " + SECONDS_TO_WAIT + " seconds!"));
        if (!(appStartEvent instanceof AppSuccessfulStartEvent)) {
            throw new IllegalStateException("The application failed to start");
        }
        final AppSuccessfulStartEvent successfulStartEvent = (AppSuccessfulStartEvent) appStartEvent;
        testApplication(successfulStartEvent.getProtocol(), successfulStartEvent.getPort());
    }

    @Override
    public void testApplication(final String protocol, final int port) throws IOException {
        final String applicationUrl = protocol + "://localhost:" + port;
        testApplication(applicationUrl);
    }

    protected abstract void verifyResponse(final String targetUrl, final String expectedResponse, final String actualResponse);

    protected void testApplication(final String applicationUrl) throws IOException {
        for (final Map.Entry<String, String> environmentVariable : System.getenv().entrySet()) {
            final Matcher expectationMatcher = ENV_EXPECTATION_URL_PATTERN.matcher(environmentVariable.getKey());
            if (expectationMatcher.find()) {
                final String subUrl = expectationMatcher.group(1).trim();
                final String url = (subUrl.isEmpty() || subUrl.startsWith("/"))
                        ? (applicationUrl + subUrl)
                        : applicationUrl + "/" + subUrl;
                testUrl(url, environmentVariable.getValue());
            }
        }
    }

    protected void testUrl(final String targetUrl, final String expectedResponse) throws IOException {
        final URLConnection connection = new URL(targetUrl).openConnection();
        try (final Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.US_ASCII)) {
            scanner.useDelimiter("\\Z");
            verifyResponse(targetUrl, expectedResponse, scanner.next());
        }
    }
}
