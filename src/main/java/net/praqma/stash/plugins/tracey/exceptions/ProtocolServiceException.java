package net.praqma.stash.plugins.tracey.exceptions;

public class ProtocolServiceException extends Exception {

    public ProtocolServiceException(final String message, final Throwable error) {
        super(message, error);
    }

    public ProtocolServiceException(final String message) {
        super(message);
    }
}
