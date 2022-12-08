package moe.mewore.e2e.settings;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomTestClassesTestIT {

    @Test
    void testCreateFromJar() throws IOException, ClassNotFoundException {
        try (final InputStream stream = Objects.requireNonNull(
                getClass().getClassLoader().getResource("spring-example-e2e-0.0.1-SNAPSHOT.jar"),
                "spring-example-e2e-0.0.1-SNAPSHOT.jar should exist").openStream()) {
            final File tmpFile = File.createTempFile("testCreateFromJar", ".jar");
            try (final FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
                fileOutputStream.write(stream.readAllBytes());
            }
            final CustomTestClasses customTestClasses = CustomTestClasses.createFromJar(tmpFile.getAbsolutePath(),
                    "moe.mewore.e2e.springexample.e2e.CustomApplicationOutputTracker",
                    "moe.mewore.e2e.springexample.e2e.CustomApplicationVerifier");
            assertNotNull(customTestClasses.getAppOutputTrackerClass());
            assertNotNull(customTestClasses.getAppVerifierClass());
        }
    }

    @Test
    void testCreateFromJar_noCustomClasses() throws IOException, ClassNotFoundException {
        try (final InputStream stream = Objects.requireNonNull(
                getClass().getClassLoader().getResource("spring-example-e2e-0.0.1-SNAPSHOT.jar"),
                "spring-example-e2e-0.0.1-SNAPSHOT.jar should exist").openStream()) {
            final File tmpFile = File.createTempFile("testCreateFromJar", ".jar");
            try (final FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
                fileOutputStream.write(stream.readAllBytes());
            }
            final CustomTestClasses customTestClasses =
                    CustomTestClasses.createFromJar(tmpFile.getAbsolutePath(), null, null);
            assertNotNull(customTestClasses.getAppOutputTrackerClass());
            assertNotNull(customTestClasses.getAppVerifierClass());
        }
    }

    @Test
    void testCreateFromJar_noCustomClasses_noJar() throws IOException, ClassNotFoundException {
        final CustomTestClasses customTestClasses = CustomTestClasses.createFromJar(null, null, null);
        assertNull(customTestClasses.getAppOutputTrackerClass());
        assertNull(customTestClasses.getAppVerifierClass());
    }

    @Test
    void testCreateFromJar_noJar() throws IOException, ClassNotFoundException {
        final CustomTestClasses customTestClasses =
                CustomTestClasses.createFromJar(null, Manmenmi.class.getName(), Manmenmi.class.getName());
        assertSame(Manmenmi.class, customTestClasses.getAppOutputTrackerClass());
        assertSame(Manmenmi.class, customTestClasses.getAppVerifierClass());
    }

    @Test
    void testCreateFromJar_noSuchFile() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CustomTestClasses.createFromJar("nonexistent", null, null));
        assertTrue(exception.getMessage().startsWith("There is no file:"));
    }

    @Test
    void testCreateFromJar_cannotReadFile() throws IOException {
        final File tmpFile = File.createTempFile("testCreateFromJar_cannotReadFile", ".jar");
        assertTrue(tmpFile.setReadable(false));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CustomTestClasses.createFromJar(tmpFile.getAbsolutePath(), null, null));
        assertEquals("Cannot read file: " + tmpFile.getAbsolutePath(), exception.getMessage());
    }

    private static class Manmenmi {

    }
}