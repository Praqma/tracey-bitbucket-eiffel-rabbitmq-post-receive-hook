package net.praqma.stash.plugins.tracey.components.api;

import net.praqma.stash.plugins.tracey.exceptions.BrokerServiceException;

public interface BrokerService {

    public void send(String message, String destination) throws BrokerServiceException;

}