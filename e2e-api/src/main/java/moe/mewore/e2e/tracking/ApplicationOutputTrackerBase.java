package moe.mewore.e2e.tracking;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import moe.mewore.e2e.output.LineScanner;
import moe.mewore.e2e.tracking.event.AppFailureToStartEvent;
import moe.mewore.e2e.tracking.event.AppStartEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public abstract class ApplicationOutputTrackerBase implements ApplicationOutputTracker {

    private final LineScanner applicationOutput;

    protected final BlockingQueue<AppStartEvent> appStartEventQueue = new ArrayBlockingQueue<>(1);
    protected final BlockingQueue<Boolean> initializationDoneResultQueue = new ArrayBlockingQueue<>(1);

    private boolean closed = false;
    private Thread trackingThread;

    protected abstract @Nullable AppStartEvent inspectLine(final String line);

    @Synchronized
    private void initializeTrackingThread() {
        if (trackingThread == null && !closed) {
            trackingThread = new Thread(this::trackOutput);
            trackingThread.start();
        }
    }

    protected void trackOutput() {
        while (applicationOutput.mayHaveNextLine()) {
            final String line;
            try {
                line = applicationOutput.nextLine();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (final IOException e) {
                e.printStackTrace();
                continue;
            }
            System.out.print("| ");
            System.out.println(line);
            final @Nullable AppStartEvent event = inspectLine(line);
            if (event != null && !appStartEventQueue.offer(event)) {
                warn("A second application start event has been detected?!");
            }
        }
        offerDummies();
    }

    @Override
    public @Nullable AppStartEvent waitForAppToStart(final Duration timeout) throws InterruptedException {
        initializeTrackingThread();
        final @Nullable AppStartEvent result = appStartEventQueue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (result != null && !appStartEventQueue.offer(result)) {
            warn("A second application start event has been detected?!");
        }
        return result;
    }

    @Override
    @Synchronized
    public void close() {
        closed = true;
        trackingThread.interrupt();
        offerDummies();
    }

    private void offerDummies() {
        if (appStartEventQueue.peek() == null && appStartEventQueue.offer(new AppFailureToStartEvent())) {
            warn("Still no app event; sending a failure one");
        }
    }

    protected void warn(final String message) {
        System.out.println(getClass().getSimpleName() + " :: [WARN] :: " + message);
    }
}
