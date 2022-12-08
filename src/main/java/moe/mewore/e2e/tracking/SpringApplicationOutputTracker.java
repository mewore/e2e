package moe.mewore.e2e.tracking;

import moe.mewore.e2e.output.LineScanner;
import moe.mewore.e2e.tracking.event.AppFailureToStartEvent;
import moe.mewore.e2e.tracking.event.AppStartEvent;
import moe.mewore.e2e.tracking.event.AppSuccessfulStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringApplicationOutputTracker extends ApplicationOutputTrackerBase {

    private final Logger logger = LogManager.getLogger(getClass());

    private @Nullable Integer port = null;
    private @Nullable String protocol = null;
    private @Nullable Boolean appStartStatus = null;

    private static final Pattern TOMCAT_START_PATTERN =
            Pattern.compile("Tomcat started on port\\(s\\): (\\d+) \\((https?)\\)");

    private static final Pattern SPRING_APP_START_PATTERN = Pattern.compile("Started \\S+ in \\S+ seconds");

    public SpringApplicationOutputTracker(final LineScanner output) {
        super(output);
    }

    @Override
    protected void trackOutput() {
        logger.info("Tracking application output...");
        super.trackOutput();
        logger.info("Reached the end of the application output");
    }

    @Override
    protected void warn(final String message) {
        logger.warn(message);
    }

    @Override
    protected @Nullable AppStartEvent inspectLine(final String line) {
        if (SPRING_APP_START_PATTERN.matcher(line).find()) {
            appStartStatus = true;
            return getAppStartEvent();
        }
        if (line.contains("APPLICATION FAILED TO START")) {
            appStartStatus = false;
            return getAppStartEvent();
        }
        final Matcher tomcatStartMatcher = TOMCAT_START_PATTERN.matcher(line);
        if (tomcatStartMatcher.find()) {
            port = Integer.parseInt(tomcatStartMatcher.group(1));
            protocol = tomcatStartMatcher.group(2);
            return getAppStartEvent();
        }
        return null;
    }

    private @Nullable AppStartEvent getAppStartEvent() {
        if (appStartStatus == null) {
            return null;
        }
        if (!appStartStatus) {
            return new AppFailureToStartEvent();
        }
        if (port != null && protocol != null) {
            return new AppSuccessfulStartEvent(protocol, port);
        }
        return null;
    }
}
