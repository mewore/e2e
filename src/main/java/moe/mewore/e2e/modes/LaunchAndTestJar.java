package moe.mewore.e2e.modes;

import moe.mewore.e2e.ApplicationVerifier;
import moe.mewore.e2e.ApplicationVerifierFactory;
import moe.mewore.e2e.output.LineScanner;
import moe.mewore.e2e.output.ProcessLineScanner;
import moe.mewore.e2e.settings.RunSettings;
import moe.mewore.e2e.tracking.AppTrackerFactory;
import moe.mewore.e2e.tracking.ApplicationOutputTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LaunchAndTestJar {

    private static final Logger LOGGER = LogManager.getLogger(LaunchAndTestJar.class);

    public static void test(final RunSettings runSettings) throws IOException, InterruptedException, ClassNotFoundException {
        LOGGER.info("E2E testing a .jar...");
        try {
            new LaunchAndTestJar().run(runSettings);
        } catch (final IOException | RuntimeException | ClassNotFoundException e) {
            LOGGER.error("JAR application verification failed!", e);
            throw e;
        }
    }

    private void run(final RunSettings runSettings) throws IOException, InterruptedException, ClassNotFoundException {
        final List<String> args = runSettings.getRemainingArgs();
        if (args.isEmpty()) {
            throw new IllegalArgumentException("There should be at least 1 argument - the .jar file to run!");
        }
        final File jarFile = new File(args.get(0));
        if (!jarFile.isFile()) {
            throw new IllegalArgumentException(jarFile.getAbsolutePath() + " is not a file!");
        }
        final String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null || javaHome.isBlank()) {
            throw new IllegalStateException("JAVA_HOME is not set!");
        }
        final File linuxJava = Path.of(javaHome, "bin", "java").toFile();
        final File windowsJava = Path.of(javaHome, "bin", "java.exe").toFile();
        if (!linuxJava.canExecute() && !windowsJava.canExecute()) {
            throw new IllegalStateException(
                    String.format("Neither %s nor %s an executable!", linuxJava.getAbsolutePath(),
                            windowsJava.getAbsolutePath()));
        }

        final File javaToUse = linuxJava.canExecute() ? linuxJava : windowsJava;
        final List<String> commandParts =
                Arrays.stream(new String[]{javaToUse.getAbsolutePath(), "-jar"}).collect(Collectors.toList());
        commandParts.addAll(args);

        final AppTrackerFactory appTrackerFactory = new AppTrackerFactory(runSettings);
        appTrackerFactory.pretestAppTrackerInstantiation();
        final ApplicationVerifier applicationVerifier = new ApplicationVerifierFactory(runSettings).instantiateAppVerifier();
        LOGGER.info("Executing: " + String.join(" ", commandParts));
        final Process process = new ProcessBuilder().command(commandParts.toArray(String[]::new))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

        LOGGER.info("Spawned process with ID: " + process.pid());
        try (final LineScanner lineScanner = new ProcessLineScanner(process)) {
            try (final ApplicationOutputTracker appTracker = appTrackerFactory.instantiateAppTracker(lineScanner)) {
                applicationVerifier.testApplication(appTracker);
            }
        } finally {
            process.destroy();
            if (process.waitFor(10L, TimeUnit.SECONDS)) {
                LOGGER.info("Process exited with code: " + process.waitFor());
            } else {
                LOGGER.error("Failed to stop process! Trying to terminate process with ID: " +
                        process.destroyForcibly().pid());
            }
        }
    }
}
