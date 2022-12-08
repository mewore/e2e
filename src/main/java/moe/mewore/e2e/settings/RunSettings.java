package moe.mewore.e2e.settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RunSettings {

    public static final String PROTOCOL_ENV_VAR = "PROTOCOL";
    public static final String PORT_ENV_VAR = "PORT";
    public static final String LOG_FILE_ENV_VAR = "LOG_FILE";
    public static final String CUSTOM_E2E_JAR_ENV_VAR = "CUSTOM_E2E";
    public static final String E2E_APP_OUTPUT_TRACKER_ENV_VAR = "E2E_APP_OUTPUT_TRACKER";
    public static final String E2E_APP_VERIFIER_ENV_VAR = "E2E_APP_VERIFIER";

    private static final Pattern LONG_PARAMETER_PATTERN = Pattern.compile("--(\\S+)");

    private final @Nullable String protocol;

    private final @Nullable Integer port;

    private final @Nullable String logFile;

    private final @Nullable String customE2eJar;

    private final @Nullable Class<?> appOutputTrackerClass;

    private final @Nullable Class<?> appVerifierClass;

    @Getter
    private final List<String> remainingArgs;

    public boolean hasJarToTest() {
        return !remainingArgs.isEmpty() && remainingArgs.get(0).endsWith(".jar");
    }

    @SuppressWarnings("OverlyComplexMethod")
    public static RunSettings createFromArgs(final String[] args) throws ClassNotFoundException, IOException {
        final Queue<String> argDeque = Arrays.stream(args).collect(Collectors.toCollection(ArrayDeque::new));
        @Nullable Integer port =
                Optional.ofNullable(System.getenv(PORT_ENV_VAR)).map(Integer::parseUnsignedInt).orElse(null);
        @Nullable String logFile = System.getenv(LOG_FILE_ENV_VAR);
        @Nullable String customE2eJarPath = System.getenv(CUSTOM_E2E_JAR_ENV_VAR);
        @Nullable String outputTrackerClassName = System.getenv(E2E_APP_OUTPUT_TRACKER_ENV_VAR);
        @Nullable String appVerifierClassName = System.getenv(E2E_APP_VERIFIER_ENV_VAR);
        while (!argDeque.isEmpty()) {
            final String paramKey;
            if (argDeque.element().startsWith("--")) {
                switch (paramKey = argDeque.remove()) {
                    case "--port" -> port = Integer.parseUnsignedInt(argDeque.remove());
                    case "--log-file" -> logFile = argDeque.remove();
                    case "--custom-e2e" -> customE2eJarPath = argDeque.remove();
                    case "--output-tracker" -> outputTrackerClassName = argDeque.remove();
                    case "--app-verifier" -> appVerifierClassName = argDeque.remove();
                    default -> throw new UnrecognizedParameterKeyException(paramKey);
                }
            } else if (argDeque.element().startsWith("-") && argDeque.element().length() == 2) {
                switch (paramKey = argDeque.remove()) {
                    case "-p" -> port = Integer.parseUnsignedInt(argDeque.remove());
                    case "-l" -> logFile = argDeque.remove();
                    case "-c" -> customE2eJarPath = argDeque.remove();
                    case "-o" -> outputTrackerClassName = argDeque.remove();
                    case "-a" -> appVerifierClassName = argDeque.remove();
                    default -> throw new UnrecognizedParameterKeyException(paramKey);
                }
            } else {
                break;
            }
        }
        if (logFile == null && !argDeque.isEmpty() && argDeque.element().endsWith(".log")) {
            logFile = argDeque.element();
        }

        final CustomTestClasses customTestClasses =
                CustomTestClasses.createFromJar(customE2eJarPath, outputTrackerClassName, appVerifierClassName);

        return new RunSettings(System.getenv(PROTOCOL_ENV_VAR), port, logFile, customE2eJarPath,
                customTestClasses.getAppOutputTrackerClass(), customTestClasses.getAppVerifierClass(),
                argDeque.stream().collect(Collectors.toUnmodifiableList()));
    }
}
