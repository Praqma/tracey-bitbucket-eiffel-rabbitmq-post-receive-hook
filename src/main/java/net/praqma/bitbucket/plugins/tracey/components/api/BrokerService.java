package net.praqma.bitbucket.plugins.tracey.components.api;

import net.praqma.bitbucket.plugins.tracey.exceptions.BrokerServiceException;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQRoutingInfo;

public interface BrokerService {

    void send(String message, RabbitMQRoutingInfo destination) throws BrokerServiceException;

}