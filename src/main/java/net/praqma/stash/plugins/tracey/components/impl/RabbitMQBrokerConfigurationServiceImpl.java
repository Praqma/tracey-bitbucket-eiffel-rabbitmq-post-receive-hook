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
    private String exchangeName = RabbitMQDefaults.EXCHANGE_NAME;
    private String exchangeType = RabbitMQDefaults.EXCHANGE_TYPE;
    private String routingkey = "";
    private int port = RabbitMQDefaults.PORT;

    public RabbitMQBrokerConfigurationServiceImpl(RepositoryHookContext context) {
        host = context.getSettings().getString("rabbit.url", host);
        user = context.getSettings().getString("rabbit.user", user);
        pwd = context.getSettings().getString("rabbit.password", pwd);
        exchangeName = context.getSettings().getString("rabbit.exchange.name", exchangeName);
        exchangeType = context.getSettings().getString("rabbit.exchange.type", exchangeType);
        routingkey = context.getSettings().getString("rabbit.routingkey", routingkey);
        port = context.getSettings().getInt("", port);

        LOG.info("RabbitMQ broker configured");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return user;
    }

    public String getPassword() {
        return pwd;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getExchangeType() { return exchangeType; }
}