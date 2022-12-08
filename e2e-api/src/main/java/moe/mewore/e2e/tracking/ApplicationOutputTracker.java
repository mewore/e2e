package moe.mewore.e2e.tracking;

import moe.mewore.e2e.tracking.event.AppFailureToStartEvent;
import moe.mewore.e2e.tracking.event.AppStartEvent;
import moe.mewore.e2e.tracking.event.AppSuccessfulStartEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.time.Duration;

/**
 * Classes implementing this interface should have a constructor that accepts a single {@link moe.mewore.e2e.output.LineScanner} parameter.
 */
public interface ApplicationOutputTracker extends AutoCloseable {

    /**
     * Wait for the application tracked by this tracker to start. May be of type {@link AppFailureToStartEvent} or {@link AppSuccessfulStartEvent}.
     *
     * @param timeout How much to wait for the application to start.
     * @return The application event, or {@code null} if nothing has happened in the specified time.
     * @throws InterruptedException If interrupted while waiting.
     */
    @Nullable AppStartEvent waitForAppToStart(Duration timeout) throws InterruptedException;

    @Override
    void close() throws IOException;
}
