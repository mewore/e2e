package moe.mewore.e2e.modes;

import moe.mewore.e2e.ApplicationVerifierFactory;
import moe.mewore.e2e.output.FileLineScanner;
import moe.mewore.e2e.output.LineScanner;
import moe.mewore.e2e.settings.RunSettings;
import moe.mewore.e2e.tracking.AppTrackerFactory;
import moe.mewore.e2e.tracking.ApplicationOutputTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;

public class TestAppThroughLogFile {

    private static final Logger LOGGER = LogManager.getLogger(TestAppThroughLogFile.class);

    public static void test(final RunSettings runSettings) throws IOException, InterruptedException {
        try {
            final AppTrackerFactory appTrackerFactory = new AppTrackerFactory(runSettings);
            appTrackerFactory.pretestAppTrackerInstantiation();

            final @Nullable String logFilePath = System.getenv("LOG_FILE");
            if (logFilePath == null || logFilePath.isBlank()) {
                throw new IllegalArgumentException("The 'LOG_FILE' environment variable should be set and not blank!");
            }
            final File logFile = new File(logFilePath);
            LOGGER.info("Using the 'LOG_FILE' environment variable: " + logFilePath);
            try (final LineScanner lineScanner = new FileLineScanner(logFile)) {
                try (final ApplicationOutputTracker appTracker = appTrackerFactory.instantiateAppTracker(lineScanner)) {
                    new ApplicationVerifierFactory(runSettings).instantiateAppVerifier().testApplication(appTracker);
                }
            }
        } catch (final IOException | RuntimeException e) {
            LOGGER.error("HTTP application E2E testing through its log file failed!", e);
            throw e;
        }
    }
}
