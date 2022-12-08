package moe.mewore.e2e;

import moe.mewore.e2e.modes.LaunchAndTestJar;
import moe.mewore.e2e.modes.TestAppDirectly;
import moe.mewore.e2e.modes.TestAppThroughLogFile;
import moe.mewore.e2e.settings.RunSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private static final String UNCHECKED = " ";
    private static final String CHECKED = "+";

    public static void main(final String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        final RunSettings runSettings = RunSettings.createFromArgs(args);

        final boolean hasJarOption = runSettings.hasJarToTest();
        final boolean hasLogFileOption = runSettings.getLogFile() != null;
        final boolean hasDirectOption = runSettings.getPort() != null;
        LOGGER.info(String.format("%n" + """
                        ===========================================================================================
                        =========================================== E2E ===========================================
                        ===========================================================================================
                        | Modes the E2E testing can run in (will pick the topmost available one):
                        | [%s] Run and test .jar file - if the first command line argument is "(...).jar"
                        |       $ java -jar /path/to/e2e.jar /path/to/app.jar # Only works if it has the ".jar" extension!
                        | [%s] Read the log file of an application - enabled in such ways:
                        |       $ java -jar /path/to/e2e.jar --log-file /path/to/file.log
                        |       $ java -jar /path/to/e2e.jar -l /path/to/file.log
                        |       $ %s=/path/to/file.log java -jar /path/to/e2e.jar
                        |       $ java -jar /path/to/e2e.jar /path/to/file.log # Only works if it has the ".log" extension!
                        | [%s] Directly test a running application - enabled in such ways:
                        |       $ java -jar /path/to/e2e.jar --port 8000
                        |       $ java -jar /path/to/e2e.jar -p 8000
                        |       $ %s=8000 java -jar /path/to/e2e.jar
                        ===========================================================================================
                        | Other options:
                        | [%s] Use custom E2E code - enabled in such ways:
                        |       $ java -jar /path/to/e2e.jar --custom-e2e /path/to/your/custom/e2e.jar
                        |       $ java -jar /path/to/e2e.jar -c /path/to/your/custom/e2e.jar
                        |       $ %s=/path/to/your/custom/e2e.jar java -jar /path/to/e2e.jar
                        |     (Custom E2E code location: %s)
                        | [%s] Use a custom app output tracker (only makes sense with custom E2E code) - enabled in such ways:
                        |       $ java -jar /path/to/e2e.jar --output-tracker your.package.YourOutputTracker
                        |       $ java -jar /path/to/e2e.jar -o your.package.YourOutputTracker
                        |       $ %s=your.package.YourOutputTracker java -jar /path/to/e2e.jar
                        |     (Custom app output tracker: %s)
                        | [%s] Use a custom app verifier (only makes sense with custom E2E code) - enabled in such ways:
                        |       $ java -jar /path/to/e2e.jar --app-verifier your.package.YourAppVerifier
                        |       $ java -jar /path/to/e2e.jar -a your.package.YourAppVerifier
                        |       $ %s=your.package.YourAppVerifier java -jar /path/to/e2e.jar
                        |     (Custom app verifier: %s)
                        ===========================================================================================
                        """.replace("\n", "%n"), hasJarOption ? CHECKED : UNCHECKED, hasLogFileOption ? CHECKED : UNCHECKED,
                RunSettings.LOG_FILE_ENV_VAR, hasDirectOption ? CHECKED : UNCHECKED, RunSettings.PORT_ENV_VAR,
                runSettings.getCustomE2eJar() != null ? CHECKED : UNCHECKED, RunSettings.CUSTOM_E2E_JAR_ENV_VAR,
                Optional.ofNullable(runSettings.getCustomE2eJar()).orElse("[none]"),
                runSettings.getAppOutputTrackerClass() != null ? CHECKED : UNCHECKED,
                RunSettings.E2E_APP_OUTPUT_TRACKER_ENV_VAR,
                Optional.ofNullable(runSettings.getAppOutputTrackerClass()).map(Class::getName).orElse("[none]"),
                runSettings.getAppVerifierClass() != null ? CHECKED : UNCHECKED, RunSettings.E2E_APP_VERIFIER_ENV_VAR,
                Optional.ofNullable(runSettings.getAppVerifierClass()).map(Class::getName).orElse("[none]")));
        if (hasJarOption) {
            LaunchAndTestJar.test(runSettings);
        } else if (hasLogFileOption) {
            TestAppThroughLogFile.test(runSettings);
        } else if (hasDirectOption) {
            TestAppDirectly.test(runSettings);
        } else {
            throw new IllegalArgumentException("There are no modes I can run in!");
        }
    }
}
