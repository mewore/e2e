package moe.mewore.e2e;

import moe.mewore.e2e.output.DummyLineScanner;
import moe.mewore.e2e.tracking.ApplicationOutputTrackerBase;
import moe.mewore.e2e.tracking.event.AppStartEvent;
import moe.mewore.e2e.tracking.event.AppSuccessfulStartEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ApplicationVerifierBaseTest {

    @Test
    void testApplication_appOutputTracker() throws IOException, InterruptedException {
        new ImplementedApplicationVerifier().testApplication(
                new ApplicationOutputTrackerBase(new DummyLineScanner("a")) {
                    @Override
                    protected AppStartEvent inspectLine(final String line) {
                        return new AppSuccessfulStartEvent("http", 42069);
                    }
                });
    }

    @Test
    void testApplication_protocolAndPort() throws IOException {
        new ImplementedApplicationVerifier().testApplication("http", 42069);
    }

    private static class ImplementedApplicationVerifier extends ApplicationVerifierBase {

        @Override
        protected void verifyResponse(final String targetUrl, final String expectedResponse,
                final String actualResponse) {
            // Do nothing
        }
    }
}