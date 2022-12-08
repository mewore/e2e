package moe.mewore.e2e.modes;

import moe.mewore.e2e.ApplicationVerifierFactory;
import moe.mewore.e2e.settings.RunSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class TestAppDirectly {

    private static final Logger LOGGER = LogManager.getLogger(TestAppDirectly.class);

    public static void test(final RunSettings runSettings) throws IOException {
        try {
            final String protocol = Optional.ofNullable(runSettings.getProtocol()).orElseGet(() -> {
                LOGGER.warn("The '" + RunSettings.PROTOCOL_ENV_VAR +
                        "' environment variable has not been set; assuming it's 'http'");
                return "http";
            });
            final int port = Optional.ofNullable(runSettings.getPort())
                    .orElseThrow(() -> new IllegalStateException("No port has been set!"));
            new ApplicationVerifierFactory(runSettings).instantiateAppVerifier().testApplication(protocol, port);
        } catch (final IOException | RuntimeException e) {
            LOGGER.error("HTTP application E2E testing through its log file failed!", e);
            throw e;
        }
    }
}
