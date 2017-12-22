package net.praqma.bitbucket.plugins.tracey.components.impl;

import net.praqma.bitbucket.plugins.tracey.components.api.BrokerConfigurationService;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQBrokerConfigurationServiceImpl implements BrokerConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQBrokerConfigurationServiceImpl.class);
    
    public RabbitMQBrokerConfigurationServiceImpl() {
        LOG.warn("Implement me!");
    }

    // TODO: read this from global configuration
    public String getHost() {
        return RabbitMQDefaults.HOST;
    }

    // TODO: read this from global configuration
    public int getPort() {
        return RabbitMQDefaults.PORT;
    }

    // TODO: read this from global configuration
    public String getUsername() {
        return RabbitMQDefaults.USERNAME;
    }

    // TODO: read this from global configuration
    public String getPassword() {
        return RabbitMQDefaults.PASSWORD;
    }

    // TODO: read this from repo configuration
    public String getExchange() {
        return RabbitMQDefaults.EXCHANGE_NAME;
    }
}