package moe.mewore.e2e.settings;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RunSettingsTest {

    @Test
    void testCreateFromArgs() throws IOException, ClassNotFoundException {
        final RunSettings runSettings = RunSettings.createFromArgs(new String[]{
                "--port",
                "8080",
                "--log-file",
                "/log/file",
                "--output-tracker",
                getClass().getName(),
                "--app-verifier",
                RunSettings.class.getName()
        });
        assertEquals(8080, runSettings.getPort());
        assertEquals("/log/file", runSettings.getLogFile());
        assertNull(runSettings.getCustomE2eJar());
        assertSame(getClass(), runSettings.getAppOutputTrackerClass());
        assertSame(RunSettings.class, runSettings.getAppVerifierClass());
    }

    @Test
    void testCreateFromArgs_single() throws IOException, ClassNotFoundException {
        final RunSettings runSettings = RunSettings.createFromArgs(new String[]{
                "-p",
                "8080",
                "-l",
                "/log/file",
                "-o",
                getClass().getName(),
                "-a",
                RunSettings.class.getName()
        });
        assertEquals(8080, runSettings.getPort());
        assertEquals("/log/file", runSettings.getLogFile());
        assertNull(runSettings.getCustomE2eJar());
        assertSame(getClass(), runSettings.getAppOutputTrackerClass());
        assertSame(RunSettings.class, runSettings.getAppVerifierClass());
    }
}