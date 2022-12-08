package moe.mewore.e2e;

import moe.mewore.e2e.tracking.ApplicationOutputTracker;

import java.io.IOException;

/**
 * Performs E2E tests on an application and throws a runtime exception if not.
 *
 * Custom classes implementing this need to have an empty constructor.
 */
public interface ApplicationVerifier {

    void testApplication(ApplicationOutputTracker appTracker) throws IOException, InterruptedException;

    void testApplication(String protocol, int port) throws IOException;
}
