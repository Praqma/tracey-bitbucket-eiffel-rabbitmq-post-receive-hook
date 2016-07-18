package net.praqma.stash.plugins.tracey.exceptions;

public class BrokerServiceException extends Exception {
    public BrokerServiceException(final String message, final Throwable error) {
        super(message, error);
    }
}
