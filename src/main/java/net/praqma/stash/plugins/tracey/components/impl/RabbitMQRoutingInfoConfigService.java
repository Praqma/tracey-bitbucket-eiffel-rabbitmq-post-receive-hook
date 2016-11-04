package net.praqma.stash.plugins.tracey.components.impl;

import com.atlassian.stash.hook.repository.RepositoryHookContext;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQDefaults;
import net.praqma.tracey.broker.impl.rabbitmq.RabbitMQRoutingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class RabbitMQRoutingInfoConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQRoutingInfoConfigService.class);
    private Map<String, Object> headers = new HashMap<>();
    private String exchangeName = RabbitMQDefaults.EXCHANGE_NAME;
    private String exchangeType = RabbitMQDefaults.EXCHANGE_TYPE;
    private String routingKey = RabbitMQDefaults.ROUTING_KEY;
    private int deliveryMode = RabbitMQDefaults.DELEIVERY_MODE;

    public RabbitMQRoutingInfoConfigService(RepositoryHookContext context) {
        exchangeName = context.getSettings().getString("rabbit.exchange.name");
        exchangeType = context.getSettings().getString("rabbit.exchange.type");
        routingKey = context.getSettings().getString("rabbit.routingkey");
        deliveryMode = context.getSettings().getInt("rabbit.deliverymode");
        LOG.debug("Routing info configured for exchange name: " + exchangeName);
        LOG.debug("Routing info configured for exchange type: " + exchangeType);
    }

    public RabbitMQRoutingInfo destination(){
        return new RabbitMQRoutingInfo(headers, deliveryMode, routingKey, exchangeName,exchangeType);
    }
    public Map<String, Object> getHeaders() {
        return headers;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public int getDeliveryMode() {
        return deliveryMode;
    }
}