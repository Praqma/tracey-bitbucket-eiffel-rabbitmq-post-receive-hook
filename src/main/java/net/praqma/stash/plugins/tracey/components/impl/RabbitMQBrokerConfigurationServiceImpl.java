package net.praqma.stash.plugins.tracey.components.impl;

import net.praqma.stash.plugins.tracey.components.api.BrokerConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQBrokerConfigurationServiceImpl implements BrokerConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQBrokerConfigurationServiceImpl.class);

    public RabbitMQBrokerConfigurationServiceImpl() {
    }

    // TODO: read this from global configuration
    public String getHost() {
        return "localhost";
    }

    // TODO: read this from global configuration
    public int getPort() {
        return 5672;
    }

    // TODO: read this from global configuration
    public String getUsername() {
        return "guest";
    }

    // TODO: read this from global configuration
    public String getPassword() {
        return "guest";
    }

    // TODO: read this from repo configuration
    public String getExchange() {
        return "EiffelSourceChangeCreatedEvent";
    }
}