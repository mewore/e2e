package moe.mewore.e2e.tracking;

import lombok.RequiredArgsConstructor;
import moe.mewore.e2e.output.LineScanner;
import moe.mewore.e2e.settings.RunSettings;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@RequiredArgsConstructor
public class AppTrackerFactory {

    private final RunSettings runSettings;

    public void pretestAppTrackerInstantiation() {
        instantiateAppTracker(new EmptyLineScanner());
    }

    public ApplicationOutputTracker instantiateAppTracker(final LineScanner appOutputLines) {
        final @Nullable Class<?> appTrackerClass = runSettings.getAppOutputTrackerClass();
        if (appTrackerClass == null) {
            return new SpringApplicationOutputTracker(appOutputLines);
        }
        if (!ApplicationOutputTracker.class.isAssignableFrom(appTrackerClass)) {
            throw new IllegalArgumentException(
                    String.format("The class [%s] (%s environment variable) does not implement [%s]",
                            appTrackerClass.getName(), RunSettings.E2E_APP_OUTPUT_TRACKER_ENV_VAR,
                            ApplicationOutputTracker.class.getName()));
        }

        final Constructor<?> constructor;
        try {
            constructor = appTrackerClass.getConstructor(LineScanner.class);
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format(
                    "Class [%s] (%s environment variable) does not have a constructor accepting a single [%s] parameter",
                    appTrackerClass.getName(), RunSettings.E2E_APP_OUTPUT_TRACKER_ENV_VAR, LineScanner.class.getName()),
                    e);
        }

        try {
            if (constructor.canAccess(null)) {
                return (ApplicationOutputTracker) constructor.newInstance(appOutputLines);
            }

            try {
                constructor.setAccessible(true);
                return (ApplicationOutputTracker) constructor.newInstance(appOutputLines);
            } finally {
                constructor.setAccessible(false);
            }
        } catch (final InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format("Failed to instantiate app tracker [%s] despite my best efforts",
                    appTrackerClass.getName()), e);
        }
    }

    private static class EmptyLineScanner implements LineScanner {
        @Override
        public String nextLine() {
            return "";
        }

        @Override
        public boolean mayHaveNextLine() {
            return false;
        }

        @Override
        public void close() {

        }
    }
}
