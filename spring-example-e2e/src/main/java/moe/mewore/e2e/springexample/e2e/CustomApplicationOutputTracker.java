package moe.mewore.e2e.springexample.e2e;

import moe.mewore.e2e.output.LineScanner;
import moe.mewore.e2e.tracking.ApplicationOutputTrackerBase;
import moe.mewore.e2e.tracking.event.AppStartEvent;
import moe.mewore.e2e.tracking.event.AppSuccessfulStartEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class CustomApplicationOutputTracker extends ApplicationOutputTrackerBase {

    private static final Pattern CUSTOM_PORT_PATTERN = Pattern.compile("The port is: (\\d+)");
    private static final Pattern CUSTOM_PROTOCOL_PATTERN = Pattern.compile("The protocol is: (\\S+)");

    private String protocol = "";
    private int port = -1;

    public CustomApplicationOutputTracker(final LineScanner applicationOutput) {
        super(applicationOutput);
    }

    @Override
    protected @Nullable AppStartEvent inspectLine(final String line) {
        if (line.contains("Done waiting! Fully initialized!")) {
            return new AppSuccessfulStartEvent(protocol, port);
        }

        final Matcher portMatcher = CUSTOM_PORT_PATTERN.matcher(line);
        if (portMatcher.find()) {
            port = Integer.parseUnsignedInt(portMatcher.group(1));
        }
        final Matcher protocolMatcher = CUSTOM_PROTOCOL_PATTERN.matcher(line);
        if (protocolMatcher.find()) {
            protocol = protocolMatcher.group(1);
        }
        return null;
    }
}
