package moe.mewore.e2e;

import lombok.RequiredArgsConstructor;
import moe.mewore.e2e.settings.RunSettings;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@RequiredArgsConstructor
public class ApplicationVerifierFactory {

    private final RunSettings runSettings;

    public ApplicationVerifier instantiateAppVerifier() {
        final @Nullable Class<?> appVerifierClass = runSettings.getAppVerifierClass();
        if (appVerifierClass == null) {
            return new DefaultApplicationVerifier();
        }
        if (!ApplicationVerifier.class.isAssignableFrom(appVerifierClass)) {
            throw new IllegalArgumentException(
                    String.format("The class [%s] (%s environment variable) does not implement [%s]",
                            appVerifierClass.getName(), RunSettings.E2E_APP_VERIFIER_ENV_VAR, ApplicationVerifier.class.getName()));
        }

        final Constructor<?> constructor;
        try {
            constructor = appVerifierClass.getConstructor();
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format(
                    "Class [%s] (%s environment variable) does not have a constructor accepting no parameters",
                    appVerifierClass.getName(), RunSettings.E2E_APP_VERIFIER_ENV_VAR), e);
        }

        try {
            if (constructor.canAccess(null)) {
                return (ApplicationVerifier) constructor.newInstance();
            }

            try {
                constructor.setAccessible(true);
                return (ApplicationVerifier) constructor.newInstance();
            } finally {
                constructor.setAccessible(false);
            }
        } catch (final InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format("Failed to instantiate app verifier [%s] despite my best efforts",
                    appVerifierClass.getName()), e);
        }
    }
}
