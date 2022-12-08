package moe.mewore.e2e.settings;

public class UnrecognizedParameterKeyException extends RuntimeException {

    public UnrecognizedParameterKeyException(final String key) {
        super(String.format("Unrecognized parameter key '%s'", key));
    }
}
