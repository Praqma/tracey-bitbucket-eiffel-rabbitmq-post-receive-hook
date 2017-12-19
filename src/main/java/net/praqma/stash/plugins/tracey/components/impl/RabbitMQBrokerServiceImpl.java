package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.setting.Settings;
import net.praqma.stash.plugins.tracey.components.api.BrokerService;
import net.praqma.stash.plugins.tracey.exceptions.BrokerServiceException;
import net.praqma.tracey.broker.api.TraceyBroker;
import net.praqma.tracey.broker.api.TraceyIOError;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQConnection;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQRoutingInfo;
import net.praqma.tracey.broker.impl.rabbitmq.TraceyRabbitMQBrokerImpl;
import net.praqma.tracey.broker.impl.rabbitmq.TraceyRabbitMQSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQBrokerServiceImpl implements BrokerService {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQBrokerServiceImpl.class);
    private final TraceyBroker broker;

    public RabbitMQBrokerServiceImpl(RepositoryHookContext context) {
        // Read configuration when it is implemented
        this.broker = new TraceyRabbitMQBrokerImpl();
        final Settings settings = context.getSettings();
        final RabbitMQConnection connection = new RabbitMQConnection(
                settings.getString("rabbit.url"),
                settings.getInt("rabbit.port"),
                settings.getString("rabbit.user"),
                settings.getString("rabbit.password"),
                true);
        broker.setSender(new TraceyRabbitMQSenderImpl(connection));
    }

    @Override
    public void send(String message, RabbitMQRoutingInfo destination) throws BrokerServiceException{
        LOG.debug("Ready to send the following message to desination " + destination + "\n" + message);
        try {
            broker.send(message, destination);
        } catch (TraceyIOError error) {
            throw new BrokerServiceException("Failed to send RabbitMQ messages", error);
        }
    }
}