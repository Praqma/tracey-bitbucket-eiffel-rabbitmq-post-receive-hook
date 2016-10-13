package net.praqma.stash.plugins.tracey.components.api;

import net.praqma.stash.plugins.tracey.exceptions.BrokerServiceException;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQRoutingInfo;

public interface BrokerService {

    void send(String message, RabbitMQRoutingInfo destination) throws BrokerServiceException;

}