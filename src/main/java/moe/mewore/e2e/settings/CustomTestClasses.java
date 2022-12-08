package moe.mewore.e2e.settings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import moe.mewore.e2e.ApplicationVerifier;
import moe.mewore.e2e.tracking.ApplicationOutputTracker;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CustomTestClasses {

    private final @Nullable Class<?> appOutputTrackerClass;

    private final @Nullable Class<?> appVerifierClass;

    public static CustomTestClasses createFromJar(final @Nullable String jarPath,
            final @Nullable String outputTrackerClassName,
            final @Nullable String appVerifierClassName) throws IOException, ClassNotFoundException {

        if (jarPath == null) {
            return new CustomTestClasses(outputTrackerClassName != null ? Class.forName(outputTrackerClassName) : null,
                    appVerifierClassName != null ? Class.forName(appVerifierClassName) : null);
        }

        final File jarRawFile = new File(jarPath);
        if (!jarRawFile.isFile()) {
            throw new IllegalArgumentException("There is no file: " + jarRawFile.getAbsolutePath());
        }
        if (!jarRawFile.canRead()) {
            throw new IllegalArgumentException("Cannot read file: " + jarRawFile.getAbsolutePath());
        }
        final ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{jarRawFile.toURI().toURL()});
        final List<Class<?>> customClasses = new ArrayList<>();
        if (outputTrackerClassName == null || appVerifierClassName == null) {
            try (final JarFile customE2eJar = new JarFile(jarRawFile)) {
                final Enumeration<JarEntry> entries = customE2eJar.entries();
                while (entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();
                    if (name.endsWith(".class")) {
                        customClasses.add(
                                Class.forName(name.substring(0, name.lastIndexOf('.')).replace("/", "."), true,
                                        classLoader));
                    }
                }
            }
        }
        final Class<?> outputTrackerClass =
                findCustomClass(outputTrackerClassName, ApplicationOutputTracker.class, customClasses, classLoader);
        final Class<?> appVerifierClass =
                findCustomClass(appVerifierClassName, ApplicationVerifier.class, customClasses, classLoader);

        return new CustomTestClasses(outputTrackerClass, appVerifierClass);
    }

    private static @Nullable Class<?> findCustomClass(final @Nullable String desiredClassName, final Class<?> inherits,
            final List<Class<?>> customClasses, final ClassLoader classLoader) throws ClassNotFoundException {
        if (desiredClassName != null) {
            return Class.forName(desiredClassName, true, classLoader);
        }
        return customClasses.stream()
                .filter(cls -> !Modifier.isAbstract(cls.getModifiers()) && !Modifier.isInterface(cls.getModifiers()) &&
                        inherits.isAssignableFrom(cls))
                .findAny()
                .orElse(null);
    }
}
