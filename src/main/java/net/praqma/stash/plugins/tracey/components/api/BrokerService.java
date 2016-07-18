package net.praqma.stash.plugins.tracey.components.api;

import net.praqma.stash.plugins.tracey.exceptions.BrokerServiceException;

public interface BrokerService {

    void send(String message, String destination) throws BrokerServiceException;

}