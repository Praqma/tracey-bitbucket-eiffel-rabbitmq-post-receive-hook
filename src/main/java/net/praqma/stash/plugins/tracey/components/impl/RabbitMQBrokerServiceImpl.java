package net.praqma.stash.plugins.tracey.components.impl;

import net.praqma.stash.plugins.tracey.components.api.BrokerService;
import net.praqma.stash.plugins.tracey.components.api.BrokerConfigurationService;
import net.praqma.stash.plugins.tracey.exceptions.BrokerServiceException;
import net.praqma.tracey.broker.TraceyBroker;
import net.praqma.tracey.broker.TraceyIOError;
import net.praqma.tracey.broker.TraceyValidatorError;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQBrokerImpl;
import net.praqma.tracey.broker.rabbitmq.TraceyRabbitMQSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQBrokerServiceImpl implements BrokerService {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQBrokerServiceImpl.class);
    private final TraceyBroker broker;

    public RabbitMQBrokerServiceImpl(final BrokerConfigurationService brokerConfigurationService) {
        // Read configuration when it is implemented
        this.broker = new TraceyRabbitMQBrokerImpl();
        broker.setSender(new TraceyRabbitMQSenderImpl(((RabbitMQBrokerConfigurationServiceImpl) brokerConfigurationService).getHost(),
                ((RabbitMQBrokerConfigurationServiceImpl) brokerConfigurationService).getUsername(),
                ((RabbitMQBrokerConfigurationServiceImpl) brokerConfigurationService).getPassword(),
                ((RabbitMQBrokerConfigurationServiceImpl) brokerConfigurationService).getPort()));
    }

    @Override
    public void send(String message, String destination) throws BrokerServiceException{
        log.debug("Ready to send the following message to desination " + destination + "\n" + message);
        try {
            broker.send(message, destination);
        } catch (TraceyValidatorError|TraceyIOError error) {
            throw new BrokerServiceException("Failed to send RabbitMQ messages", error);
        }
    }
}