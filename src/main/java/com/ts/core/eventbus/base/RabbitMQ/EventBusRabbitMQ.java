package com.ts.core.eventbus.base.RabbitMQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ts.core.eventbus.EventBusConfig;
import com.ts.core.eventbus.base.abstracts.BaseEventBus;
import com.ts.core.eventbus.base.abstracts.IIntegrationEventHandler;
import com.ts.core.eventbus.base.events.IntegrationEvent;

import com.rabbitmq.client.*;
import com.ts.core.eventbus.base.pubsub.pub.RabbitMqOnRemovedEventHandlerPub;
import com.ts.core.eventbus.base.pubsub.sub.RabbitMQOnRemovedEventHandler;
import com.ts.core.eventbus.base.submanager.InMemoryEventBusSubscriptionManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class EventBusRabbitMQ extends BaseEventBus {
    private RabbitMQPersistentConnection persistentConnection;
    private final ConnectionFactory connectionFactory;
    private final Channel consumerChannel;
    private final EventBusConfig eventBusConfig;

    public EventBusRabbitMQ(EventBusConfig eventBusConfig,ApplicationContext applicationContext) throws IOException, TimeoutException {
        super(eventBusConfig,applicationContext);
        this.eventBusConfig = eventBusConfig;

        if (eventBusConfig.getConnection() != null) {
            String conJson = new JSONObject(eventBusConfig.getConnection()).toString();

            this.connectionFactory = new ObjectMapper().readValue(conJson, ConnectionFactory.class);
        } else {
            this.connectionFactory = new ConnectionFactory();
        }

        this.persistentConnection = new RabbitMQPersistentConnection(this.connectionFactory, this.eventBusConfig.getConnectionRetryCount());
        this.consumerChannel = createConsumerChannel();

        // Register the event removal handler
        RabbitMQOnRemovedEventHandler handler = new RabbitMQOnRemovedEventHandler();
        Consumer<String> consumer = this::subscriptionManager_OnEventRemoved;
        handler.addConsumer(consumer);
        RabbitMqOnRemovedEventHandlerPub.getInstance().addOnEventRemovedListener(handler);
    }

    private void subscriptionManager_OnEventRemoved(String eventName) {
        eventName = processEventName(eventName);

        if (!persistentConnection.isConnected()) {
            persistentConnection.tryConnect();
        }

        try {
            consumerChannel.queueUnbind(
                    eventName,
                    eventBusConfig.getDefaultTopicName(),
                    eventName
            );

            if (super.subscriptionManager.isEmpty()) {
                consumerChannel.close();
            }
        } catch (IOException | TimeoutException e) {
            // Handle exception as needed
        }
    }

    @Override
    public void publish(IntegrationEvent event) {
        if (!persistentConnection.isConnected()) {
            persistentConnection.tryConnect();
        }

        String eventName = event.getClass().getSimpleName();
        eventName = this.processEventName(eventName);

        try {
            consumerChannel.exchangeDeclare(eventBusConfig.getDefaultTopicName(), "direct");
        } catch (IOException e) {
            throw new RuntimeException("Failed to declare an exchange", e);
        }

        Gson gson = new Gson();
        String message = gson.toJson(event);
        byte[] body = message.getBytes(StandardCharsets.UTF_8);

        String finalEventName = eventName;
        executeWithRetry(() -> {
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .deliveryMode(2) // persistent

                    .build();

            try {
                consumerChannel.basicPublish(
                        eventBusConfig.getDefaultTopicName(),
                        finalEventName,
                        true,
                        properties,
                        body
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to publish a message", e);
            }
        });
    }

    private void executeWithRetry(Runnable action) {
        for (int i = 0; i < eventBusConfig.getConnectionRetryCount(); i++) {
            try {
                action.run();
                return;
            } catch (Exception ex) {
                try {
                    TimeUnit.SECONDS.sleep((long) Math.pow(2, i));
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new RuntimeException("Failed to execute action with retries");
    }
    @Override
    public <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void subscribe(Class<T> event,Class<TH> handler) {
        String eventName = event.getSimpleName();
        eventName = processEventName(eventName);

        if (!super.subscriptionManager.hasSubscriptionsForEvent(eventName)) {
            if (!persistentConnection.isConnected()) {
                persistentConnection.tryConnect();
            }

            try {
                consumerChannel.queueDeclare(
                        getSubName(eventName),
                        true,
                        false,
                        false,
                        null
                );
                consumerChannel.queueBind(
                        getSubName(eventName),
                        eventBusConfig.getDefaultTopicName(),
                        eventName
                );
            } catch (IOException e) {
                // Handle exception as needed
            }
        }

        super.subscriptionManager.addSubscription(event,handler);
        startBasicConsume(eventName);
    }

    @Override
    public <T extends IntegrationEvent, TH extends IIntegrationEventHandler<T>> void unsubscribe(Class<T> event,Class<TH> handler) {
        super.subscriptionManager.removeSubscription(event, handler);
    }

    private Channel createConsumerChannel() throws IOException {
        if (!persistentConnection.isConnected()) {
            persistentConnection.tryConnect();
        }

        Channel channel = persistentConnection.createModel();
        channel.exchangeDeclare(eventBusConfig.getDefaultTopicName(), "direct");

        return channel;
    }

    private void startBasicConsume(String eventName) {
        if (consumerChannel != null) {
            var consumer = new DefaultConsumer(consumerChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    String routingKey = envelope.getRoutingKey();

                    try {
                        processEvent(routingKey, message);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    consumerChannel.basicAck(envelope.getDeliveryTag(), false);
                }
            };

            try {
                consumerChannel.basicConsume(getSubName(eventName), false, consumer);
            } catch (IOException e) {
                // Handle exception as needed
            }
        }
    }
}

