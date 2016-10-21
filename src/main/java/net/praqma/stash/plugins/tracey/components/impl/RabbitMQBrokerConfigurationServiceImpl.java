package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import net.praqma.stash.plugins.tracey.components.api.BrokerConfigurationService;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQBrokerConfigurationServiceImpl implements BrokerConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQBrokerConfigurationServiceImpl.class);

    private String host = RabbitMQDefaults.HOST;
    private String user = RabbitMQDefaults.USERNAME;
    private String pwd = RabbitMQDefaults.PASSWORD;
    private String exchange = RabbitMQDefaults.EXCHANGE_NAME;
    private String routingkey = "";
    private int port = RabbitMQDefaults.PORT;

    public RabbitMQBrokerConfigurationServiceImpl(RepositoryHookContext context) {
        host = context.getSettings().getString("rabbit.url", host);
        user = context.getSettings().getString("rabbit.user", user);
        pwd = context.getSettings().getString("rabbit.password", pwd);
        exchange = context.getSettings().getString("rabbit.exchange", exchange);
        routingkey = context.getSettings().getString("rabbit.routingkey", routingkey);
        port = context.getSettings().getInt("", port);

        LOG.warn("Implement me!");
    }

    // TODO: read this from global configuration
    public String getHost() {
        return host;
    }

    // TODO: read this from global configuration
    public int getPort() {
        return port;
    }

    // TODO: read this from global configuration
    public String getUsername() {
        return user;
    }

    // TODO: read this from global configuration
    public String getPassword() {
        return pwd;
    }

    // TODO: read this from repo configuration
    public String getExchange() {
        return exchange;
    }
}