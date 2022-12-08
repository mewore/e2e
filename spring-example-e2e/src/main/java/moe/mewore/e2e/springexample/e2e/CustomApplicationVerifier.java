package moe.mewore.e2e.springexample.e2e;

import moe.mewore.e2e.ApplicationVerifierBase;

@SuppressWarnings("unused")
public class CustomApplicationVerifier extends ApplicationVerifierBase {

    @Override
    protected void verifyResponse(final String targetUrl, final String expectedResponse, final String actualResponse) {
        if (expectedResponse.contains(actualResponse)) {
            System.out.println("yey!!!");
        } else {
            throw new IllegalStateException("Did not get the expected response from " + targetUrl);
        }
    }
}
