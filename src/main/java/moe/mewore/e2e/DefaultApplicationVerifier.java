package moe.mewore.e2e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class DefaultApplicationVerifier extends ApplicationVerifierBase {

    private static final Logger LOGGER = LogManager.getLogger(DefaultApplicationVerifier.class);

    @Override
    public void testApplication(final String protocol, final int port) throws IOException {
        LOGGER.info("Verifying application running at: " + protocol + "://localhost:" + port);
        super.testApplication(protocol, port);
    }

    @Override
    protected void verifyResponse(final String targetUrl, final String expectedResponse, final String actualResponse) {
        if (actualResponse.contains(expectedResponse)) {
            DefaultApplicationVerifier.LOGGER.info(
                    "Got the expected response '" + expectedResponse + "' from " + targetUrl);
        } else {
            DefaultApplicationVerifier.LOGGER.error(
                    "Did not get the expected response '" + expectedResponse + "' from " + targetUrl +
                            "\nThe actual response was:\n" + actualResponse);
            throw new IllegalStateException("Did not get the expected response from " + targetUrl);
        }
    }
}
